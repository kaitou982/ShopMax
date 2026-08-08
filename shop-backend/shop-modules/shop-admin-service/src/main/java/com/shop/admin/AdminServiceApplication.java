package com.shop.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 后台管理服务启动类（已解耦：不再扫描 payment/community 包）
 *
 * @author shop
 * @since 2026-05-25
 */
@SpringBootApplication(scanBasePackages = {"com.shop.admin", "com.shop.common"})
@MapperScan({"com.shop.admin.mapper"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.shop.common.feign.client")
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
