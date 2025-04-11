package com.elwg.ai3dbackend.common;

import com.elwg.ai3dbackend.exception.ErrorCode;

/**
 * 响应工具类
 * 用于生成各种类型的响应对象
 */
public class ResultUtils {

    /**
     * 成功响应
     *
     * @param <T> 数据类型
     * @return 不带数据的成功响应
     */
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), null, ErrorCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 带数据的成功响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, ErrorCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应（带数据和自定义消息）
     *
     * @param data    响应数据
     * @param message 自定义响应消息
     * @param <T>     数据类型
     * @return 带数据和自定义消息的成功响应
     */
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, message);
    }

    /**
     * 失败响应（使用错误码枚举）
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return 包含错误码和错误信息的失败响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败响应（使用错误码枚举和自定义错误信息）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     * @param <T>       数据类型
     * @return 包含错误码和自定义错误信息的失败响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

    /**
     * 失败响应（使用自定义错误码和错误信息）
     *
     * @param code    自定义错误码
     * @param message 自定义错误信息
     * @param <T>     数据类型
     * @return 包含自定义错误码和错误信息的失败响应
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }
}
