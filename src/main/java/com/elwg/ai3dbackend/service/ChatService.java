package com.elwg.ai3dbackend.service;

import com.elwg.ai3dbackend.model.entity.ChatSession;
import com.elwg.ai3dbackend.model.entity.ChatMessage;
import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天服务接口
 * <p>
 * 提供聊天相关的核心功能，包括会话管理、消息发送和接收等
 * </p>
 */
public interface ChatService {
    /**
     * 创建新的对话会话
     *
     * @param userId 用户ID
     * @param sessionName 会话名称
     * @return 创建的会话对象
     */
    ChatSession createSession(Long userId, String sessionName);

    /**
     * 获取用户的所有会话
     *
     * @param userId 用户ID
     * @return 用户的会话列表
     */
    List<ChatSession> listUserSessions(Long userId);

    /**
     * 获取会话的历史消息
     *
     * @param sessionId 会话ID
     * @return 会话的消息列表
     */
    List<ChatMessage> listSessionMessages(Long sessionId);

    /**
     * 发送消息并获取AI回复（支持自动创建会话）
     * <p>
     * 如果sessionId为null，则会自动创建一个新会话，并使用该会话发送消息。
     * 如果sessionId不为null，则使用指定的会话发送消息。
     * </p>
     *
     * @param sessionId 会话ID，如果为null则自动创建会话
     * @param userId 用户ID，当sessionId为null时必须提供
     * @param message 消息内容
     * @return AI回复消息对象
     * @throws IllegalArgumentException 当sessionId为null且userId也为null时抛出
     */
    ChatMessage sendMessage(Long sessionId, Long userId, String message);

    /**
     * 删除会话
     *
     * @param sessionId 要删除的会话ID
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteSession(Long sessionId);

    /**
     * 流式发送消息（支持自动创建会话）
     * <p>
     * 如果sessionId为null，则会自动创建一个新会话，并使用该会话流式发送消息。
     * 如果sessionId不为null，则使用指定的会话流式发送消息。
     * 响应会通过SSE（Server-Sent Events）方式实时返回。
     * </p>
     *
     * @param sessionId 会话ID，如果为null则自动创建会话
     * @param userId 用户ID，当sessionId为null时必须提供
     * @param message 消息内容
     * @param emitter SSE发射器，用于发送流式响应
     * @throws IllegalArgumentException 当sessionId为null且userId也为null时抛出
     */
    void streamMessage(Long sessionId, Long userId, String message, SseEmitter emitter);

    /**
     * 删除消息
     * <p>
     * 删除指定的消息，可以是用户消息或AI回复。
     * </p>
     *
     * @param messageId 消息ID
     * @param userId 用户ID（用于验证权限）
     * @return 删除成功返回true，否则返回false
     * @throws IllegalArgumentException 当消息不存在或用户无权限删除时抛出
     */
    boolean deleteMessage(Long messageId, Long userId);
}
