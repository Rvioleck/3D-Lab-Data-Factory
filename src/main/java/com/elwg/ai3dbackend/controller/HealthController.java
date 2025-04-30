package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于监控应用程序的健康状态和运行情况。提供简单和详细两种健康检查接口，
 * 可用于服务健康监控、负载均衡健康检查、运行状态监控等场景。
 */
@RestController
@RequestMapping("/health")
@Tag(name = "健康检查接口", description = "提供应用程序健康状态和运行情况监控")
public class HealthController {

    @Autowired
    private Environment environment;

    /**
     * 简单健康检查
     * <p>
     * 提供简单的健康检查接口，返回"OK"表示服务正常运行。
     * 该接口可用于负载均衡器的健康检查、服务可用性监控等场景。
     * 响应快速，不包含详细信息，适合频繁调用。
     * </p>
     *
     * @return 包含健康状态的响应对象，正常时返回"OK"
     */
    @GetMapping
    @Operation(summary = "简单健康检查", description = "返回基本的健康状态，可用于负载均衡器检查")

    public BaseResponse<String> health() {
        return ResultUtils.success("OK");
    }

    /**
     * 详细健康检查
     * <p>
     * 提供详细的健康检查接口，返回应用程序的详细运行信息。
     * 返回的信息包括：
     * 1. 应用信息：应用名称、端口、上下文路径等
     * 2. JVM信息：总内存、空闲内存、最大内存、可用处理器数等
     * 3. 系统信息：Java版本、操作系统名称、架构、版本等
     * 4. 时间信息：服务器当前时间
     * </p>
     * <p>
     * 该接口适用于运维监控、故障诊断和系统状态分析等场景。
     * </p>
     *
     * @return 包含应用程序详细信息的响应对象
     */
    @GetMapping("/detail")
    @Operation(summary = "详细健康检查", description = "返回应用程序的详细运行信息，包括应用信息、JVM信息、系统信息等")
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
