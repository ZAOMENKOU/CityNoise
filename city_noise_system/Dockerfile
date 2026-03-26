# 后端服务Dockerfile
# 使用官方OpenJDK 17镜像作为基础
FROM openjdk:17-jdk-alpine

# 维护者信息
LABEL maintainer="city_noise_system"

# 设置工作目录
WORKDIR /app

# 复制Maven构建文件
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 构建应用（使用maven wrapper）
RUN ./mvnw clean package -DskipTests

# 复制编译后的jar文件到容器中
COPY target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 创建上传文件目录
RUN mkdir -p /app/uploads

# 暴露8080端口
EXPOSE 8080

# 环境变量配置
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]