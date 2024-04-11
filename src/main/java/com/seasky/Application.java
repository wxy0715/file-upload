package com.seasky;

import com.seasky.core.config.DataSourceConfig;
import com.seasky.core.config.MyBatisConfig;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableDiscoveryClient
@EnableFileStorage
@ComponentScan(excludeFilters  = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {DataSourceConfig.class, MyBatisConfig.class})})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}