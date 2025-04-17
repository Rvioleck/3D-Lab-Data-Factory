package com.elwg.ai3dbackend.model.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息模型
 * <p>
 * 用于表示从WebSocket服务器接收的JSON消息
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebSocketMessage {

    /**
     * 消息类型
     * 可能的值：status, result_part
     */
    @JsonProperty("type")
    private String type;

    /**
     * 状态（当type为status时）
     * 可能的值：processing, completed, failed
     */
    @JsonProperty("status")
    private String status;

    /**
     * 错误信息（当status为failed时）
     */
    @JsonProperty("error")
    private String error;

    /**
     * 结果部分名称（当type为result_part时）
     * 例如：pixel_images.png, xyz_images.png, output3d.zip
     */
    @JsonProperty("name")
    private String name;

    /**
     * 内容类型（当type为result_part时）
     * 例如：image/png, application/zip
     */
    @JsonProperty("content_type")
    private String contentType;
}
