# Переменные
FRONT_DIR=frontend
BACK_DIR=src/main/resources
STATIC_DIR=$(BACK_DIR)/static
DOCKER_COMPOSE_FILE=docker-compose.yml
DOCKER_COMPOSE_DEV=docker-compose.dev.yml

# Node.js команда
NODE_CMD = cd $(FRONT_DIR) && . ~/.nvm/nvm.sh && nvm use 24 &&

# Цвета для вывода
RED=\033[0;31m
GREEN=\033[0;32m
YELLOW=\033[1;33m
NC=\033[0m # No Color

.PHONY: help clean build-static docker-upd build-back build-front all-clean all dev prod test logs

# Default target
help:
	@echo "$(YELLOW)DN Quest - Доступные команды:$(NC)"
	@echo ""
	@echo "$(GREEN)Разработка:$(NC)"
	@echo "  make dev          - Запуск в режиме разработки"
	@echo "  make logs         - Просмотр логов"
	@echo "  make test         - Запуск тестов"
	@echo ""
	@echo "$(GREEN)Сборка:$(NC)"
	@echo "  make build-static - Сборка фронтенда в статические файлы"
	@echo "  make build-back   - Сборка бэкенда"
	@echo "  make build-front  - Сборка фронтенда и запуск"
	@echo "  make all          - Полная сборка проекта"
	@echo ""
	@echo "$(GREEN)Production:$(NC)"
	@echo "  make prod         - Запуск в production режиме"
	@echo "  make deploy       - Деплой приложения"
	@echo ""
	@echo "$(GREEN)Очистка:$(NC)"
	@echo "  make clean        - Очистка Docker контейнеров"
	@echo "  make all-clean    - Полная очистка проекта"

# Очистка
clean:
	@echo "$(YELLOW)Очистка Docker контейнеров...$(NC)"
	docker compose down --remove-orphans
	@echo "$(YELLOW)Очистка node_modules...$(NC)"
	cd $(FRONT_DIR) && rm -rf node_modules package-lock.json
	@echo "$(GREEN)Очистка завершена$(NC)"

# Сборка фронтенда в статические файлы
build-static:
	@echo "$(YELLOW)Сборка фронтенда...$(NC)"
	$(NODE_CMD) npm ci --prefer-offline --no-audit --silent
	$(NODE_CMD) npm run build
	@echo "$(YELLOW)Копирование статических файлов...$(NC)"
	rm -rf $(STATIC_DIR)
	mkdir -p $(STATIC_DIR)
	cp -r $(FRONT_DIR)/dist/* $(STATIC_DIR)/
	@echo "$(GREEN)Фронтенд собран$(NC)"

# Сборка бэкенда
build-back:
	@echo "$(YELLOW)Сборка бэкенда...$(NC)"
	./gradlew clean build -x test
	@echo "$(GREEN)Бэкенд собран$(NC)"

# Запуск в режиме разработки
dev:
	@echo "$(YELLOW)Запуск в режиме разработки...$(NC)"
	DOCKERFILE=Dockerfile.dev SPRING_PROFILES=dev FRONTEND_TARGET=development docker compose -f $(DOCKER_COMPOSE_FILE) up -d --build
	@echo "$(GREEN)Приложение запущено в режиме разработки$(NC)"
	@echo "$(YELLOW)Frontend: http://localhost:5173$(NC)"
	@echo "$(YELLOW)Backend: http://localhost:8080$(NC)"
	@echo "$(YELLOW)Debug: localhost:5004$(NC)"

# Запуск в production режиме
prod:
	@echo "$(YELLOW)Запуск в production режиме...$(NC)"
	DOCKERFILE=Dockerfile SPRING_PROFILES=prod FRONTEND_TARGET=production docker compose -f $(DOCKER_COMPOSE_FILE) up -d --build
	@echo "$(GREEN)Приложение запущено в production режиме$(NC)"

# Быстрый перезапуск
docker-upd:
	@echo "$(YELLOW)Перезапуск контейнеров...$(NC)"
	docker compose down --remove-orphans
	docker compose up -d --build --remove-orphans

# Сборка фронтенда и запуск
build-front: build-static docker-upd

# Полная сборка проекта
all: build-static build-back
	@echo "$(GREEN)Проект полностью собран$(NC)"

# Полная очистка и сборка
all-clean: clean build-static build-back
	@echo "$(GREEN)Проект полностью очищен и собран$(NC)"

# Просмотр логов
logs:
	docker compose logs -f

# Логи конкретного сервиса
logs-backend:
	docker compose logs -f backend

logs-frontend:
	docker compose logs -f frontend

logs-db:
	docker compose logs -f db

# Запуск тестов
test:
	@echo "$(YELLOW)Запуск тестов...$(NC)"
	./gradlew test

# Деплой (заглушка для будущего CI/CD)
deploy:
	@echo "$(YELLOW)Деплой приложения...$(NC)"
	# Здесь будет логика деплоя
	@echo "$(GREEN)Деплой завершен$(NC)"

# Запуск с дополнительными сервисами
dev-full:
	@echo "$(YELLOW)Запуск с дополнительными сервисами...$(NC)"
	DOCKERFILE=Dockerfile.dev SPRING_PROFILES=dev FRONTEND_TARGET=development docker compose -f $(DOCKER_COMPOSE_FILE) --profile admin --profile cache --profile ai up -d --build
	@echo "$(GREEN)Приложение запущено с дополнительными сервисами$(NC)"
	@echo "$(YELLOW)PgAdmin: http://localhost:5050$(NC)"
	@echo "$(YELLOW)Redis: localhost:6379$(NC)"
	@echo "$(YELLOW)Qdrant: http://localhost:6333$(NC)"

# Проверка статуса
status:
	@echo "$(YELLOW)Статус контейнеров:$(NC)"
	docker compose ps

# Остановка
stop:
	@echo "$(YELLOW)Остановка контейнеров...$(NC)"
	docker compose stop
	@echo "$(GREEN)Контейнеры остановлены$(NC)"
