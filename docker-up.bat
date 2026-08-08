@echo off
chcp 65001 >nul
echo ==========================================
echo   ShopMax 服务启动脚本
echo ==========================================

REM 创建数据目录
if not exist data\mysql mkdir data\mysql
if not exist data\redis mkdir data\redis
if not exist data\nacos mkdir data\nacos
if not exist data\elasticsearch mkdir data\elasticsearch
if not exist data\rocketmq mkdir data\rocketmq
if not exist data\seata mkdir data\seata
if not exist data\sonarqube mkdir data\sonarqube
if not exist data\xxl-job mkdir data\xxl-job

echo.
echo [1/6] 启动基础服务 (MySQL, Redis, Nacos)...
docker-compose up -d shopmax-mysql shopmax-redis nacos
echo 等待 MySQL 就绪...
timeout /t 15 /nobreak >nul

echo.
echo [2/6] 启动 ELK 日志服务...
docker-compose up -d shopmax-elasticsearch shopmax-logstash shopmax-kibana

echo.
echo [3/6] 启动 RocketMQ 消息队列...
docker-compose up -d shopmax-rocketmq-namesrv shopmax-rocketmq-broker shopmax-rocketmq-console

echo.
echo [4/6] 启动 XXL-JOB 定时任务...
docker-compose up -d shopmax-xxljob

echo.
echo [5/6] 启动 Seata 分布式事务...
docker-compose up -d shopmax-seata

echo.
echo [6/6] 启动 SonarQube 代码质量...
docker-compose up -d shopmax-sonarqube

echo.
echo ==========================================
echo   所有服务启动完成！
echo ==========================================
echo.
echo 服务访问地址：
echo   Nacos:          http://localhost:8848/nacos (nacos/nacos)
echo   Sentinel:       http://localhost:8858 (sentinel/sentinel)
echo   MinIO:          http://localhost:9001 (minioadmin/minioadmin123)
echo   Kibana:         http://localhost:5601
echo   XXL-JOB:        http://localhost:8095 (admin/123456)
echo   RocketMQ:       http://localhost:18080
echo   Seata:          http://localhost:7091
echo   SonarQube:      http://localhost:9002 (admin/admin)
echo.
echo 验证服务状态：
echo   docker-compose ps
