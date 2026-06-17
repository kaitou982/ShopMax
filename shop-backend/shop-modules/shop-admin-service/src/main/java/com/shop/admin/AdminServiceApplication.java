package com.shop.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 后台管理服务启动类
 *
 * @author shop
 * @since 2026-05-25
 */
@SpringBootApplication(scanBasePackages = {"com.shop.admin", "com.shop.common", "com.shop.payment", "com.shop.community"})
@MapperScan("com.shop.*.mapper")
@EnableDiscoveryClient
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
