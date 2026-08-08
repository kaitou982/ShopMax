# 多阶段构建
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 先复制 pom 文件，利用 Docker 缓存层
COPY pom.xml .
COPY shop-common/pom.xml shop-common/
COPY shop-common/shop-common-core/pom.xml shop-common/shop-common-core/
COPY shop-common/shop-common-web/pom.xml shop-common/shop-common-web/
COPY shop-common/shop-common-redis/pom.xml shop-common/shop-common-redis/
COPY shop-common/shop-common-security/pom.xml shop-common/shop-common-security/
COPY shop-common/shop-common-storage/pom.xml shop-common/shop-common-storage/
COPY shop-common/shop-common-feign/pom.xml shop-common/shop-common-feign/
COPY shop-gateway/pom.xml shop-gateway/
COPY shop-auth/pom.xml shop-auth/
COPY shop-modules/pom.xml shop-modules/
COPY shop-modules/shop-user-service/pom.xml shop-modules/shop-user-service/
COPY shop-modules/shop-product-service/pom.xml shop-modules/shop-product-service/
COPY shop-modules/shop-order-service/pom.xml shop-modules/shop-order-service/
COPY shop-modules/shop-payment-service/pom.xml shop-modules/shop-payment-service/
COPY shop-modules/shop-admin-service/pom.xml shop-modules/shop-admin-service/
COPY shop-modules/shop-customer-service/pom.xml shop-modules/shop-customer-service/
COPY shop-modules/shop-marketing-service/pom.xml shop-modules/shop-marketing-service/
COPY shop-modules/shop-community-service/pom.xml shop-modules/shop-community-service/
COPY shop-modules/shop-live-service/pom.xml shop-modules/shop-live-service/
COPY shop-modules/shop-file-service/pom.xml shop-modules/shop-file-service/

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY . .
ARG MODULE=shop-gateway
RUN mvn package -pl ${MODULE} -am -DskipTests -B

# 运行阶段
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 添加时区支持
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 添加 curl 用于健康检查
RUN apk add --no-cache curl

# JVM 优化参数
ENV JAVA_OPTS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+UseG1GC \
  -XX:+UseStringDeduplication \
  -Djava.security.egd=file:/dev/./urandom"

ARG MODULE=shop-gateway
COPY --from=builder /app/${MODULE}/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
