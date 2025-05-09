package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.annotation.AuthCheck;
import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.model.dto.ChatRequest;
import com.elwg.ai3dbackend.model.entity.ChatSession;
import com.elwg.ai3dbackend.model.entity.ChatMessage;
import com.elwg.ai3dbackend.service.ChatService;
import com.elwg.ai3dbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ArrayList;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天控制器
 * <p>
 * 提供与AI对话相关的API接口，包括会话管理、消息发送和接收等功能。
 * 支持创建会话、获取会话列表、获取会话消息历史、发送消息、删除会话和消息等操作。
 * 同时提供流式消息接口，支持实时返回AI回复。
 * </p>
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "AI对话接口", description = "提供AI对话相关的所有功能，包括会话管理和消息处理")
@AuthCheck(mustRole = "admin") // 暂时只允许管理员访问聊天功能
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;

    /**
     * 创建新的对话会话
     * <p>
     * 根据提供的会话名称创建一个新的对话会话。会话与当前登录用户关联，
     * 用户可以在此会话中与AI进行多轮对话。创建成功后返回会话详细信息。
     * </p>
     *
     * @param sessionName 会话名称，用于标识和区分不同的对话会话
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 包含新创建会话信息的响应对象，包括会话ID、名称、创建时间等
     */
    @PostMapping("/session")
    @Operation(summary = "创建会话", description = "创建一个新的对话会话，需要提供会话名称，返回创建的会话详情")
    public BaseResponse<ChatSession> createSession(@RequestParam String sessionName,
                                                 HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        ChatSession session = chatService.createSession(userId, sessionName);
        return ResultUtils.success(session);
    }

    /**
     * 获取当前用户的所有会话列表
     * <p>
     * 获取当前登录用户创建的所有对话会话列表，按创建时间倒序排列（最新的会话排在前面）。
     * 每个会话包含会话ID、名称、创建时间等信息，可用于在用户界面上展示会话历史。
     * </p>
     *
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 包含用户会话列表的响应对象，列表可能为空（如果用户没有创建过会话）
     */
    @GetMapping("/session/list")
    @Operation(summary = "获取会话列表", description = "获取当前登录用户的所有对话会话，按创建时间倒序排列")
    public BaseResponse<List<ChatSession>> listSessions(HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        List<ChatSession> sessions = chatService.listUserSessions(userId);
        return ResultUtils.success(sessions);
    }

    /**
     * 获取指定会话的消息历史
     * <p>
     * 根据提供的会话ID，获取该会话下的所有历史消息，按时间顺序排列（最早的消息排在前面）。
     * 消息包括用户发送的内容和AI的回复，每条消息包含角色标识（user/assistant）、内容、发送时间等信息。
     * 如果会话ID无效，将返回空列表。
     * </p>
     *
     * @param sessionId 会话ID，用于标识要获取消息的会话
     * @return 包含会话消息历史的响应对象，按时间顺序排列的消息列表
     */
    @GetMapping("/message/{sessionId}")
    @Operation(summary = "获取会话消息历史", description = "获取指定会话的所有历史消息，按时间顺序排列")
    public BaseResponse<List<ChatMessage>> listMessages(@PathVariable String sessionId) {
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
     * 向AI发送消息并获取回复。该接口支持两种模式：
     * 1. 首次对话模式（first=true）：自动创建新会话并发送消息，此时sessionId可以不传。系统会根据消息内容自动生成会话名称。
     * 2. 继续对话模式（first=false）：在现有会话中继续对话，此时必须提供有效的sessionId。
     * </p>
     * <p>
     * 接口会同步返回AI的回复内容，同时在后台异步保存消息记录。如果需要实时流式返回AI回复，请使用/stream接口。
     * </p>
     *
     * @param chatRequest 聊天请求对象，包含会话ID、消息内容和是否首次消息的标记
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 包含AI回复消息的响应对象，包括消息ID、内容、角色标识等
     */
    @PostMapping("/message")
    @Operation(summary = "发送消息", description = "发送消息并获取AI回复，支持自动创建新会话或在现有会话中继续对话")
    public BaseResponse<ChatMessage> sendMessage(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        // 获取用户ID
        Long userId = userService.getLoginUser(request).getId();

        // 如果是首次消息，则sessionId传null，自动创建会话
        Long sessionId = null;
        if (!chatRequest.getFirst() && chatRequest.getSessionId() != null) {
            // 如果不是首次消息，并且提供了sessionId
            try {
                sessionId = Long.parseLong(chatRequest.getSessionId());
            } catch (NumberFormatException e) {
                // 如果转换失败，则使用null，会自动创建新会话
                sessionId = null;
            }
        }

        // 调用统一的发送消息方法
        ChatMessage response = chatService.sendMessage(sessionId, userId, chatRequest.getMessage());
        return ResultUtils.success(response);
    }

    /**
     * 删除指定的对话会话
     * <p>
     * 根据提供的会话ID删除对应的会话及其所有相关消息。删除操作是永久性的，无法恢复。
     * 如果会话ID无效或会话已被删除，将返回false。
     * </p>
     *
     * @param sessionId 要删除的会话ID，必须是有效的数字ID
     * @return 包含删除结果的响应对象，删除成功返回true，否则返回false
     */
    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定的对话会话及其所有消息，操作不可恢复")
    public BaseResponse<Boolean> deleteSession(@PathVariable String sessionId) {
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
     * 删除指定的消息及其相关联的消息。具体行为如下：
     * 1. 如果指定的是用户消息，则会同时删除该消息及其对应的AI回复。
     * 2. 如果指定的是AI回复消息，则会查找并删除对应的用户消息及该AI回复。
     * </p>
     * <p>
     * 删除操作会验证当前用户是否有权限删除该消息（即消息是否属于该用户）。
     * 删除操作是永久性的，无法恢复。
     * </p>
     *
     * @param messageId 要删除的消息ID，必须是有效的数字ID
     * @param request HTTP请求对象，用于获取当前登录用户信息（用于权限验证）
     * @return 包含删除结果的响应对象，删除成功返回true，否则返回false
     */
    @DeleteMapping("/message/{messageId}")
    @Operation(summary = "删除消息", description = "删除指定的消息及其关联消息，需要用户拥有该消息的权限")
    public BaseResponse<Boolean> deleteMessage(@PathVariable String messageId, HttpServletRequest request) {
        // 获取当前登录用户ID
        Long userId = userService.getLoginUser(request).getId();

        try {
            Long messageIdLong = Long.parseLong(messageId);
            boolean result = chatService.deleteMessage(messageIdLong, userId);
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
     * 向AI发送消息并通过流式方式（SSE）实时获取回复。该接口支持两种模式：
     * 1. 首次对话模式（first=true）：自动创建新会话并流式发送消息，此时sessionId可以不传。系统会根据消息内容自动生成会话名称。
     * 2. 继续对话模式（first=false）：在现有会话中继续对话，此时必须提供有效的sessionId。
     * </p>
     * <p>
     * 响应会通过SSE（Server-Sent Events）方式实时返回，客户端可以逐字接收AI的回复内容，提供更好的用户体验。
     * 完整的回复内容会在后台异步保存到数据库。流式响应完成后会发送特殊标记"[DONE]"表示结束。
     * </p>
     * <p>
     * 注意：该接口有3分钟的超时时间，如果在此期间未完成响应，连接会自动关闭。
     * </p>
     *
     * @param chatRequest 聊天请求对象，包含会话ID、消息内容和是否首次消息的标记
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return SSE发射器，用于发送流式响应，客户端需要正确处理SSE事件
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式发送消息", description = "流式发送消息并获取AI实时回复，支持自动创建会话，响应使用SSE格式，适合需要实时显示回复的场景")
    public SseEmitter streamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        // 1. 验证用户登录状态
        Long userId = userService.getLoginUser(request).getId();
        // 2. 设置SSE
        SseEmitter emitter = new SseEmitter(180000L); // 3 minutes timeout
        // 3. 设置错误处理
        emitter.onTimeout(emitter::complete);
        emitter.onError(emitter::completeWithError);
        // 4. 如果是首次消息，则sessionId传null，自动创建会话
        Long sessionId = null;
        if (!chatRequest.getFirst() && chatRequest.getSessionId() != null) {
            // 如果不是首次消息，并且提供了sessionId
            sessionId = Long.parseLong(chatRequest.getSessionId());
        }
        // 5. 调用统一的流式消息方法
        chatService.streamMessage(sessionId, userId, chatRequest.getMessage(), emitter);
        return emitter;
    }
}
