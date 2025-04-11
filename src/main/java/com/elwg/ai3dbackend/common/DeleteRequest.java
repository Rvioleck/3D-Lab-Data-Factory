package com.elwg.ai3dbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求
 * 用于接收删除请求的参数
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
}
