# =============================================
# DN Quest — Makefile для быстрой разработки
# =============================================

.PHONY: help build build-service dev-up dev-down dev-restart logs status clean minio-console swagger

# Простые ANSI цвета (работают везде: Windows, Linux, macOS, WSL, Git Bash)
GREEN  := \033[32m
YELLOW := \033[33m
RESET  := \033[39m

help: ## Показать все команды
	@echo "=== DN Quest — команды разработки ==="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$    ' \( (MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf " $(GREEN)%-25s$(RESET) %s\n",     $$1, $$    2}'

build: ## Полная пересборка всех сервисов
	@echo "$(GREEN)Пересборка всего проекта... $(RESET)"
	./gradlew clean build -x test --parallel

build-service: ## Пересобрать один сервис: make build-service SERVICE=file-storage-service
	@echo "$(GREEN)Пересборка \( (SERVICE)... \)$(RESET)"
	./gradlew :\( (SERVICE):clean : \)(SERVICE):bootJar -x test

dev-up: ## Запустить dev-окружение (make dev-up SERVICE=xxx — только один)
	@echo "$(GREEN)Запуск dev-окружения... $(RESET)"
	docker compose -f docker-compose.dev.yml up -d --build $(SERVICE)

dev-down: ## Остановить и удалить всё
	@echo "$(YELLOW)Остановка всех сервисов... $(RESET)"
	docker compose -f docker-compose.dev.yml down -v --remove-orphans

dev-down-service: ## Остановить и удалить один сервис: make dev-down-service SERVICE=notification-service-dev
	@echo "$(YELLOW)Остановка сервиса $(SERVICE)... $(RESET)"
	docker compose -f docker-compose.dev.yml down -v $(SERVICE)

dev-restart: ## Полный перезапуск
	@$(MAKE) dev-down
	@$(MAKE) dev-up

dev-common:
	@echo "$(GREEN) Запускаем базовые: Postgres, Redis, Zookeeper, Kafka, Minio $(RESET)"
	docker compose -f docker-compose.dev.yml up -d postgres-dev redis-dev zookeeper-dev kafka-dev minio-dev

dev-notif:
	@echo "$(GREEN) Запускаем notification-service-dev $(RESET)"
	docker compose -f docker-compose.dev.yml up -d notification-service-dev

dev-ready:
	@echo "$(GREEN) Запускаем готовые сервисы $(RESET)"
	$(MAKE) dev-common
	docker compose -f docker-compose.dev.yml up -d authentication-service-dev user-management-service-dev file-storage-service-dev quest-management-service-dev team-management-service-dev game-engine-service-dev statistics-service-dev

dev-test:
	@echo "$(GREEN) Запускаем тест этап $(RESET)"
	$(MAKE) dev-ready
	$(MAKE) dev-notif

logs: ## Показать логи сервиса: make logs SERVICE=file-storage-service
	docker compose -f docker-compose.dev.yml logs -f $(SERVICE)

status: ## Статус всех контейнеров
	docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

clean: ## Полная очистка проекта
	@echo "$(YELLOW)Полная очистка... $(RESET)"
	./gradlew clean
	rm -rf */build/ .gradle/ build/
	docker compose -f docker-compose.dev.yml down -v --remove-orphans
	docker rmi     $$(docker images "dn-quest/*:dev" -q) 2>/dev/null || true
	@echo "$(GREEN)Очистка завершена! $(RESET)"

minio-console: ## Открыть MinIO консоль
	@echo "Открываем MinIO → http://localhost:9001"
	@open http://localhost:9001 2>/dev/null || xdg-open http://localhost:9001 2>/dev/null || echo "Открой вручную: http://localhost:9001"

swagger: ## Открыть Swagger UI
	@echo "Открываем Swagger → http://localhost:8080/swagger-ui.html"
	@open http://localhost:8080/swagger-ui.html 2>/dev/null || xdg-open http://localhost:8080/swagger-ui.html 2>/dev/null || echo "Открой вручную: http://localhost:8080/swagger-ui.html"
