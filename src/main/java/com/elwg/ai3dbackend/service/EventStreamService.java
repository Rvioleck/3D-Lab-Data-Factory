package com.elwg.ai3dbackend.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 事件流服务接口
 * <p>
 * 管理活跃的SSE连接，提供创建连接、发送事件、处理连接关闭/超时的功能
 * </p>
 */
public interface EventStreamService {

    /**
     * 创建新的SSE连接
     *
     * @param taskId 任务ID
     * @return SseEmitter实例
     */
    SseEmitter createEmitter(String taskId);

    /**
     * 获取指定任务的SSE连接
     *
     * @param taskId 任务ID
     * @return SseEmitter实例，如果不存在则返回null
     */
    SseEmitter getEmitter(String taskId);

    /**
     * 移除指定任务的SSE连接
     *
     * @param taskId 任务ID
     */
    void removeEmitter(String taskId);

    /**
     * 发送状态更新事件
     *
     * @param taskId 任务ID
     * @param status 状态
     * @param error 错误信息（可选）
     * @return 是否发送成功
     */
    boolean sendStatusEvent(String taskId, String status, String error);

    /**
     * 发送结果可用事件
     *
     * @param taskId 任务ID
     * @param name 结果文件名
     * @param url 结果文件URL
     * @return 是否发送成功
     */
    boolean sendResultEvent(String taskId, String name, String url);

    /**
     * 发送自定义事件
     *
     * @param taskId 任务ID
     * @param eventName 事件名称
     * @param data 事件数据
     * @return 是否发送成功
     */
    boolean sendEvent(String taskId, String eventName, Object data);

    /**
     * 完成指定任务的SSE连接
     *
     * @param taskId 任务ID
     */
    void completeEmitter(String taskId);

    /**
     * 获取所有活跃的SSE连接
     *
     * @return 任务ID到SseEmitter的映射
     */
    Map<String, SseEmitter> getActiveEmitters();
}
