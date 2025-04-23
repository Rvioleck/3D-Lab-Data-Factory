package com.elwg.ai3dbackend.service.impl;

import com.elwg.ai3dbackend.service.EventStreamService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 事件流服务实现类
 * <p>
 * 管理活跃的SSE连接，提供创建连接、发送事件、处理连接关闭/超时的功能
 * </p>
 */
@Slf4j
@Service
public class EventStreamServiceImpl implements EventStreamService {

    /**
     * 默认超时时间：30分钟
     */
    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    /**
     * 心跳间隔：30秒
     */
    private static final long HEARTBEAT_INTERVAL = 30 * 1000L;

    /**
     * 存储活跃的SSE连接
     */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 事件ID生成器
     */
    private final AtomicLong eventIdGenerator = new AtomicLong(0);

    /**
     * JSON序列化工具
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 心跳任务Map
     */
    private final Map<String, Runnable> heartbeatTasks = new ConcurrentHashMap<>();

    /**
     * 创建新的SSE连接
     *
     * @param taskId 任务ID
     * @return SseEmitter实例
     */
    @Override
    public SseEmitter createEmitter(String taskId) {
        // 先移除现有的连接（如果有）
        SseEmitter existingEmitter = emitters.get(taskId);
        if (existingEmitter != null) {
            log.info("Removing existing SSE connection for task: {}", taskId);
            try {
                existingEmitter.complete();
            } catch (Exception e) {
                log.warn("Error completing existing emitter for task: {}", taskId, e);
            }
            removeEmitter(taskId);
        }

        // 创建新的发射器
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 设置连接建立时的回调
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for task: {}", taskId);
            removeEmitter(taskId);
        });

        // 设置连接超时的回调
        emitter.onTimeout(() -> {
            log.info("SSE connection timeout for task: {}", taskId);
            removeEmitter(taskId);
        });

        // 设置连接错误的回调
        emitter.onError(ex -> {
            log.error("SSE connection error for task: {}, error: {}", taskId, ex.getMessage(), ex);
            removeEmitter(taskId);
        });

        // 存储连接
        emitters.put(taskId, emitter);
        log.info("Created SSE connection for task: {}, active connections: {}", taskId, emitters.size());

        // 发送初始连接成功事件
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(eventIdGenerator.incrementAndGet()))
                    .name("connect")
                    .data("Connected successfully"));

            // 启动心跳任务
            startHeartbeat(taskId);
            log.info("Initial connect event sent for task: {}", taskId);
        } catch (IOException e) {
            log.error("Failed to send initial event for task: {}, error: {}", taskId, e.getMessage(), e);
            removeEmitter(taskId);
            return null;
        }

        return emitter;
    }

    /**
     * 获取指定任务的SSE连接
     *
     * @param taskId 任务ID
     * @return SseEmitter实例，如果不存在则返回null
     */
    @Override
    public SseEmitter getEmitter(String taskId) {
        return emitters.get(taskId);
    }

    /**
     * 移除指定任务的SSE连接
     *
     * @param taskId 任务ID
     */
    @Override
    public void removeEmitter(String taskId) {
        SseEmitter emitter = emitters.remove(taskId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("Error completing emitter for task: {}", taskId, e);
            }
        }

        // 停止心跳任务
        stopHeartbeat(taskId);
    }

    /**
     * 启动心跳任务
     *
     * @param taskId 任务ID
     */
    private void startHeartbeat(String taskId) {
        Runnable heartbeatTask = () -> {
            while (emitters.containsKey(taskId)) {
                try {
                    // 发送心跳事件
                    boolean sent = sendEvent(taskId, "heartbeat", "Heartbeat at " + System.currentTimeMillis());
                    if (!sent) {
                        log.warn("Failed to send heartbeat to task: {}, removing emitter", taskId);
                        removeEmitter(taskId);
                        break;
                    }

                    // 等待下一次心跳
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    log.warn("Heartbeat task interrupted for task: {}", taskId);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error in heartbeat task for task: {}", taskId, e);
                    removeEmitter(taskId);
                    break;
                }
            }
            heartbeatTasks.remove(taskId);
        };

        // 存储心跳任务
        heartbeatTasks.put(taskId, heartbeatTask);

        // 启动心跳线程
        Thread heartbeatThread = new Thread(heartbeatTask);
        heartbeatThread.setDaemon(true);
        heartbeatThread.setName("sse-heartbeat-" + taskId);
        heartbeatThread.start();

        log.debug("Started heartbeat for task: {}", taskId);
    }

    /**
     * 停止心跳任务
     *
     * @param taskId 任务ID
     */
    private void stopHeartbeat(String taskId) {
        heartbeatTasks.remove(taskId);
        log.debug("Stopped heartbeat for task: {}", taskId);
    }

    /**
     * 发送状态更新事件
     *
     * @param taskId 任务ID
     * @param status 状态
     * @param error  错误信息（可选）
     * @return 是否发送成功
     */
    @Override
    public boolean sendStatusEvent(String taskId, String status, String error) {
        log.info("Preparing to send status event for task: {}, status: {}, error: {}", taskId, status, error);

        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("taskId", taskId);
        data.put("status", status);
        if (error != null && !error.isEmpty()) {
            data.put("error", error);
        }

        boolean result = sendEvent(taskId, "status", data);
        if (result) {
            log.info("Successfully sent status event for task: {}, status: {}", taskId, status);
        } else {
            log.warn("Failed to send status event for task: {}, status: {}", taskId, status);
        }

        return result;
    }

    /**
     * 发送结果可用事件
     *
     * @param taskId 任务ID
     * @param name   结果文件名
     * @param url    结果文件URL
     * @return 是否发送成功
     */
    @Override
    public boolean sendResultEvent(String taskId, String name, String url) {
        log.info("Preparing to send result event for task: {}, file: {}, url: {}", taskId, name, url);

        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("taskId", taskId);
        data.put("name", name);
        data.put("url", url);

        boolean result = sendEvent(taskId, "result", data);
        if (result) {
            log.info("Successfully sent result event for task: {}, file: {}", taskId, name);
        } else {
            log.warn("Failed to send result event for task: {}, file: {}", taskId, name);
        }

        return result;
    }

    /**
     * 发送自定义事件
     *
     * @param taskId    任务ID
     * @param eventName 事件名称
     * @param data      事件数据
     * @return 是否发送成功
     */
    @Override
    public boolean sendEvent(String taskId, String eventName, Object data) {
        SseEmitter emitter = getEmitter(taskId);
        if (emitter == null) {
            log.warn("No active SSE connection found for task: {}, event: {}", taskId, eventName);
            return false;
        }

        try {
            String jsonData = objectMapper.writeValueAsString(data);
            long eventId = eventIdGenerator.incrementAndGet();
            log.debug("Sending event: {}, id: {}, to task: {}", eventName, eventId, taskId);

            emitter.send(SseEmitter.event()
                    .id(String.valueOf(eventId))
                    .name(eventName)
                    .data(jsonData));

            log.debug("Event sent successfully: {}, id: {}, to task: {}", eventName, eventId, taskId);
            return true;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event data for task: {}, event: {}, error: {}",
                    taskId, eventName, e.getMessage(), e);
            return false;
        } catch (IOException e) {
            log.error("Failed to send event to client for task: {}, event: {}, error: {}",
                    taskId, eventName, e.getMessage(), e);
            removeEmitter(taskId);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending event to client for task: {}, event: {}, error: {}",
                    taskId, eventName, e.getMessage(), e);
            removeEmitter(taskId);
            return false;
        }
    }

    /**
     * 完成指定任务的SSE连接
     *
     * @param taskId 任务ID
     */
    @Override
    public void completeEmitter(String taskId) {
        SseEmitter emitter = getEmitter(taskId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("Error completing emitter for task: {}", taskId, e);
            } finally {
                removeEmitter(taskId);
            }
        }
    }

    /**
     * 获取所有活跃的SSE连接
     *
     * @return 任务ID到SseEmitter的映射
     */
    @Override
    public Map<String, SseEmitter> getActiveEmitters() {
        return emitters;
    }
}
