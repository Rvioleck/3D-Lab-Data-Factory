package com.elwg.ai3dbackend.common;

import lombok.Data;
import com.elwg.ai3dbackend.exception.ErrorCode;

import java.io.Serializable;

/**
 * 通用响应类
 * 用于封装接口的统一响应格式
 * @param <T> 响应数据的类型
 */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 构造响应对象（无消息）
     *
     * @param code 响应状态码
     * @param data 响应数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 构造响应对象（完整参数）
     *
     * @param code 响应状态码
     * @param data 响应数据
     * @param message 响应消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 根据错误码构造响应对象
     *
     * @param errorCode 错误码枚举
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
