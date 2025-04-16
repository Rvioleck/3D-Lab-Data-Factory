package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.model.dto.ChatRequest;
import com.elwg.ai3dbackend.model.entity.ChatSession;
import com.elwg.ai3dbackend.model.entity.ChatMessage;
import com.elwg.ai3dbackend.service.ChatService;
import com.elwg.ai3dbackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ArrayList;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天控制器
 * <p>
 * 提供与AI对话相关的API接口，包括会话管理、消息发送和接收等功能
 * </p>
 */
@RestController
@RequestMapping("/chat")
@Api(tags = "AI对话接口")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;

    /**
     * 创建新的对话会话
     *
     * @param sessionName 会话名称
     * @param request HTTP请求对象，用于获取当前登录用户
     * @return 包含新创建会话信息的响应对象
     */
    @PostMapping("/session")
    @ApiOperation(value = "创建会话", notes = "创建一个新的对话会话，需要提供会话名称")
    @ApiImplicitParam(name = "sessionName", value = "会话名称", required = true, dataType = "String", paramType = "query")
    public BaseResponse<ChatSession> createSession(@RequestParam String sessionName,
                                                 HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        ChatSession session = chatService.createSession(userId, sessionName);
        return ResultUtils.success(session);
    }

    /**
     * 获取当前用户的所有会话列表
     *
     * @param request HTTP请求对象，用于获取当前登录用户
     * @return 包含用户会话列表的响应对象
     */
    @GetMapping("/session/list")
    @ApiOperation(value = "获取会话列表", notes = "获取当前登录用户的所有对话会话")
    public BaseResponse<List<ChatSession>> listSessions(HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        List<ChatSession> sessions = chatService.listUserSessions(userId);
        return ResultUtils.success(sessions);
    }

    /**
     * 获取指定会话的消息历史
     *
     * @param sessionId 会话ID
     * @return 包含会话消息历史的响应对象
     */
    @GetMapping("/message/{sessionId}")
    @ApiOperation(value = "获取会话消息历史", notes = "获取指定会话的所有历史消息")
    @ApiImplicitParam(name = "sessionId", value = "会话ID", required = true, dataType = "String", paramType = "path")
    public BaseResponse<List<ChatMessage>> listMessages(@PathVariable String sessionId) {
        // 判断是否是临时会话ID（以temp-开头）
        if (sessionId.startsWith("temp-")) {
            // 如果是临时会话ID，返回空列表
            return ResultUtils.success(new ArrayList<>());
        }

        try {
            Long sessionIdLong = Long.parseLong(sessionId);
            List<ChatMessage> messages = chatService.listSessionMessages(sessionIdLong);
            return ResultUtils.success(messages);
        } catch (NumberFormatException e) {
            // 如果转换失败，返回空列表
            return ResultUtils.success(new ArrayList<>());
        }
    }

    /**
     * 发送消息并获取AI回复（支持自动创建会话）
     * <p>
     * 当first=true时，会自动创建新会话并发送消息，此时sessionId可以不传。
     * 当first=false时，使用指定的会话发送消息，此时必须提供sessionId。
     * </p>
     *
     * @param chatRequest 聊天请求对象，包含会话ID、消息内容和是否首次消息的标记
     * @param request HTTP请求对象，用于获取当前登录用户
     * @return 包含AI回复消息的响应对象
     */
    @PostMapping("/message")
    @ApiOperation(value = "发送消息", notes = "发送消息并获取AI回复，支持创建新会话")
    public BaseResponse<ChatMessage> sendMessage(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        // 获取用户ID
        Long userId = userService.getLoginUser(request).getId();

        // 如果是首次消息，则sessionId传null，自动创建会话
        Long sessionId = null;
        if (!chatRequest.getFirst() && chatRequest.getSessionId() != null) {
            // 如果不是首次消息，并且提供了sessionId
            String sessionIdStr = chatRequest.getSessionId();
            // 判断是否是临时会话ID（以temp-开头）
            if (!sessionIdStr.startsWith("temp-")) {
                try {
                    sessionId = Long.parseLong(sessionIdStr);
                } catch (NumberFormatException e) {
                    // 如果转换失败，则使用null，会自动创建新会话
                    sessionId = null;
                }
            }
        }

        // 调用统一的发送消息方法
        ChatMessage response = chatService.sendMessage(sessionId, userId, chatRequest.getMessage());
        return ResultUtils.success(response);
    }

    /**
     * 删除指定的对话会话
     *
     * @param sessionId 要删除的会话ID
     * @return 包含删除结果的响应对象，删除成功返回true，否则返回false
     */
    @DeleteMapping("/session/{sessionId}")
    @ApiOperation(value = "删除会话", notes = "删除指定的对话会话及其所有消息")
    @ApiImplicitParam(name = "sessionId", value = "会话ID", required = true, dataType = "String", paramType = "path")
    public BaseResponse<Boolean> deleteSession(@PathVariable String sessionId) {
        // 判断是否是临时会话ID（以temp-开头）
        if (sessionId.startsWith("temp-")) {
            // 如果是临时会话ID，直接返回成功
            return ResultUtils.success(true);
        }

        try {
            Long sessionIdLong = Long.parseLong(sessionId);
            boolean result = chatService.deleteSession(sessionIdLong);
            return ResultUtils.success(result);
        } catch (NumberFormatException e) {
            // 如果转换失败，返回失败
            return ResultUtils.success(false);
        }
    }

    /**
     * 删除消息对（用户消息和对应的AI回复）
     * <p>
     * 删除指定的用户消息及其对应的AI回复。
     * 如果指定的是AI回复消息，则会查找并删除对应的用户消息。
     * </p>
     *
     * @param messageId 要删除的消息ID
     * @param request HTTP请求对象，用于获取当前登录用户
     * @return 包含删除结果的响应对象，删除成功返回true，否则返回false
     */
    @DeleteMapping("/message/{messageId}")
    @ApiOperation(value = "删除消息", notes = "删除指定的消息对（用户消息和对应的AI回复）")
    @ApiImplicitParam(name = "messageId", value = "消息ID", required = true, dataType = "String", paramType = "path")
    public BaseResponse<Boolean> deleteMessage(@PathVariable String messageId, HttpServletRequest request) {
        // 获取当前登录用户ID
        Long userId = userService.getLoginUser(request).getId();

        try {
            Long messageIdLong = Long.parseLong(messageId);
            boolean result = chatService.deleteMessagePair(messageIdLong, userId);
            return ResultUtils.success(result);
        } catch (NumberFormatException e) {
            // 如果转换失败，返回失败
            return ResultUtils.success(false);
        } catch (IllegalArgumentException e) {
            // 如果消息不存在或用户无权限删除，返回失败
            return ResultUtils.success(false);
        }
    }

    /**
     * 流式发送消息并获取AI回复（支持自动创建会话）
     * <p>
     * 当first=true时，会自动创建新会话并流式发送消息，此时sessionId可以不传。
     * 当first=false时，使用指定的会话流式发送消息，此时必须提供sessionId。
     * 响应会通过SSE（Server-Sent Events）方式实时返回。
     * </p>
     *
     * @param chatRequest 聊天请求对象，包含会话ID、消息内容和是否首次消息的标记
     * @param request HTTP请求对象，用于获取当前登录用户
     * @return SSE发射器，用于发送流式响应
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation(value = "流式发送消息", notes = "流式发送消息并获取AI实时回复，支持自动创建会话，响应使用SSE格式")
    public SseEmitter streamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        // 1. 验证用户登录状态
        Long userId = userService.getLoginUser(request).getId();
        // 2. 设置SSE
        SseEmitter emitter = new SseEmitter(180000L); // 3 minutes timeout
        // 3. 设置错误处理
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError((ex) -> emitter.completeWithError(ex));
        // 4. 如果是首次消息，则sessionId传null，自动创建会话
        Long sessionId = null;
        if (!chatRequest.getFirst() && chatRequest.getSessionId() != null) {
            // 如果不是首次消息，并且提供了sessionId
            String sessionIdStr = chatRequest.getSessionId();
            // 判断是否是临时会话ID（以temp-开头）
            if (!sessionIdStr.startsWith("temp-")) {
                try {
                    sessionId = Long.parseLong(sessionIdStr);
                } catch (NumberFormatException e) {
                    // 如果转换失败，则使用null，会自动创建新会话
                    sessionId = null;
                }
            }
        }
        // 5. 调用统一的流式消息方法
        chatService.streamMessage(sessionId, userId, chatRequest.getMessage(), emitter);
        return emitter;
    }
}
