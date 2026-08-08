package com.shop.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * 用户服务启动类
 *
 * @author shop
 * @since 2026-04-15w
 */
@SpringBootApplication(
        scanBasePackages = {"com.shop.user", "com.shop.common"}
)
@MapperScan("com.shop.user.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.shop.common.feign.client")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
