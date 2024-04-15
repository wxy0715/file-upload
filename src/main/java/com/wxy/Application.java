package com.wxy;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true,exposeProxy = true)
@EnableAsync
@EnableFileStorage
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}