package com.elwg.ai3dbackend.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 抛出异常工具类
 * 用于在条件满足时抛出指定的异常
 */
@Slf4j
public class ThrowUtils {

    /**
     * 条件成立则抛出指定的运行时异常
     *
     * @param condition 判断条件
     * @param runtimeException 待抛出的异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            log.error("抛出异常: " + runtimeException.getMessage());
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛出包含错误码的业务异常
     *
     * @param condition 判断条件
     * @param errorCode 错误码枚举
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛出包含错误码和自定义错误信息的业务异常
     *
     * @param condition 判断条件
     * @param errorCode 错误码枚举
     * @param message 自定义错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
