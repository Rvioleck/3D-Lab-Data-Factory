package com.elwg.ai3dbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.elwg.ai3dbackend.service.ChatService;
import com.elwg.ai3dbackend.service.DeepSeekService;
import com.elwg.ai3dbackend.mapper.ChatSessionMapper;
import com.elwg.ai3dbackend.mapper.ChatMessageMapper;
import com.elwg.ai3dbackend.model.entity.ChatSession;
import com.elwg.ai3dbackend.model.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天服务实现类
 * 提供会话管理和消息处理功能
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private DeepSeekService deepSeekService;

    @Resource
    private TransactionTemplate transactionTemplate;

    // ==================== 会话管理相关方法 ====================

    @Override
    public ChatSession createSession(Long userId, String sessionName) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setSessionName(sessionName);
        chatSessionMapper.insert(session);
        return session;
    }

    @Override
    public List<ChatSession> listUserSessions(Long userId) {
        QueryWrapper<ChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                   .eq("isDelete", 0)
                   .orderByDesc("createTime");
        return chatSessionMapper.selectList(queryWrapper);
    }

    @Override
    public boolean deleteSession(Long sessionId) {
        return chatSessionMapper.deleteById(sessionId) > 0;
    }

    @Override
    public List<ChatMessage> listSessionMessages(Long sessionId) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sessionId", sessionId)
                   .eq("isDelete", 0)
                   .orderByAsc("createTime");
        return chatMessageMapper.selectList(queryWrapper);
    }

    // ==================== 消息处理相关方法 ====================

    @Override
    public ChatMessage sendMessage(Long sessionId, Long userId, String message) {
        // 在事务外部定义最终会话ID
        final Long[] finalSessionId = new Long[1];

        // 使用TransactionTemplate进行事务管理
        transactionTemplate.execute(status -> {
            try {
                // 0. 如果没有提供sessionId，则自动创建会话
                if (sessionId == null) {
                    if (userId == null) {
                        throw new IllegalArgumentException("当sessionId为null时，必须提供userId");
                    }
                    // 从消息内容生成会话名称
                    String sessionName = generateSessionName(message);
                    // 创建新会话
                    ChatSession session = createSession(userId, sessionName); // 数据库插入操作
                    finalSessionId[0] = session.getId();
                } else {
                    finalSessionId[0] = sessionId;
                }

                // 1. 保存用户消息
                ChatMessage userMessage = new ChatMessage();
                userMessage.setSessionId(finalSessionId[0]);
                userMessage.setRole("user");
                userMessage.setContent(message);
                chatMessageMapper.insert(userMessage); // 数据库插入操作

                return null; // 事务执行结果，这里不需要返回值
            } catch (Exception e) {
                log.error("Transaction failed in sendMessage", e);
                status.setRollbackOnly();
                throw e;
            }
        });

        // 2. 获取历史消息 - 事务外部操作
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sessionId", finalSessionId[0])
                   .eq("isDelete", 0)
                   .orderByAsc("createTime");
        List<ChatMessage> historyMessages = chatMessageMapper.selectList(queryWrapper);

        // 3. 构建对话历史
        List<Map<String, String>> messages = getHistory(historyMessages);

        // 4. 调用 DeepSeek API - 耗时操作，在事务外部执行
        String aiResponse = deepSeekService.chatCompletion(messages);

        // 5. 异步保存AI回复
        saveAiMessage(finalSessionId[0], aiResponse);

        // 6. 立即返回响应
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setSessionId(finalSessionId[0]);
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        return aiMessage;
    }

    @Override
    public void streamMessage(Long sessionId, Long userId, String message, SseEmitter emitter) {
        try {
            // 在事务外部定义最终会话ID
            final Long[] finalSessionId = new Long[1];

            // 使用TransactionTemplate进行事务管理
            transactionTemplate.execute(status -> {
                try {
                    // 如果没有提供sessionId，则自动创建会话
                    if (sessionId == null) {
                        if (userId == null) {
                            throw new IllegalArgumentException("当sessionId为null时，必须提供userId");
                        }
                        // 从消息内容生成会话名称
                        String sessionName = generateSessionName(message);
                        // 创建新会话
                        ChatSession session = createSession(userId, sessionName);
                        finalSessionId[0] = session.getId();
                    } else {
                        finalSessionId[0] = sessionId;
                    }

                    // 1. 保存用户消息
                    ChatMessage userMessage = new ChatMessage();
                    userMessage.setSessionId(finalSessionId[0]);
                    userMessage.setRole("user");
                    userMessage.setContent(message);
                    chatMessageMapper.insert(userMessage);

                    return null; // 事务执行结果，这里不需要返回值
                } catch (Exception e) {
                    log.error("Transaction failed in streamMessage", e);
                    status.setRollbackOnly();
                    throw e;
                }
            });

            // 2. 获取历史消息并限制长度 - 事务外部操作
            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sessionId", finalSessionId[0])
                       .eq("isDelete", 0)
                       .orderByDesc("createTime")
                       .last("LIMIT 20");
            List<ChatMessage> historyMessages = chatMessageMapper.selectList(queryWrapper);
            Collections.reverse(historyMessages); // 反转为时间正序

            // 3. 构建对话历史
            List<Map<String, String>> messages = getHistory(historyMessages);

            // 4. 创建StringBuilder来收集完整响应
            StringBuilder fullResponse = new StringBuilder();

            // 5. 调用流式API并处理响应 - 耗时操作，在事务外部执行
            deepSeekService.streamChatCompletion(messages, new SseEmitter() {
                @Override
                public void send(@NotNull Object data) throws IOException {
                    String chunk = data.toString();
                    fullResponse.append(chunk);
                    emitter.send(chunk);
                }

                @Override
                public void complete() {
                    try {
                        saveAiMessage(finalSessionId[0], fullResponse.toString());
                        // 发送结束标记
                        emitter.send("[DONE]");
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("Error completing stream for session: {}", finalSessionId[0], e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void completeWithError(@NotNull Throwable ex) {
                    log.error("Stream error for session: {}", finalSessionId[0], ex);
                    emitter.completeWithError(ex);
                }
            });
        } catch (Exception e) {
            log.error("Error in stream message processing", e);
            emitter.completeWithError(e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 异步保存AI回复消息
     * 使用独立的线程池执行，不阻塞主线程
     *
     * @param sessionId 会话ID
     * @param aiResponse AI响应内容
     */
    @Async("chatMessageExecutor")
    protected void saveAiMessage(Long sessionId, String aiResponse) {
        try {
            ChatMessage aiMessage = new ChatMessage();
            aiMessage.setSessionId(sessionId);
            aiMessage.setRole("assistant");
            aiMessage.setContent(aiResponse);
            chatMessageMapper.insert(aiMessage);
            log.info("AI response saved successfully for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Failed to save AI response for session: {}", sessionId, e);
        }
    }

    /**
     * 将消息历史转换为DeepSeek API所需的格式
     *
     * @param historyMessages 消息历史列表
     * @return 格式化后的消息列表
     */
    private List<Map<String, String>> getHistory(List<ChatMessage> historyMessages) {
        List<Map<String, String>> messages = new ArrayList<>();
        for (ChatMessage historyMessage : historyMessages) {
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("role", historyMessage.getRole());
            messageMap.put("content", historyMessage.getContent());
            messages.add(messageMap);
        }
        return messages;
    }

    /**
     * 从消息内容生成会话名称
     * 取消息前20个字符，如果超过长度则添加省略号
     *
     * @param message 消息内容
     * @return 生成的会话名称
     */
    private String generateSessionName(String message) {
        if (message == null || message.isEmpty()) {
            return "新对话";
        }

        // 去除换行符和多余空格
        String cleanMessage = message.replaceAll("\\s+", " ").trim();

        // 取前20个字符
        int maxLength = 20;
        if (cleanMessage.length() <= maxLength) {
            return cleanMessage;
        } else {
            return cleanMessage.substring(0, maxLength) + "...";
        }
    }

    @Override
    public boolean deleteMessage(Long messageId, Long userId) {
        // 1. 查询消息是否存在
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null || message.getIsDelete() == 1) {
            throw new IllegalArgumentException("消息不存在");
        }

        // 2. 验证消息所属的会话是否属于当前用户
        ChatSession session = chatSessionMapper.selectById(message.getSessionId());
        if (session == null || !session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此消息");
        }

        // 3. 直接删除指定消息
        return chatMessageMapper.deleteById(messageId) > 0;
    }
}
