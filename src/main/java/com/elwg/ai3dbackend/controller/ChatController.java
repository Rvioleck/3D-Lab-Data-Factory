package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.model.dto.ChatRequest;
import com.elwg.ai3dbackend.model.entity.ChatSession;
import com.elwg.ai3dbackend.model.entity.ChatMessage;
import com.elwg.ai3dbackend.service.ChatService;
import com.elwg.ai3dbackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/chat")
@Api(tags = "AI对话接口")
public class ChatController {

    @Resource
    private ChatService chatService;
    
    @Resource
    private UserService userService;

    @PostMapping("/session")
    @ApiOperation("创建会话")
    public BaseResponse<ChatSession> createSession(@RequestParam String sessionName, 
                                                 HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        ChatSession session = chatService.createSession(userId, sessionName);
        return ResultUtils.success(session);
    }

    @GetMapping("/session/list")
    @ApiOperation("获取会话列表")
    public BaseResponse<List<ChatSession>> listSessions(HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        List<ChatSession> sessions = chatService.listUserSessions(userId);
        return ResultUtils.success(sessions);
    }

    @GetMapping("/message/{sessionId}")
    @ApiOperation("获取会话消息历史")
    public BaseResponse<List<ChatMessage>> listMessages(@PathVariable Long sessionId) {
        List<ChatMessage> messages = chatService.listSessionMessages(sessionId);
        return ResultUtils.success(messages);
    }

    @PostMapping("/message")
    @ApiOperation("发送消息")
    public BaseResponse<ChatMessage> sendMessage(@RequestBody ChatRequest chatRequest) {
        ChatMessage response = chatService.sendMessage(
            chatRequest.getSessionId(), 
            chatRequest.getMessage()
        );
        return ResultUtils.success(response);
    }

    @DeleteMapping("/session/{sessionId}")
    @ApiOperation("删除会话")
    public BaseResponse<Boolean> deleteSession(@PathVariable Long sessionId) {
        boolean result = chatService.deleteSession(sessionId);
        return ResultUtils.success(result);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation("流式发送消息")
    public SseEmitter streamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        // 1. 验证用户登录状态
        Long userId = userService.getLoginUser(request).getId();
        // 2. 设置SSE
        SseEmitter emitter = new SseEmitter(180000L); // 3 minutes timeout
        // 3. 设置错误处理
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError((ex) -> emitter.completeWithError(ex));
        // 4. 调用服务
        chatService.streamMessage(chatRequest.getSessionId(), chatRequest.getMessage(), emitter);
        return emitter;
    }
}
