package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异常测试控制器
 * 用于测试全局异常处理器的功能
 */
@RestController
@RequestMapping("/exception")
public class ExceptionTestController {

    /**
     * 测试业务异常处理
     *
     * @return 正常情况下不会返回任何内容，会抛出业务异常
     */
    @GetMapping("/business")
    public BaseResponse<String> testBusinessException() {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "测试业务异常");
    }

    /**
     * 测试运行时异常处理
     *
     * @return 正常情况下不会返回任何内容，会抛出运行时异常
     */
    @GetMapping("/runtime")
    public BaseResponse<String> testRuntimeException() {
        throw new RuntimeException("测试运行时异常");
    }

    /**
     * 测试普通异常处理
     *
     * @return 正常情况下不会返回任何内容，会抛出普通异常
     */
    @GetMapping("/common")
    public BaseResponse<String> testCommonException() throws Exception {
        throw new Exception("测试普通异常");
    }

    /**
     * 测试正常返回
     *
     * @return 正常返回的响应对象
     */
    @GetMapping("/normal")
    public BaseResponse<String> testNormal() {
        return ResultUtils.success("正常返回测试");
    }
}
