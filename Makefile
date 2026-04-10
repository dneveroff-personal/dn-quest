# =============================================
# DN Quest — Makefile для быстрой разработки
# =============================================

.PHONY: help build build-service dev-up dev-down dev-restart logs logs-all status stats health clean minio-console swagger \
	test test-service dependencies check format flyway-migrate volumes networks prune psql redis-cli kafka-cli \
	dev-infra dev-up-service dev-db dev-redis inspect reload dev-all dev-stopped

# Простые ANSI цвета (работают везде: Windows, Linux, macOS, WSL, Git Bash)
GREEN  := \033[32m
YELLOW := \033[33m
BLUE   := \033[34m
RED    := \033[31m
RESET  := \033[39m

# Сервисы
INFRA_SERVICES := postgres-dev redis-dev zookeeper-dev kafka-dev minio-dev
ALL_SERVICES := api-gateway-dev authentication-service-dev user-management-service-dev quest-management-service-dev \
	game-engine-service-dev team-management-service-dev notification-service-dev statistics-service-dev file-storage-service-dev
FRONTEND_SERVICE := frontend-dev
ALL_MICROSERVICES := $(ALL_SERVICES) $(FRONTEND_SERVICE)

help: ## Показать все команды
	@echo "=== DN Quest — команды разработки ==="
	@echo ""
	@echo "$(GREEN)Сборка:$(RESET)"
	@echo "  build               - Полная пересборка всех сервисов"
	@echo "  build-service      - Пересобрать один сервис: make build-service SERVICE=file-storage-service"
	@echo "  test              - Запустить все тесты"
	@echo "  test-service      - Запустить тесты для одного сервиса: make test-service SERVICE=authentication-service"
	@echo "  dependencies     - Показать зависимости проекта"
	@echo "  check             - Проверить качество кода (lint, static analysis)"
	@echo "  format            - Форматировать код"
	@echo ""
	@echo "$(GREEN)Запуск (Docker):$(RESET)"
	@echo "  dev-up            - Запустить все микросервисы (SERVICE=xxx для одного)"
	@echo "  dev-infra         - Запустить только инфраструктуру (postgres, redis, kafka, minio)"
	@echo "  dev-all          - Запустить инфраструктуру + все микросервисы"
	@echo "  dev-down         - Остановить и удалить все контейнеры"
	@echo "  dev-restart      - Полный перезапуск (dev-down + dev-up)"
	@echo ""
	@echo "$(GREEN)Управление сервисами:$(RESET)"
	@echo "  dev-up-service  - Запустить один сервис: make dev-up-service SERVICE=auth-service-dev"
	@echo "  logs            - Логи сервиса: make logs SERVICE=authentication-service-dev"
	@echo "  logs-all        - Логи всех сервисов"
	@echo "  status          - Статус всех контейнеров"
	@echo "  stats           - Статистика использования ресурсов контейнерами"
	@echo "  health          - Проверить здоровье всех сервисов"
	@echo "  inspect         - Информация о контейнере: make inspect SERVICE=authentication-service-dev"
	@echo "  reload          - Перезагрузить сервис: make reload SERVICE=authentication-service-dev"
	@echo ""
	@echo "$(GREEN)Инфраструктура:$(RESET)"
	@echo "  dev-db          - По��ключиться к PostgreSQL: make dev-db DB=dnquest_auth"
	@echo "  dev-redis-cli   - Подключиться к Redis CLI"
	@echo "  kafka-cli      - Подключиться к Kafka container"
	@echo "  volumes        - Показать тома Docker"
	@echo "  networks       - Показать сети Docker"
	@echo "  prune          - Очистить неиспользуемые ресурсы Docker"
	@echo ""
	@echo "$(GREEN)Консоли:$(RESET)"
	@echo "  minio-console  - Открыть MinIO консоль (http://localhost:9001)"
	@echo "  swagger        - Открыть Swagger UI (http://localhost:8080/swagger-ui.html)"
	@echo "  pgadmin         - Открыть pgAdmin (http://localhost:5050)"
	@echo ""
	@echo "$(GREEN)Очистка:$(RESET)"
	@echo "  clean           - Полная очистка проекта (build + docker)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(RESET) %s\n", $$1, $$2}'

# =============================================
# Сборка
# =============================================

build: ## Полная пересборка всех сервисов
	@echo "$(GREEN)Пересборка всего проекта...$(RESET)"
	./gradlew clean build -x test --parallel

build-service: ## Пересобрать один сервис: make build-service SERVICE=file-storage-service
	@echo "$(GREEN)Пересборка $(SERVICE)...$(RESET)"
	./gradlew :$(SERVICE):clean :$(SERVICE):bootJar -x test

test: ## Запустить все тесты
	@echo "$(GREEN)Запуск тестов...$(RESET)"
	./gradlew test --parallel

test-service: ## Запустить тесты для одного сервиса: make test-service SERVICE=authentication-service
	@echo "$(GREEN)Запуск тестов для $(SERVICE)...$(RESET)"
	./gradlew :$(SERVICE):test

dependencies: ## Показать зависимости проекта
	./gradlew dependencies --configuration runtimeClasspath

check: ## Проверить качество кода (lint, static analysis)
	@echo "$(GREEN)Проверка качества кода...$(RESET)"
	./gradlew check

format: ## Форматировать код
	@echo "$(GREEN)Форматирование кода...$(RESET)"
	./gradlew spotlessApply

# =============================================
# Docker Compose — Основные команды
# =============================================

dev-up: ## Запустить все микросервисы (SERVICE=xxx для одного сервиса)
	@echo "$(GREEN)Запуск dev-окружения...$(RESET)"
	docker compose -f docker-compose.dev.yml up -d --build $(SERVICE)

dev-infra: ## Запустить только инфраструктуру
	@echo "$(GREEN)Запуск инфраструктуры...$(RESET)"
	docker compose -f docker-compose.dev.yml up -d --build $(INFRA_SERVICES)

dev-all: ## Запустить инфраструктуру + все микросервисы
	@echo "$(GREEN)Запуск полного окружения...$(RESET)"
	docker compose -f docker-compose.dev.yml up -d --build

dev-down: ## Остановить и удалить все контейнеры
	@echo "$(YELLOW)Остановка всех сервисов...$(RESET)"
	docker compose -f docker-compose.dev.yml down -v --remove-orphans

dev-restart: ## Полный перезапуск
	@$(MAKE) dev-down
	@$(MAKE) dev-all

dev-stopped: ## Показать остановленные контейнеры
	docker compose -f docker-compose.dev.yml ps -a

# =============================================
# Управлени�� се��висами
# =============================================

dev-up-service: ## Запустить один сервис: make dev-up-service SERVICE=authentication-service-dev
	@echo "$(GREEN)Запуск сервиса $(SERVICE)...$(RESET)"
	docker compose -f docker-compose.dev.yml up -d --build $(SERVICE)

dev-down-service: ## Остановить один сервис: make dev-down-service SERVICE=notification-service-dev
	@echo "$(YELLOW)Остановка сервиса $(SERVICE)...$(RESET)"
	docker compose -f docker-compose.dev.yml down -v $(SERVICE)

reload: ## Перезагрузить сервис: make reload SERVICE=authentication-service-dev
	@echo "$(BLUE)Перезагрузка сервиса $(SERVICE)...$(RESET)"
	docker compose -f docker-compose.dev.yml restart $(SERVICE)

logs: ## Логи сервиса: make logs SERVICE=authentication-service-dev
	docker compose -f docker-compose.dev.yml logs -f $(SERVICE)

logs-all: ## Логи всех микросервисов
	@echo "$(GREEN)Логи всех микросервисов (Ctrl+C для выхода)...$(RESET)"
	docker compose -f docker-compose.dev.yml logs -f $(ALL_SERVICES)

status: ## Статус всех контейнеров
	@docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

stats: ## Статистика использования ресурсов контейнерами
	@docker stats --no-stream

ps: ## Просто ps всех контейнеров (включая остановленные)
	@docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

health: ## Проверить здоровье всех сервисов
	@echo "$(GREEN)Проверка здоровья сервисов...$(RESET)"
	@$(MAKE) status | grep -v "NAMES" || true

inspect: ## Информация о контейнере: make inspect SERVICE=authentication-service-dev
	docker inspect dn-quest-$(SERVICE)

# =============================================
# Инфраструктура
# =============================================

dev-db: ## Подключиться к PostgreSQL: make dev-db DB=dnquest_auth
	@echo "$(GREEN)Подключение к PostgreSQL...$(RESET)"
	@echo "Use password: $(POSTGRES_PASSWORD)"
	docker exec -it dn-quest-postgres-dev psql -U $(POSTGRES_USER) -d $(DB)

dev-redis-cli: ## Подключиться к Redis CLI
	@echo "$(GREEN)Подключение к Redis CLI...$(RESET)"
	docker exec -it dn-quest-redis-dev redis-cli

kafka-cli: ## Подключиться к Kafka container
	@echo "$(GREEN)Подключение к Kafka container...$(RESET)"
	docker exec -it --entrypoint /bin/bash dn-quest-kafka-dev

volumes: ## Показать тома Docker
	docker volume ls | grep dn-quest

networks: ## Показать сети Docker
	@echo "$(GREEN)Сети в проекте:$(RESET)"
	docker network ls | grep dn-quest

prune: ## Очистить неиспользуемые ресурсы Docker
	@echo "$(YELLOW)Очистка неиспользуемых ресурсов...$(RESET)"
	docker system prune -af --volumes
	@echo "$(GREEN)Очистка завершена!$(RESET)"

# =============================================
# Консоли
# =============================================

minio-console: ## Открыть MinIO консоль
	@echo "Открываем MinIO → http://localhost:9001"
	@open http://localhost:9001 2>/dev/null || xdg-open http://localhost:9001 2>/dev/null || echo "Открой вручную: http://localhost:9001"

swagger: ## Открыть Swagger UI
	@echo "Открываем Swagger → http://localhost:8080/swagger-ui.html"
	@open http://localhost:8080/swagger-ui.html 2>/dev/null || xdg-open http://localhost:8080/swagger-ui.html 2>/dev/null || echo "Открой вручную: http://localhost:8080/swagger-ui.html"

pgadmin: ## Открыть pgAdmin
	@echo "Открываем pgAdmin → http://localhost:5050 (email: admin@dnquest.dev, password: dn)"
	@open http://localhost:5050 2>/dev/null || xdg-open http://localhost:5050 2>/dev/null || echo "Открой вручную: http://localhost:5050"

# =============================================
# Очистка
# =============================================

clean: ## Полная очистка проекта
	@echo "$(YELLOW)Полная очистка...$(RESET)"
	./gradlew clean
	rm -rf */build/ .gradle/ build/
	docker compose -f docker-compose.dev.yml down -v --remove-orphans
	docker rmi $$(docker images "dn-quest/*:dev" -q) 2>/dev/null || true
	@echo "$(GREEN)Очистка завершена!$(RESET)"
