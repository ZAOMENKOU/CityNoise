# 前端服务Dockerfile
# 阶段1：构建应用
FROM node:20-alpine AS build

# 设置工作目录
WORKDIR /app

# 复制package.json和package-lock.json
COPY package.json pnpm-lock.yaml* ./

# 安装依赖
RUN npm install -g pnpm && pnpm install

# 复制源代码
COPY . .

# 构建应用
RUN pnpm run build

# 阶段2：运行应用
FROM nginx:alpine

# 复制构建产物到Nginx的静态文件目录
COPY --from=build /app/dist /usr/share/nginx/html

# 复制自定义Nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露80端口
EXPOSE 80

# 启动Nginx
CMD ["nginx", "-g", "daemon off;"]