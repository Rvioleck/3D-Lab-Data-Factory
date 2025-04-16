package com.elwg.ai3dbackend.service;

import com.elwg.ai3dbackend.model.entity.ChatSession;
import com.elwg.ai3dbackend.model.entity.ChatMessage;
import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {
    // 创建新的对话会话
    ChatSession createSession(Long userId, String sessionName);
    
    // 获取用户的所有会话
    List<ChatSession> listUserSessions(Long userId);
    
    // 获取会话的历史消息
    List<ChatMessage> listSessionMessages(Long sessionId);
    
    // 发送消息并获取AI回复
    ChatMessage sendMessage(Long sessionId, String message);
    
    // 删除会话
    boolean deleteSession(Long sessionId);
    
    void streamMessage(Long sessionId, String message, SseEmitter emitter);
}
