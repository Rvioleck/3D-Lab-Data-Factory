package com.elwg.ai3dbackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 聊天请求对象
 * <p>
 * 用于发送消息和流式消息的请求参数
 * </p>
 */
@Data
@Schema(name = "ChatRequest", description = "聊天请求对象")
public class ChatRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     * <p>
     * 当first=true时可以不传，会自动创建新会话
     * </p>
     */
    @Schema(description = "会话ID", example = "1234567890123456789")
    private String sessionId;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，请问有什么可以帮助你的？")
    private String message;

    /**
     * 是否是首次消息
     * <p>
     * true表示自动创建会话，false表示使用已有会话
     * </p>
     */
    @Schema(description = "是否是首次消息", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean first = false;
}