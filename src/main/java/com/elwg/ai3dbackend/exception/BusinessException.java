package com.elwg.ai3dbackend.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于封装业务逻辑异常，包含错误码和错误信息
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 创建业务异常
     *
     * @param code 自定义错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 通过错误码枚举创建业务异常
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 通过错误码枚举和自定义消息创建业务异常
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
