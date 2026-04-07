#!/bin/bash

# Скрипт для развертывания мониторинговой инфраструктуры DN Quest
# Включает Prometheus, Grafana, AlertManager, ELK stack, Jaeger

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функции для вывода
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Проверка зависимостей
check_dependencies() {
    log_info "Проверка зависимостей..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker не установлен. Пожалуйста, установите Docker."
        exit 1
    fi
    
    if ! command -v docker compose &> /dev/null; then
        log_error "Docker Compose не установлен. Пожалуйста, установите Docker Compose."
        exit 1
    fi
    
    log_success "Все зависимости установлены"
}

# Создание необходимых директорий
create_directories() {
    log_info "Создание директорий для мониторинга..."
    
    directories=(
        "logs/prometheus"
        "logs/grafana"
        "logs/alertmanager"
        "logs/elasticsearch"
        "logs/logstash"
        "logs/kibana"
        "logs/jaeger"
        "data/prometheus"
        "data/grafana"
        "data/alertmanager"
        "data/elasticsearch"
        "data/jaeger"
    )
    
    for dir in "${directories[@]}"; do
        mkdir -p "$dir"
        chmod 755 "$dir"
    done
    
    log_success "Директории созданы"
}

# Настройка переменных окружения
setup_environment() {
    log_info "Настройка переменных окружения..."
    
    # Создаем .env файл для мониторинга если не существует
    if [ ! -f ".env.monitoring" ]; then
        cat > .env.monitoring << EOF
# Переменные окружения для мониторинга DN Quest
COMPOSE_PROJECT_NAME=dn-quest-monitoring

# Версии сервисов
PROMETHEUS_VERSION=latest
GRAFANA_VERSION=latest
ALERTMANAGER_VERSION=latest
ELASTICSEARCH_VERSION=8.11.0
LOGSTASH_VERSION=8.11.0
KIBANA_VERSION=8.11.0
JAEGER_VERSION=latest

# Порты
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
ALERTMANAGER_PORT=9093
ELASTICSEARCH_PORT=9200
KIBANA_PORT=5601
JAEGER_UI_PORT=16686
JAEGER_COLLECTOR_PORT=14268

# Настройки безопасности
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin123

# Настройки алертов
ALERT_EMAIL_SMTP_HOST=smtp.gmail.com
ALERT_EMAIL_SMTP_PORT=587
ALERT_EMAIL_FROM=alerts@dn-quest.com
ALERT_EMAIL_TO=admin@dn-quest.com

# Настройки Slack
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK

# Настройки Telegram
TELEGRAM_BOT_TOKEN=YOUR_BOT_TOKEN
TELEGRAM_CHAT_ID=YOUR_CHAT_ID

# Окружение
ENVIRONMENT=development
EOF
        log_success "Создан файл .env.monitoring"
    fi
    
    # Загружаем переменные окружения
    export $(cat .env.monitoring | grep -v '^#' | xargs)
}

# Резервное копирование текущих конфигураций
backup_configurations() {
    log_info "Резервное копирование текущих конфигураций..."
    
    backup_dir="backups/monitoring-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$backup_dir"
    
    # Копируем конфигурационные файлы
    if [ -d "docker" ]; then
        cp -r docker "$backup_dir/"
    fi
    
    if [ -f ".env.monitoring" ]; then
        cp .env.monitoring "$backup_dir/"
    fi
    
    log_success "Конфигурации сохранены в $backup_dir"
}

# Развертывание мониторинга
deploy_monitoring() {
    log_info "Развертывание мониторинговой инфраструктуры..."
    
    # Останавливаем предыдущие контейнеры если есть
    docker compose -f docker-compose.monitoring.yml down --remove-orphans || true
    
    # Запускаем мониторинг
    docker compose -f docker-compose.monitoring.yml up -d
    
    log_success "Мониторинговая инфраструктура развернута"
}

# Проверка здоровья сервисов
check_health() {
    log_info "Проверка здоровья сервисов..."
    
    services=(
        "prometheus:$PROMETHEUS_PORT"
        "grafana:$GRAFANA_PORT"
        "alertmanager:$ALERTMANAGER_PORT"
        "elasticsearch:$ELASTICSEARCH_PORT"
        "kibana:$KIBANA_PORT"
        "jaeger:$JAEGER_UI_PORT"
    )
    
    for service in "${services[@]}"; do
        service_name=$(echo $service | cut -d':' -f1)
        port=$(echo $service | cut -d':' -f2)
        
        log_info "Проверка $service_name на порту $port..."
        
        max_attempts=30
        attempt=1
        
        while [ $attempt -le $max_attempts ]; do
            if curl -f "http://localhost:$port" &> /dev/null; then
                log_success "$service_name готов"
                break
            fi
            
            if [ $attempt -eq $max_attempts ]; then
                log_warning "$service_name не отвечает после $max_attempts попыток"
            fi
            
            sleep 2
            ((attempt++))
        done
    done
}

# Настройка Grafana
setup_grafana() {
    log_info "Настройка Grafana..."
    
    # Ждем запуска Grafana
    sleep 10
    
    # Импортируем дашборды через API
    grafana_url="http://localhost:$GRAFANA_PORT"
    
    # Аутентификация
    auth="$GRAFANA_ADMIN_USER:$GRAFANA_ADMIN_PASSWORD"
    
    # Импорт дашбордов
    dashboards=(
        "docker/grafana/dashboards/dn-quest-system-overview.json"
        "docker/grafana/dashboards/dn-quest-business-metrics.json"
        "docker/grafana/dashboards/dn-quest-security-metrics.json"
    )
    
    for dashboard in "${dashboards[@]}"; do
        if [ -f "$dashboard" ]; then
            log_info "Импорт дашборда: $dashboard"
            curl -X POST \
                -u "$auth" \
                -H "Content-Type: application/json" \
                -d @"$dashboard" \
                "$grafana_url/api/dashboards/db" || log_warning "Не удалось импортировать дашборд: $dashboard"
        fi
    done
    
    log_success "Настройка Grafana завершена"
}

# Создание индексных шаблонов в Elasticsearch
setup_elasticsearch() {
    log_info "Настройка Elasticsearch..."
    
    # Ждем запуска Elasticsearch
    sleep 20
    
    es_url="http://localhost:$ELASTICSEARCH_PORT"
    
    # Создаем индексный шаблон для логов DN Quest
    curl -X PUT "$es_url/_index_template/dn-quest-logs" \
        -H 'Content-Type: application/json' \
        -d '{
            "index_patterns": ["dn-quest-logs-*"],
            "template": {
                "settings": {
                    "number_of_shards": 1,
                    "number_of_replicas": 0,
                    "index.lifecycle.name": "dn-quest-logs-policy",
                    "index.lifecycle.rollover_alias": "dn-quest-logs"
                },
                "mappings": {
                    "properties": {
                        "@timestamp": {"type": "date"},
                        "level": {"type": "keyword"},
                        "logger": {"type": "keyword"},
                        "thread": {"type": "keyword"},
                        "message": {"type": "text"},
                        "application": {"type": "keyword"},
                        "environment": {"type": "keyword"},
                        "trace_id": {"type": "keyword"},
                        "span_id": {"type": "keyword"},
                        "exception_class": {"type": "keyword"},
                        "exception_message": {"type": "text"},
                        "stack_trace": {"type": "text"}
                    }
                }
            }
        }' || log_warning "Не удалось создать индексный шаблон"
    
    log_success "Настройка Elasticsearch завершена"
}

# Отображение информации о доступе
show_access_info() {
    log_info "Мониторинговая инфраструктура DN Quest развернута!"
    echo
    echo "Доступ к сервисам:"
    echo "  Prometheus: http://localhost:$PROMETHEUS_PORT"
    echo "  Grafana: http://localhost:$GRAFANA_PORT (admin/$GRAFANA_ADMIN_PASSWORD)"
    echo "  AlertManager: http://localhost:$ALERTMANAGER_PORT"
    echo "  Elasticsearch: http://localhost:$ELASTICSEARCH_PORT"
    echo "  Kibana: http://localhost:$KIBANA_PORT"
    echo "  Jaeger UI: http://localhost:$JAEGER_UI_PORT"
    echo
    echo "Полезные команды:"
    echo "  Проверка статуса: docker compose -f docker-compose.monitoring.yml ps"
    echo "  Просмотр логов: docker compose -f docker-compose.monitoring.yml logs -f [service]"
    echo "  Перезапуск: docker compose -f docker-compose.monitoring.yml restart [service]"
    echo "  Остановка: docker compose -f docker-compose.monitoring.yml down"
    echo
}

# Основная функция
main() {
    log_info "Начало развертывания мониторинговой инфраструктуры DN Quest"
    
    check_dependencies
    create_directories
    setup_environment
    backup_configurations
    deploy_monitoring
    check_health
    setup_grafana
    setup_elasticsearch
    show_access_info
    
    log_success "Развертывание мониторинговой инфраструктуры завершено!"
}

# Обработка сигналов
trap 'log_error "Скрипт прерван"; exit 1' INT TERM

# Запуск
main "$@"