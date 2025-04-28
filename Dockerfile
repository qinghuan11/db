# 第一阶段：构建阶段
FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

# 复制 Maven 配置文件和源代码
COPY pom.xml .
COPY src ./src

# 打包项目（假设使用 Maven）
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

# 第二阶段：运行阶段
FROM openjdk:21-jdk-slim

WORKDIR /app

# 从构建阶段复制生成的 JAR 文件
COPY --from=builder /app/target/db-0.0.1-SNAPSHOT.jar app.jar

# 创建非 root 用户
RUN useradd -m appuser
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]