package com.elwg.ai3dbackend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 聊天请求对象
 * <p>
 * 用于发送消息和流式消息的请求参数
 * </p>
 */
@Data
@ApiModel(value = "ChatRequest", description = "聊天请求对象")
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
    @ApiModelProperty(value = "会话ID", notes = "当first=true时可以不传，支持数字ID或临时ID格式", example = "1234567890123456789")
    private String sessionId;

    /**
     * 消息内容
     */
    @ApiModelProperty(value = "消息内容", required = true, example = "你好，请问有什么可以帮助你的？")
    private String message;

    /**
     * 是否是首次消息
     * <p>
     * true表示自动创建会话，false表示使用已有会话
     * </p>
     */
    @ApiModelProperty(value = "是否是首次消息", notes = "true表示自动创建会话，false表示使用已有会话", example = "false", required = true)
    private Boolean first = false;
}