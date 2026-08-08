#!/bin/bash
# ============================================================
# ShopMax 阿里云 VPS 一键部署脚本
# 在新买的阿里云服务器上执行此脚本即可完成部署
# ============================================================
set -e

echo "========================================="
echo "  ShopMax VPS 部署脚本"
echo "========================================="

# 1. 安装 Docker
if ! command -v docker &>/dev/null; then
  echo ">>> 安装 Docker..."
  curl -fsSL https://get.docker.com | bash
  systemctl enable docker && systemctl start docker
fi

# 2. 安装 Docker Compose
if ! command -v docker &>/dev/null; then
  echo ">>> 安装 Docker Compose..."
  curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  chmod +x /usr/local/bin/docker-compose
fi

# 3. 克隆项目
if [ ! -d /opt/shopmax ]; then
  echo ">>> 克隆项目..."
  git clone git@github.com:kaitou982/ShopMax.git /opt/shopmax
else
  echo ">>> 项目已存在，拉取最新代码..."
  cd /opt/shopmax && git pull origin main
fi

cd /opt/shopmax

# 4. 配置环境变量
if [ ! -f .env ]; then
  echo ">>> 创建 .env 文件..."
  cp .env.example .env
  echo "请编辑 /opt/shopmax/.env 填入真实配置（数据库密码等）"
  echo "然后重新运行此脚本"
  exit 1
fi

# 5. 创建数据目录
mkdir -p data/{mysql,redis,nacos,minio}

# 6. 启动所有服务
echo ">>> 启动服务（首次构建可能需要 10-15 分钟）..."
docker compose -f docker-compose.prod.yml up -d --build

echo ""
echo "========================================="
echo " 部署完成！"
echo " API 地址: http://你的服务器IP"
echo " 管理后台: https://shop-max-mujl.vercel.app"
echo " Web 端:  https://shop-max-kr9t.vercel.app"
echo "========================================="
