# ==========================================
# 1. Build Stage
# ==========================================
FROM node:20-alpine AS builder
WORKDIR /app

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

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制打包后的 dist 文件
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
