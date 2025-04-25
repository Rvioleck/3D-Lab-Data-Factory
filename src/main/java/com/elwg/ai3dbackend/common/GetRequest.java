package com.elwg.ai3dbackend.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用获取请求
 * 用于接收包含ID的获取请求参数
 */
@Data
@Schema(name = "GetRequest", description = "通用获取请求")
public class GetRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 实体id
     */
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890123456789")
    private Long id;
}
