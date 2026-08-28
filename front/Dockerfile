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
# 2. Production Stage (Node.js High Performance Runner)
# ==========================================
FROM node:20-alpine
WORKDIR /app

# 全局安装静态服务器 serve
RUN npm install -g serve

# 复制打包后的 dist 产物与配置文件
COPY --from=builder /app/dist ./dist
COPY serve.json ./serve.json

ENV PORT=80
EXPOSE 80

# 动态绑定 Railway 注入的端口并使用 serve.json
CMD ["sh", "-c", "serve -c serve.json -l ${PORT:-80}"]
