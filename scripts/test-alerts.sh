#!/bin/bash

# Скрипт для тестирования алертов в мониторинговой системе DN Quest

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

# Загрузка переменных окружения
load_environment() {
    if [ -f ".env.monitoring" ]; then
        export $(cat .env.monitoring | grep -v '^#' | xargs)
    else
        log_warning "Файл .env.monitoring не найден, используем значения по умолчанию"
        export PROMETHEUS_PORT=9090
        export ALERTMANAGER_PORT=9093
        export GRAFANA_PORT=3000
    fi
}

# Проверка доступности сервисов
check_services() {
    log_info "Проверка доступности сервисов..."
    
    services=(
        "Prometheus:$PROMETHEUS_PORT"
        "AlertManager:$ALERTMANAGER_PORT"
    )
    
    for service in "${services[@]}"; do
        service_name=$(echo $service | cut -d':' -f1)
        port=$(echo $service | cut -d':' -f2)
        
        if curl -f "http://localhost:$port" &> /dev/null; then
            log_success "$service_name доступен"
        else
            log_error "$service_name недоступен на порту $port"
            exit 1
        fi
    done
}

# Тестирование алерта о недоступности сервиса
test_service_down_alert() {
    log_info "Тестирование алерта о недоступности сервиса..."
    
    # Создаем тестовый алерт через API Prometheus
    curl -X POST "http://localhost:$PROMETHEUS_PORT/api/v1/alerts" \
        -H 'Content-Type: application/json' \
        -d '{
            "alerts": [
                {
                    "labels": {
                        "alertname": "ServiceDown",
                        "severity": "critical",
                        "service": "test-service",
                        "instance": "localhost:8080"
                    },
                    "annotations": {
                        "summary": "Тестовый сервис недоступен",
                        "description": "Это тестовый алерт для проверки работы AlertManager"
                    }
                }
            ]
        }' || log_warning "Не удалось создать тестовый алерт"
    
    log_success "Тестовый алерт о недоступности сервиса отправлен"
}

# Тестирование алерта о высокой загрузке CPU
test_high_cpu_alert() {
    log_info "Тестирование алерта о высокой загрузке CPU..."
    
    # Отправляем метрику с высокой загрузкой CPU
    curl -X POST "http://localhost:$PROMETHEUS_PORT/api/v1/alerts" \
        -H 'Content-Type: application/json' \
        -d '{
            "alerts": [
                {
                    "labels": {
                        "alertname": "HighCPUUsage",
                        "severity": "warning",
                        "instance": "localhost:8080",
                        "job": "test-job"
                    },
                    "annotations": {
                        "summary": "Высокая загрузка CPU",
                        "description": "Тестовый алерт о высокой загрузке CPU (95%)"
                    }
                }
            ]
        }' || log_warning "Не удалось создать тестовый алерт"
    
    log_success "Тестовый алерт о высокой загрузке CPU отправлен"
}

# Тестирование алерта о нехватке памяти
test_memory_alert() {
    log_info "Тестирование алерта о нехватке памяти..."
    
    curl -X POST "http://localhost:$PROMETHEUS_PORT/api/v1/alerts" \
        -H 'Content-Type: application/json' \
        -d '{
            "alerts": [
                {
                    "labels": {
                        "alertname": "HighMemoryUsage",
                        "severity": "warning",
                        "instance": "localhost:8080",
                        "job": "test-job"
                    },
                    "annotations": {
                        "summary": "Высокое использование памяти",
                        "description": "Тестовый алерт о высоком использовании памяти (85%)"
                    }
                }
            ]
        }' || log_warning "Не удалось создать тестовый алерт"
    
    log_success "Тестовый алерт о нехватке памяти отправлен"
}

# Тестирование алерта о проблемах с базой данных
test_database_alert() {
    log_info "Тестирование алерта о проблемах с базой данных..."
    
    curl -X POST "http://localhost:$PROMETHEUS_PORT/api/v1/alerts" \
        -H 'Content-Type: application/json' \
        -d '{
            "alerts": [
                {
                    "labels": {
                        "alertname": "DatabaseConnectionFailure",
                        "severity": "critical",
                        "database": "postgresql",
                        "instance": "localhost:5432"
                    },
                    "annotations": {
                        "summary": "Проблемы с подключением к базе данных",
                        "description": "Тестовый алерт о проблемах с подключением к PostgreSQL"
                    }
                }
            ]
        }' || log_warning "Не удалось создать тестовый алерт"
    
    log_success "Тестовый алерт о проблемах с базой данных отправлен"
}

# Тестирование бизнес-алерта
test_business_alert() {
    log_info "Тестирование бизнес-алерта..."
    
    curl -X POST "http://localhost:$PROMETHEUS_PORT/api/v1/alerts" \
        -H 'Content-Type: application/json' \
        -d '{
            "alerts": [
                {
                    "labels": {
                        "alertname": "LowUserActivity",
                        "severity": "warning",
                        "metric": "user_activity_rate"
                    },
                    "annotations": {
                        "summary": "Низкая активность пользователей",
                        "description": "Тестовый алерт о низкой активности пользователей"
                    }
                }
            ]
        }' || log_warning "Не удалось создать тестовый алерт"
    
    log_success "Тестовый бизнес-алерт отправлен"
}

# Проверка статуса алертов в AlertManager
check_alert_status() {
    log_info "Проверка статуса алертов в AlertManager..."
    
    # Получаем список активных алертов
    alerts=$(curl -s "http://localhost:$ALERTMANAGER_PORT/api/v1/alerts" | jq -r '.data[] | .labels.alertname // "unknown"' 2>/dev/null || echo "error")
    
    if [ "$alerts" = "error" ]; then
        log_warning "Не удалось получить статус алертов из AlertManager"
    else
        log_info "Активные алерты в AlertManager:"
        if [ -n "$alerts" ]; then
            echo "$alerts" | while read alert; do
                echo "  - $alert"
            done
        else
            echo "  Нет активных алертов"
        fi
    fi
}

# Проверка конфигурации алертов
check_alert_rules() {
    log_info "Проверка конфигурации правил алертов..."
    
    # Получаем правила из Prometheus
    rules=$(curl -s "http://localhost:$PROMETHEUS_PORT/api/v1/rules" | jq -r '.data.groups[] | select(.name=="dn-quest-alerts") | .rules[] | .name // "unknown"' 2>/dev/null || echo "error")
    
    if [ "$rules" = "error" ]; then
        log_warning "Не удалось получить правила алертов из Prometheus"
    else
        log_success "Правила алертов DN Quest:"
        if [ -n "$rules" ]; then
            echo "$rules" | while read rule; do
                echo "  - $rule"
            done
        else
            echo "  Правила не найдены"
        fi
    fi
}

# Тестирование уведомлений
test_notifications() {
    log_info "Проверка конфигурации уведомлений..."
    
    # Получаем конфигурацию AlertManager
    config=$(curl -s "http://localhost:$ALERTMANAGER_PORT/api/v1/status" | jq -r '.data.config.raw' 2>/dev/null || echo "error")
    
    if [ "$config" = "error" ]; then
        log_warning "Не удалось получить конфигурацию AlertManager"
    else
        # Проверяем наличие конфигурации email
        if echo "$config" | grep -q "email_configs"; then
            log_success "Конфигурация email уведомлений найдена"
        else
            log_warning "Конфигурация email уведомлений не найдена"
        fi
        
        # Проверяем наличие конфигурации Slack
        if echo "$config" | grep -q "slack_configs"; then
            log_success "Конфигурация Slack уведомлений найдена"
        else
            log_warning "Конфигурация Slack уведомлений не найдена"
        fi
        
        # Проверяем наличие конфигурации Telegram
        if echo "$config" | grep -q "telegram_configs"; then
            log_success "Конфигурация Telegram уведомлений найдена"
        else
            log_warning "Конфигурация Telegram уведомлений не найдена"
        fi
    fi
}

# Очистка тестовых алертов
cleanup_test_alerts() {
    log_info "Очистка тестовых алертов..."
    
    # Удаляем тестовые алерты
    curl -X POST "http://localhost:$PROMETHEUS_PORT/api/v1/alerts" \
        -H 'Content-Type: application/json' \
        -d '{
            "alerts": []
        }' || log_warning "Не удалось очистить алерты"
    
    log_success "Тестовые алерты очищены"
}

# Отображение информации
show_info() {
    log_info "Информация о тестировании алертов:"
    echo
    echo "Проверьте следующие источники уведомлений:"
    echo "  Email: $ALERT_EMAIL_TO"
    echo "  Slack: Настроенный webhook"
    echo "  Telegram: Настроенный бот"
    echo
    echo "Web интерфейсы:"
    echo "  Prometheus: http://localhost:$PROMETHEUS_PORT/alerts"
    echo "  AlertManager: http://localhost:$ALERTMANAGER_PORT"
    echo "  Grafana: http://localhost:$GRAFANA_PORT/alerting"
    echo
}

# Основная функция
main() {
    local test_type=${1:-"all"}
    
    log_info "Начало тестирования алертов DN Quest"
    
    load_environment
    check_services
    
    case $test_type in
        "service")
            test_service_down_alert
            ;;
        "cpu")
            test_high_cpu_alert
            ;;
        "memory")
            test_memory_alert
            ;;
        "database")
            test_database_alert
            ;;
        "business")
            test_business_alert
            ;;
        "status")
            check_alert_status
            ;;
        "rules")
            check_alert_rules
            ;;
        "notifications")
            test_notifications
            ;;
        "cleanup")
            cleanup_test_alerts
            ;;
        "all")
            test_service_down_alert
            sleep 2
            test_high_cpu_alert
            sleep 2
            test_memory_alert
            sleep 2
            test_database_alert
            sleep 2
            test_business_alert
            sleep 5
            check_alert_status
            check_alert_rules
            test_notifications
            ;;
        *)
            log_error "Неизвестный тип теста: $test_type"
            echo "Доступные типы: service, cpu, memory, database, business, status, rules, notifications, cleanup, all"
            exit 1
            ;;
    esac
    
    show_info
    
    log_success "Тестирование алертов завершено!"
}

# Обработка сигналов
trap 'log_error "Скрипт прерван"; exit 1' INT TERM

# Запуск
main "$@"