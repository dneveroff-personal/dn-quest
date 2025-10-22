# ---------- Stage 1: Build ----------
FROM node:24-alpine AS builder

WORKDIR /app

# Настройка npm cache для надежности сети
ENV npm_config_cache=/root/.npm

# Копируем package.json и package-lock.json
COPY package*.json ./

# Устанавливаем зависимости детерминированно (npm ci)
# --prefer-offline ускоряет сборку и снижает влияние сети
RUN npm ci --prefer-offline --no-audit --silent

# Копируем весь исходный код
COPY . .

# Собираем фронтенд (production build)
RUN npm run build

# ---------- Stage 2: Runtime (Vite dev-сервер) ----------
FROM node:24-alpine

WORKDIR /app

# Копируем все из билд-стадии
COPY --from=builder /app /app

# Настройка npm cache
ENV npm_config_cache=/root/.npm

EXPOSE 5173

# Запуск dev-сервера
CMD ["npm", "run", "dev", "--", "--host"]

USER nobody
