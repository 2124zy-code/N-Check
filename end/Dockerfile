# ==========================================
# 1. Build Stage
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# 缓存依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# 编译源码并打包
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# 2. Runtime Stage
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/ncheck-backend-1.0.0.jar app.jar

# JVM 内存调优参数 (限制在 384MB 堆内存以内，防止 Railway 容器 OOM)
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseG1GC -Dfile.encoding=UTF-8"

# 动态将 Railway 注入的 $PORT 传递给 Spring Boot
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
