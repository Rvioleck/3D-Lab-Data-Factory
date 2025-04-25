package com.elwg.ai3dbackend.config;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * DeepSeek API HTTP客户端配置类
 * 提供OkHttpClient实例用于调用DeepSeek API
 */
@Configuration
public class DeepSeekHttpClientConfig {

    @Value("${deepseek.api.connect-timeout:30}")
    private int connectTimeout;

    @Value("${deepseek.api.read-timeout:30}")
    private int readTimeout;

    @Value("${deepseek.api.write-timeout:30}")
    private int writeTimeout;

    /**
     * 创建OkHttpClient实例
     * 用于调用DeepSeek API
     */
    @Bean(name = "deepSeekOkHttpClient")
    public OkHttpClient deepSeekOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .readTimeout(Duration.ofSeconds(readTimeout))
                .writeTimeout(Duration.ofSeconds(writeTimeout))
                // 启用HTTP/2和HTTP/1.1协议，按优先级排序
                .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS))
                .build();
    }
}
