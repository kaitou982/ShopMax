package com.shop.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证中心启动类
 *
 * @author shop
 * @since 2026-04-19
 */
@MapperScan("com.shop.user.mapper")
@SpringBootApplication(
        // TODO: 移除 com.shop.user.service 扫描,改为 Feign 调用用户服务
        scanBasePackages = {"com.shop.auth", "com.shop.common.security", "com.shop.user.service", "com.shop.common"},
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
        }
)
@EnableDiscoveryClient
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
