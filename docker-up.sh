#!/bin/bash
# ShopMax Docker 快速启动脚本

set -e

echo "=========================================="
echo "  ShopMax 服务启动脚本"
echo "=========================================="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker 未运行，请先启动 Docker Desktop"
    exit 1
fi

# 创建数据目录
mkdir -p data/{mysql,redis,nacos,minio,elasticsearch,rocketmq,seata,sonarqube,xxl-job}

echo ""
echo "[1/6] 启动基础服务 (MySQL, Redis, Nacos)..."
docker-compose up -d shopmax-mysql shopmax-redis nacos
echo "等待 MySQL 就绪..."
sleep 10

echo ""
echo "[2/6] 启动 ELK 日志服务..."
docker-compose up -d shopmax-elasticsearch shopmax-logstash shopmax-kibana

echo ""
echo "[3/6] 启动 RocketMQ 消息队列..."
docker-compose up -d shopmax-rocketmq-namesrv shopmax-rocketmq-broker shopmax-rocketmq-console

echo ""
echo "[4/6] 启动 XXL-JOB 定时任务..."
docker-compose up -d shopmax-xxljob

echo ""
echo "[5/6] 启动 Seata 分布式事务..."
docker-compose up -d shopmax-seata

echo ""
echo "[6/6] 启动 SonarQube 代码质量..."
docker-compose up -d shopmax-sonarqube

echo ""
echo "=========================================="
echo "  所有服务启动完成！"
echo "=========================================="
echo ""
echo "服务访问地址："
echo "  Nacos:          http://localhost:8848/nacos (nacos/nacos)"
echo "  Sentinel:       http://localhost:8858 (sentinel/sentinel)"
echo "  MinIO:          http://localhost:9001 (minioadmin/minioadmin123)"
echo "  Kibana:         http://localhost:5601"
echo "  XXL-JOB:        http://localhost:8095 (admin/123456)"
echo "  RocketMQ:       http://localhost:18080"
echo "  Seata:          http://localhost:7091"
echo "  SonarQube:      http://localhost:9002 (admin/admin)"
echo ""
echo "验证服务状态："
echo "  docker-compose ps"
echo ""
echo "查看服务日志："
echo "  docker-compose logs -f [服务名]"
