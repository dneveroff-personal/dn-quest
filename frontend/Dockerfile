# Multi-stage build для frontend
FROM node:24-alpine AS base

# Устанавливаем рабочую директорию
WORKDIR /app

# Устанавливаем npm cache
ENV npm_config_cache=/root/.npm

# Копируем package файлы
COPY package*.json ./

# ---------- Stage 1: Dependencies ----------
FROM base AS deps

# Устанавливаем зависимости детерминированно
RUN npm ci --prefer-offline --no-audit --silent && npm cache clean --force

# ---------- Stage 2: Development ----------
FROM base AS development

# Копируем зависимости из deps stage
COPY --from=deps /app/node_modules ./node_modules

# Копируем исходный код
COPY . .

# Создаем пользователя
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nextjs -u 1001

# Переключаемся на пользователя
USER nextjs

EXPOSE 5173

# Запуск dev-сервера
CMD ["npm", "run", "dev", "--", "--host", "--port", "5173"]

# ---------- Stage 3: Build ----------
FROM base AS builder

# Копируем зависимости из deps stage
COPY --from=deps /app/node_modules ./node_modules

# Копируем исходный код
COPY . .

# Собираем приложение
RUN npm run build

# ---------- Stage 4: Production ----------
FROM nginx:alpine AS production

# Копируем кастомный конфиг nginx
COPY docker/nginx/nginx.conf /etc/nginx/nginx.conf
COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf

# Копируем собранные файлы из builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Создаем пользователя
RUN addgroup -g 1001 -S nginx && \
    adduser -S nginx -u 1001 -G nginx

# Устанавливаем правильные права
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d

# Создаем директорию для pid файла
RUN touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

# Переключаемся на пользователя
USER nginx

EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
