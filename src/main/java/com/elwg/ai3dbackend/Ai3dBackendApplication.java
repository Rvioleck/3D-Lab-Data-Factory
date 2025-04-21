package com.elwg.ai3dbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAspectJAutoProxy(exposeProxy = true) // 可以运行时获取代理类
@EnableAsync  // 添加异步支持
@SpringBootApplication
public class Ai3dBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ai3dBackendApplication.class, args);
    }

}


