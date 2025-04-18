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
     * 默认超时时间：10分钟
     */
    private static final long DEFAULT_TIMEOUT = 10 * 60 * 1000L;

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
     * 创建新的SSE连接
     *
     * @param taskId 任务ID
     * @return SseEmitter实例
     */
    @Override
    public SseEmitter createEmitter(String taskId) {
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
            log.error("SSE connection error for task: {}", taskId, ex);
            removeEmitter(taskId);
        });
        
        // 存储连接
        emitters.put(taskId, emitter);
        log.info("Created SSE connection for task: {}", taskId);
        
        // 发送初始连接成功事件
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(eventIdGenerator.incrementAndGet()))
                    .name("connect")
                    .data("Connected successfully"));
        } catch (IOException e) {
            log.error("Failed to send initial event for task: {}", taskId, e);
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
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("taskId", taskId);
        data.put("status", status);
        if (error != null && !error.isEmpty()) {
            data.put("error", error);
        }
        
        return sendEvent(taskId, "status", data);
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
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("taskId", taskId);
        data.put("name", name);
        data.put("url", url);
        
        return sendEvent(taskId, "result", data);
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
            log.warn("No active SSE connection found for task: {}", taskId);
            return false;
        }
        
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(eventIdGenerator.incrementAndGet()))
                    .name(eventName)
                    .data(jsonData));
            return true;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event data for task: {}", taskId, e);
            return false;
        } catch (IOException e) {
            log.error("Failed to send event to client for task: {}", taskId, e);
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
