package com.elwg.ai3dbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.elwg.ai3dbackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 可以运行时获取代理类
@SpringBootApplication
public class Ai3dBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ai3dBackendApplication.class, args);
    }

}


