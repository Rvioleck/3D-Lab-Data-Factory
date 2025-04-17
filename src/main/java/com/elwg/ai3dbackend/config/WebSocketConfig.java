package com.elwg.ai3dbackend.config;

import com.elwg.ai3dbackend.service.WebSocketService;
import com.elwg.ai3dbackend.service.impl.OkHttpWebSocketServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * WebSocket配置类
 * <p>
 * 配置WebSocket服务的实现类
 * </p>
 */
@Configuration
public class WebSocketConfig {

    /**
     * 配置WebSocket服务实现
     * 使用OkHttp实现的WebSocket客户端
     *
     * @return WebSocketService实现
     */
    @Bean
    @Primary
    public WebSocketService webSocketService() {
        return new OkHttpWebSocketServiceImpl();
    }
}
