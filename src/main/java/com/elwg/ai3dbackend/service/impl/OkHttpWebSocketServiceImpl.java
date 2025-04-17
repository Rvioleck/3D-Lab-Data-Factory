package com.elwg.ai3dbackend.service.impl;

import com.elwg.ai3dbackend.model.websocket.WebSocketMessage;
import com.elwg.ai3dbackend.service.WebSocketService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * WebSocket服务OkHttp实现类
 * <p>
 * 使用OkHttp的WebSocket客户端实现与WebSocket服务器的通信，包括发送图片数据和接收处理结果
 * </p>
 */
@Slf4j
@Service
public class OkHttpWebSocketServiceImpl implements WebSocketService {

    @Value("${websocket.server.url:ws://10.0.0.2:8001/generate3d}")
    private String serverUrl;

    @Value("${websocket.connection.timeout:30}")
    private int connectionTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient client;
    private WebSocket webSocket;
    private final AtomicInteger requestIdCounter = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, CompletableFuture<byte[]>> pendingRequests = new ConcurrentHashMap<>();

    // 渐进式处理相关
    private final ConcurrentHashMap<String, ResultPartCallback> taskCallbacks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<String>> taskFutures = new ConcurrentHashMap<>();
    private final AtomicReference<WebSocketMessage> lastResultPartMessage = new AtomicReference<>();

    // 连接状态
    private volatile boolean isConnected = false;

    // 重连锁，防止并发重连
    private final Object reconnectLock = new Object();

    // 最后一次接收到消息的时间
    private volatile long lastMessageTime = 0;

    // 心跳检查器
    private ScheduledExecutorService heartbeatExecutor;

    /**
     * 构造函数，初始化OkHttp客户端
     */
    public OkHttpWebSocketServiceImpl() {
        // 配置OkHttp客户端，设置超时
        this.client = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)  // 读取超时设置为5分钟，与业务逻辑一致
                .writeTimeout(30, TimeUnit.SECONDS)
                .pingInterval(30, TimeUnit.SECONDS) // 设置心跳间隔
                .build();
    }

    /**
     * 初始化WebSocket客户端
     */
    @PostConstruct
    public void init() {
        try {
            // 启动心跳检查器
            startHeartbeatChecker();

            // 尝试初始连接，但只尝试一次，不重试
            initConnection();
        } catch (Exception e) {
            log.error("Failed to initialize WebSocket client, will retry on first request", e);
        }
    }

    /**
     * 启动心跳检查器
     */
    private void startHeartbeatChecker() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }

        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "websocket-heartbeat-checker");
            t.setDaemon(true);
            return t;
        });

        // 每120秒检查一次连接状态，减少检查频率
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                // 只在已连接但长时间无消息时检查连接状态
                if (isConnected && System.currentTimeMillis() - lastMessageTime > 180000) { // 3分钟
                    log.warn("No message received for 3 minutes, checking connection status");

                    // 发送一个ping来检查连接
                    if (webSocket != null) {
                        boolean sent = webSocket.send("ping");
                        if (!sent) {
                            log.warn("Failed to send ping, connection may be broken");
                            isConnected = false; // 标记为未连接，下次请求时会重连
                        } else {
                            log.info("Ping sent successfully");
                        }
                    }
                }

                // 不主动重连，等待下次请求时重连
            } catch (Exception e) {
                log.error("Error in heartbeat checker", e);
            }
        }, 120, 120, TimeUnit.SECONDS); // 2分钟检查一次
    }

    /**
     * 连接WebSocket服务器
     */
    private void connectWebSocket() {
        if (isConnected) {
            return;
        }

        synchronized (reconnectLock) {
            if (isConnected) {
                return;
            }

            try {
                Request request = new Request.Builder()
                        .url(serverUrl)
                        .build();

                webSocket = client.newWebSocket(request, new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        log.info("WebSocket connection established to {}", serverUrl);
                        isConnected = true;
                        lastMessageTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        log.debug("Received text message: {}", text);
                        lastMessageTime = System.currentTimeMillis();

                        try {
                            // 解析WebSocket消息
                            WebSocketMessage message = objectMapper.readValue(text, WebSocketMessage.class);

                            // 根据消息类型处理
                            if ("status".equals(message.getType())) {
                                // 处理状态消息
                                handleStatusMessage(message);
                            } else if ("result_part".equals(message.getType())) {
                                // 处理结果部分消息
                                log.info("Received result part message: name={}, contentType={}",
                                        message.getName(), message.getContentType());
                                // 保存最后一个结果部分消息，用于关联后续的二进制数据
                                lastResultPartMessage.set(message);
                            }
                        } catch (Exception e) {
                            log.error("Error parsing text message: {}", text, e);
                        }
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, ByteString bytes) {
                        log.info("Received binary message of size: {} bytes", bytes.size());
                        lastMessageTime = System.currentTimeMillis();

                        byte[] binaryData = bytes.toByteArray();

                        // 检查是否有最近的结果部分消息
                        WebSocketMessage resultPartMessage = lastResultPartMessage.getAndSet(null);
                        if (resultPartMessage != null) {
                            // 渐进式处理模式
                            handleResultPartBinaryData(resultPartMessage, binaryData);
                        } else {
                            // 兼容旧的处理方式
                            int requestId = requestIdCounter.get();
                            CompletableFuture<byte[]> future = pendingRequests.remove(requestId);

                            if (future != null) {
                                future.complete(binaryData);
                                log.info("Completed request ID: {}", requestId);
                            } else {
                                log.warn("Received binary message but no pending request found");
                            }
                        }
                    }

                    @Override
                    public void onClosing(WebSocket webSocket, int code, String reason) {
                        log.info("WebSocket closing: code={}, reason={}", code, reason);
                        isConnected = false;
                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        log.info("WebSocket closed: code={}, reason={}", code, reason);
                        isConnected = false;

                        // 处理所有待处理的请求，返回错误
                        pendingRequests.forEach((id, future) ->
                            future.completeExceptionally(new RuntimeException("Connection closed: " + reason)));
                        pendingRequests.clear();

                        // 处理所有渐进式任务
                        taskFutures.forEach((taskId, future) -> {
                            if (!future.isDone()) {
                                future.completeExceptionally(new RuntimeException("Connection closed: " + reason));
                                // 通知回调失败
                                ResultPartCallback callback = taskCallbacks.remove(taskId);
                                if (callback != null) {
                                    callback.onStatusUpdate("failed", "Connection closed: " + reason);
                                }
                            }
                        });
                        taskFutures.clear();
                        taskCallbacks.clear();
                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                        log.error("WebSocket failure", t);
                        isConnected = false;

                        // 处理所有待处理的请求，返回错误
                        pendingRequests.forEach((id, future) ->
                            future.completeExceptionally(t));
                        pendingRequests.clear();

                        // 处理所有渐进式任务
                        taskFutures.forEach((taskId, future) -> {
                            if (!future.isDone()) {
                                future.completeExceptionally(t);
                                // 通知回调失败
                                ResultPartCallback callback = taskCallbacks.remove(taskId);
                                if (callback != null) {
                                    callback.onStatusUpdate("failed", t.getMessage());
                                }
                            }
                        });
                        taskFutures.clear();
                        taskCallbacks.clear();

                        // 处理连接失败，下次请求时重试
                        handleConnectionFailure();
                    }
                });

                // 更新最后消息时间
                lastMessageTime = System.currentTimeMillis();
            } catch (Exception e) {
                log.error("Failed to connect to WebSocket server", e);
                isConnected = false;
                // 不自动重连，下次请求时重试
            }
        }
    }

    /**
     * 处理连接失败，不再自动重连
     */
    private void handleConnectionFailure() {
        log.warn("WebSocket connection failed, will retry on next request");
        // 仅标记为未连接，不主动重连
        isConnected = false;
    }

    /**
     * 初始化WebSocket连接
     * 主动建立与WebSocket服务器的连接
     *
     * @return 如果连接成功建立，则返回true
     */
    @Override
    public boolean initConnection() {
        if (isConnected) {
            return true;
        }

        log.info("Initializing WebSocket connection to {}", serverUrl);
        connectWebSocket();
        return isConnected;
    }

    /**
     * 发送图片数据到WebSocket服务器并获取处理结果
     *
     * @param imageData 图片二进制数据
     * @return 包含处理结果的CompletableFuture，结果为ZIP文件的二进制数据
     * @throws IOException 如果通信过程中发生错误
     */
    @Override
    public CompletableFuture<byte[]> sendImageAndGetResult(byte[] imageData) throws IOException {
        if (!isConnected) {
            log.info("WebSocket client not connected, attempting to connect...");
            // 尝试连接，但只尝试一次
            initConnection();

            // 如果连接失败，立即抛出异常，不重试
            if (!isConnected) {
                throw new IOException("Failed to connect to WebSocket server. Please ensure the 3D reconstruction service is running.");
            }
        }

        int requestId = requestIdCounter.incrementAndGet();
        CompletableFuture<byte[]> resultFuture = new CompletableFuture<>();
        pendingRequests.put(requestId, resultFuture);

        // 发送图片数据
        boolean sent = webSocket.send(ByteString.of(imageData));
        if (!sent) {
            pendingRequests.remove(requestId);
            throw new IOException("Failed to send image data");
        }

        log.info("Sent image data of size: {} bytes, request ID: {}", imageData.length, requestId);

        // 设置超时
        resultFuture.orTimeout(5, TimeUnit.MINUTES)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Request {} timed out or failed", requestId, ex);
                        pendingRequests.remove(requestId);
                    }
                });

        return resultFuture;
    }

    /**
     * 关闭WebSocket连接
     */
    @Override
    public void closeConnection() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
            log.info("WebSocket connection closed");
        }
        isConnected = false;
    }

    /**
     * 检查WebSocket连接状态
     *
     * @return 如果连接已建立并且是开放的，则返回true
     */
    @Override
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 重置WebSocket连接
     * 关闭现有连接并重置状态，下次请求时将尝试重新连接
     */
    @Override
    public void resetConnection() {
        log.info("Manually resetting WebSocket connection");

        // 关闭现有连接
        if (webSocket != null) {
            try {
                webSocket.close(1000, "Manual reset");
            } catch (Exception e) {
                log.warn("Error closing existing WebSocket connection during reset", e);
            }
            webSocket = null;
        }

        // 重置状态
        isConnected = false;

        // 处理所有未完成的请求
        pendingRequests.forEach((id, future) ->
            future.completeExceptionally(new RuntimeException("Connection reset manually")));
        pendingRequests.clear();

        // 处理所有渐进式任务
        taskFutures.forEach((taskId, future) -> {
            if (!future.isDone()) {
                future.completeExceptionally(new RuntimeException("Connection reset manually"));
                // 通知回调失败
                ResultPartCallback callback = taskCallbacks.remove(taskId);
                if (callback != null) {
                    callback.onStatusUpdate("failed", "Connection reset manually");
                }
            }
        });
        taskFutures.clear();
        taskCallbacks.clear();
        lastResultPartMessage.set(null);

        log.info("WebSocket connection has been reset");
    }

    /**
     * 处理状态消息
     *
     * @param message 状态消息
     */
    private void handleStatusMessage(WebSocketMessage message) {
        String status = message.getStatus();
        String error = message.getError();
        log.info("Received status update: {}, error: {}", status, error);

        // 遍历所有任务回调，通知状态更新
        taskCallbacks.forEach((taskId, callback) -> {
            callback.onStatusUpdate(status, error);

            // 如果是最终状态（completed或failed），完成任务
            if ("completed".equals(status) || "failed".equals(status)) {
                CompletableFuture<String> future = taskFutures.remove(taskId);
                if (future != null && !future.isDone()) {
                    future.complete(status);
                }
                taskCallbacks.remove(taskId);
            }
        });
    }

    /**
     * 处理结果部分的二进制数据
     *
     * @param message 结果部分消息
     * @param data 二进制数据
     */
    private void handleResultPartBinaryData(WebSocketMessage message, byte[] data) {
        String name = message.getName();
        String contentType = message.getContentType();
        log.info("Processing binary data for result part: {}, size: {} bytes", name, data.length);

        // 遍历所有任务回调，通知结果部分
        taskCallbacks.forEach((taskId, callback) -> {
            callback.onResultPart(name, contentType, data);
        });
    }

    /**
     * 发送图片数据到WebSocket服务器并渐进式处理结果
     *
     * @param imageData 图片二进制数据
     * @param taskId 任务ID
     * @param callback 结果部分回调
     * @return 包含最终状态的CompletableFuture
     * @throws IOException 如果通信过程中发生错误
     */
    @Override
    public CompletableFuture<String> sendImageAndProcessResult(byte[] imageData, String taskId, ResultPartCallback callback) throws IOException {
        if (!isConnected) {
            log.info("WebSocket client not connected, attempting to connect...");
            // 尝试连接，但只尝试一次
            initConnection();

            // 如果连接失败，立即抛出异常，不重试
            if (!isConnected) {
                throw new IOException("Failed to connect to WebSocket server. Please ensure the 3D reconstruction service is running.");
            }
        }

        // 创建任务完成的Future
        CompletableFuture<String> resultFuture = new CompletableFuture<>();

        // 注册回调和Future
        taskCallbacks.put(taskId, callback);
        taskFutures.put(taskId, resultFuture);

        // 发送图片数据
        boolean sent = webSocket.send(ByteString.of(imageData));
        if (!sent) {
            taskCallbacks.remove(taskId);
            taskFutures.remove(taskId);
            throw new IOException("Failed to send image data");
        }

        log.info("Sent image data of size: {} bytes for task: {}", imageData.length, taskId);

        // 设置超时
        resultFuture.orTimeout(5, TimeUnit.MINUTES)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Task {} timed out or failed", taskId, ex);
                        taskCallbacks.remove(taskId);
                        taskFutures.remove(taskId);
                        callback.onStatusUpdate("failed", "Request timed out after 5 minutes");
                    }
                });

        return resultFuture;
    }

    /**
     * 在应用关闭时关闭WebSocket连接和心跳检查器
     */
    @PreDestroy
    public void destroy() {
        closeConnection();

        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }
    }
}
