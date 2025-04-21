package com.elwg.ai3dbackend.exception;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 * 用于统一处理项目中抛出的各类异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("业务异常: " + e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理运行时异常
     *
     * @param e 运行时异常
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("运行时异常: " + e.getMessage(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 处理数据库完整性约束异常
     * 主要用于处理唯一索引冲突等数据库约束问题
     *
     * @param e 数据库完整性约束异常
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public BaseResponse<?> sqlIntegrityConstraintViolationExceptionHandler(SQLIntegrityConstraintViolationException e) {
        log.error("数据库约束异常: " + e.getMessage(), e);
        String errorMessage = e.getMessage();

        // 处理唯一索引冲突
        if (errorMessage.contains("Duplicate entry")) {
            // 提取重复的值
            String duplicateValue = errorMessage.split("Duplicate entry")[1].split("for key")[0].trim();
            duplicateValue = duplicateValue.replace("'", "");

            // 根据索引名称返回不同的错误信息
            if (errorMessage.contains("idx_userAccount")) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), "账号 " + duplicateValue + " 已存在，请更换其他账号");
            }

            // 可以根据需要添加其他索引的处理

            // 默认的唯一索引冲突错误
            return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), "数据已存在: " + duplicateValue);
        }

        // 其他数据库约束错误
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), "数据验证失败，请检查输入");
    }

    /**
     * 处理所有其他异常
     *
     * @param e 异常
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> exceptionHandler(Exception e) {
        log.error("系统异常: " + e.getMessage(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }
}
