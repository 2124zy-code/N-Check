# ==========================================
# 1. Build Stage
# ==========================================
FROM node:20-alpine AS builder
WORKDIR /app

# 安装依赖
COPY package.json package-lock.json ./
RUN npm ci

# 编译 Vue 3 生产产物
COPY . .
RUN npm run build

# ==========================================
# 2. Lightweight Cloud Runtime Stage
# ==========================================
FROM node:20-alpine
WORKDIR /app

# 安装高性能轻量静态托管服务器 serve (完美支持 SPA 路由与云平台动态 PORT)
RUN npm install -g serve

# 复制打包好的 dist 产物
COPY --from=builder /app/dist ./dist

ENV PORT=80
EXPOSE 80

# 动态绑定 Railway 注入的端口并开启 SPA 单页重定向 (-s)
CMD ["sh", "-c", "serve -s dist -l ${PORT:-80}"]
