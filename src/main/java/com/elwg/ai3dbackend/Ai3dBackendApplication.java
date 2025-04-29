package com.elwg.ai3dbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true) // 添加运行时获取代理类支持
@EnableAsync  // 添加异步支持
@EnableScheduling // 添加定时任务支持
public class Ai3dBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ai3dBackendApplication.class, args);
    }

}


