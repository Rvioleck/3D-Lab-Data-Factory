package com.elwg.ai3dbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.elwg.ai3dbackend.service.ChatService;
import com.elwg.ai3dbackend.service.DeepSeekService;
import com.elwg.ai3dbackend.mapper.ChatSessionMapper;
import com.elwg.ai3dbackend.mapper.ChatMessageMapper;
import com.elwg.ai3dbackend.model.entity.ChatSession;
import com.elwg.ai3dbackend.model.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private DeepSeekService deepSeekService;

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
        queryWrapper.eq("userId", userId);
        return chatSessionMapper.selectList(queryWrapper);
    }

    @Override
    public List<ChatMessage> listSessionMessages(Long sessionId) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sessionId", sessionId) ;
        return chatMessageMapper.selectList(queryWrapper);
    }

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

    @Override
    public ChatMessage sendMessage(Long sessionId, String message) {
        // 1. 保存用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(message);
        chatMessageMapper.insert(userMessage);

        // 2. 获取历史消息
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sessionId", sessionId)
                   .orderByAsc("createTime");
        List<ChatMessage> historyMessages = chatMessageMapper.selectList(queryWrapper);

        // 3. 构建对话历史
        List<Map<String, String>> messages = new ArrayList<>();
        for (ChatMessage historyMessage : historyMessages) {
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("role", historyMessage.getRole());
            messageMap.put("content", historyMessage.getContent());
            messages.add(messageMap);
        }

        // 4. 调用 DeepSeek API
        String aiResponse = deepSeekService.chatCompletion(messages);

        // 5. 异步保存AI回复
        saveAiMessage(sessionId, aiResponse);

        // 6. 立即返回响应
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setSessionId(sessionId);
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        return aiMessage;
    }

    @Override
    public boolean deleteSession(Long sessionId) {
        return chatSessionMapper.deleteById(sessionId) > 0;
    }

    @Override
    public void streamMessage(Long sessionId, String message, SseEmitter emitter) {
        try {
            // 1. 保存用户消息
            ChatMessage userMessage = new ChatMessage();
            userMessage.setSessionId(sessionId);
            userMessage.setRole("user");
            userMessage.setContent(message);
            chatMessageMapper.insert(userMessage);

            // 2. 获取历史消息并限制长度
            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sessionId", sessionId)
                       .orderByDesc("createTime")
                       .last("LIMIT 20");
            List<ChatMessage> historyMessages = chatMessageMapper.selectList(queryWrapper);
            Collections.reverse(historyMessages);

            // 3. 构建对话历史
            List<Map<String, String>> messages = new ArrayList<>();
            for (ChatMessage historyMessage : historyMessages) {
                Map<String, String> messageMap = new HashMap<>();
                messageMap.put("role", historyMessage.getRole());
                messageMap.put("content", historyMessage.getContent());
                messages.add(messageMap);
            }

            // 4. 创建StringBuilder来收集完整响应
            StringBuilder fullResponse = new StringBuilder();

            // 5. 调用流式API并处理响应
            deepSeekService.streamChatCompletion(messages, new SseEmitter() {
                @Override
                public void send(Object data) throws IOException {
                    String chunk = data.toString();
                    fullResponse.append(chunk);
                    // 直接发送纯文本数据，不使用event()包装
                    emitter.send(chunk);
                }

                @Override
                public void complete() {
                    try {
                        saveAiMessage(sessionId, fullResponse.toString());
                        // 发送结束标记
                        emitter.send("[DONE]");
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("Error completing stream for session: {}", sessionId, e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void completeWithError(Throwable ex) {
                    log.error("Stream error for session: {}", sessionId, ex);
                    emitter.completeWithError(ex);
                }
            });
        } catch (Exception e) {
            log.error("Error in stream message processing", e);
            emitter.completeWithError(e);
        }
    }
}
