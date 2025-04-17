package com.elwg.ai3dbackend.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * WebSocket服务接口
 * <p>
 * 提供与WebSocket服务器通信的功能，包括发送图片数据和接收处理结果
 * </p>
 */
public interface WebSocketService {

    /**
     * 结果部分回调接口
     * 用于处理渐进式接收的结果部分
     */
    interface ResultPartCallback {
        /**
         * 处理结果部分
         *
         * @param name 结果部分名称
         * @param contentType 内容类型
         * @param data 二进制数据
         */
        void onResultPart(String name, String contentType, byte[] data);

        /**
         * 处理状态更新
         *
         * @param status 状态（processing, completed, failed）
         * @param error 错误信息（如果状态为failed）
         */
        void onStatusUpdate(String status, String error);
    }

    /**
     * 发送图片数据到WebSocket服务器并获取处理结果（旧方法，保留向后兼容性）
     *
     * @param imageData 图片二进制数据
     * @return 包含处理结果的CompletableFuture，结果为ZIP文件的二进制数据
     * @throws IOException 如果通信过程中发生错误
     * @deprecated 使用 {@link #sendImageAndProcessResult(byte[], String, ResultPartCallback)} 代替
     */
    @Deprecated
    CompletableFuture<byte[]> sendImageAndGetResult(byte[] imageData) throws IOException;

    /**
     * 发送图片数据到WebSocket服务器并渐进式处理结果
     *
     * @param imageData 图片二进制数据
     * @param taskId 任务ID
     * @param callback 结果部分回调
     * @return 包含最终状态的CompletableFuture
     * @throws IOException 如果通信过程中发生错误
     */
    CompletableFuture<String> sendImageAndProcessResult(byte[] imageData, String taskId, ResultPartCallback callback) throws IOException;

    /**
     * 关闭WebSocket连接
     */
    void closeConnection();

    /**
     * 检查WebSocket连接状态
     *
     * @return 如果连接已建立并且是开放的，则返回true
     */
    boolean isConnected();

    /**
     * 重置WebSocket连接
     * 关闭现有连接并重置状态，下次请求时将尝试重新连接
     */
    void resetConnection();

    /**
     * 初始化WebSocket连接
     * 主动建立与WebSocket服务器的连接
     *
     * @return 如果连接成功建立，则返回true
     */
    boolean initConnection();
}
