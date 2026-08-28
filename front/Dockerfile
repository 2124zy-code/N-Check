# ==========================================
# 1. Build Stage
# ==========================================
FROM node:20-alpine AS builder
WORKDIR /app

# 接收构建参数 VITE_API_BASE_URL (可选)
ARG VITE_API_BASE_URL=""
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# 安装依赖
COPY package.json package-lock.json ./
RUN npm ci

# 构建生产产物
COPY . .
RUN npm run build

# ==========================================
# 2. Production Runtime Stage
# ==========================================
FROM nginx:alpine

# 复制自定义 Nginx 模板
COPY nginx.conf /etc/nginx/templates/default.conf.template

# 复制打包后的 dist 静态文件
COPY --from=builder /app/dist /usr/share/nginx/html

# Railway 默认端口环境变量
ENV PORT=80

EXPOSE 80

# 启动时使用 envsubst 将 ${PORT} 替换到 Nginx 配置中
CMD ["/bin/sh", "-c", "envsubst '${PORT}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
