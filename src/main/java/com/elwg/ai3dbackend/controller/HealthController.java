package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于监控应用程序的健康状态
 */
@RestController
@RequestMapping("/health")
@Api(tags = "健康检查接口")
public class HealthController {

    @Autowired
    private Environment environment;

    /**
     * 简单健康检查
     *
     * @return 健康状态
     */
    @GetMapping
    @ApiOperation("简单健康检查")
    public BaseResponse<String> health() {
        return ResultUtils.success("OK");
    }

    /**
     * 详细健康检查
     * 返回应用程序的详细信息
     *
     * @return 应用程序详细信息
     */
    @GetMapping("/detail")
    @ApiOperation("详细健康检查")
    public BaseResponse<Map<String, Object>> healthDetail() {
        Map<String, Object> details = new HashMap<>();
        
        // 应用信息
        details.put("appName", environment.getProperty("spring.application.name"));
        details.put("port", environment.getProperty("server.port"));
        details.put("contextPath", environment.getProperty("server.servlet.context-path"));
        
        // JVM信息
        Runtime runtime = Runtime.getRuntime();
        details.put("jvmTotalMemory", runtime.totalMemory() / 1024 / 1024 + "MB");
        details.put("jvmFreeMemory", runtime.freeMemory() / 1024 / 1024 + "MB");
        details.put("jvmMaxMemory", runtime.maxMemory() / 1024 / 1024 + "MB");
        details.put("availableProcessors", runtime.availableProcessors());
        
        // 系统信息
        details.put("javaVersion", System.getProperty("java.version"));
        details.put("osName", System.getProperty("os.name"));
        details.put("osArch", System.getProperty("os.arch"));
        details.put("osVersion", System.getProperty("os.version"));
        
        // 时间信息
        details.put("serverTime", System.currentTimeMillis());
        
        return ResultUtils.success(details);
    }
}
