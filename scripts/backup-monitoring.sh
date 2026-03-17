#!/bin/bash

# Скрипт для резервного копирования конфигураций и данных мониторинга DN Quest

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
        export COMPOSE_PROJECT_NAME=dn-quest-monitoring
    fi
}

# Создание директории для бэкапов
create_backup_directory() {
    local backup_dir="backups/monitoring-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$backup_dir"
    echo "$backup_dir"
}

# Резервное копирование конфигурационных файлов
backup_configurations() {
    local backup_dir=$1
    
    log_info "Резервное копирование конфигурационных файлов..."
    
    # Создаем поддиректории
    mkdir -p "$backup_dir/config/docker"
    mkdir -p "$backup_dir/config/grafana"
    mkdir -p "$backup_dir/config/prometheus"
    mkdir -p "$backup_dir/config/alertmanager"
    mkdir -p "$backup_dir/config/elasticsearch"
    mkdir -p "$backup_dir/config/logstash"
    mkdir -p "$backup_dir/config/kibana"
    mkdir -p "$backup_dir/config/jaeger"
    
    # Копируем конфигурации Docker
    if [ -f "docker-compose.monitoring.yml" ]; then
        cp docker-compose.monitoring.yml "$backup_dir/config/"
    fi
    
    if [ -d "docker" ]; then
        cp -r docker/* "$backup_dir/config/docker/"
    fi
    
    # Копируем переменные окружения
    if [ -f ".env.monitoring" ]; then
        cp .env.monitoring "$backup_dir/config/"
    fi
    
    # Копируем скрипты
    if [ -d "scripts" ]; then
        cp -r scripts "$backup_dir/config/"
    fi
    
    log_success "Конфигурационные файлы сохранены"
}

# Резервное копирование данных Prometheus
backup_prometheus_data() {
    local backup_dir=$1
    
    log_info "Резервное копирование данных Prometheus..."
    
    if docker ps | grep -q "prometheus"; then
        mkdir -p "$backup_dir/data/prometheus"
        
        # Создаем архив данных Prometheus
        docker exec prometheus tar czf /tmp/prometheus-data.tar.gz /prometheus 2>/dev/null || {
            log_warning "Не удалось создать архив данных Prometheus"
            return
        }
        
        # Копируем архив
        docker cp prometheus:/tmp/prometheus-data.tar.gz "$backup_dir/data/prometheus/"
        
        # Копируем конфигурацию
        docker cp prometheus:/etc/prometheus/prometheus.yml "$backup_dir/config/prometheus/" 2>/dev/null || true
        
        log_success "Данные Prometheus сохранены"
    else
        log_warning "Контейнер Prometheus не найден"
    fi
}

# Резервное копирование данных Grafana
backup_grafana_data() {
    local backup_dir=$1
    
    log_info "Резервное копирование данных Grafana..."
    
    if docker ps | grep -q "grafana"; then
        mkdir -p "$backup_dir/data/grafana"
        
        # Резервное копирование базы данных Grafana
        docker exec grafana curl -u admin:$GRAFANA_ADMIN_PASSWORD \
            http://localhost:3000/api/admin/settings > "$backup_dir/data/grafana/settings.json" 2>/dev/null || {
            log_warning "Не удалось получить настройки Grafana"
        }
        
        # Экспорт дашбордов
        docker exec grafana curl -u admin:$GRAFANA_ADMIN_PASSWORD \
            http://localhost:3000/api/search?type=dash-db > "$backup_dir/data/grafana/dashboards.json" 2>/dev/null || {
            log_warning "Не удалось получить список дашбордов"
        }
        
        # Копируем конфигурацию
        docker cp grafana:/etc/grafana "$backup_dir/config/grafana/" 2>/dev/null || true
        
        log_success "Данные Grafana сохранены"
    else
        log_warning "Контейнер Grafana не найден"
    fi
}

# Резервное копирование данных Elasticsearch
backup_elasticsearch_data() {
    local backup_dir=$1
    
    log_info "Резервное копирование данных Elasticsearch..."
    
    if docker ps | grep -q "elasticsearch"; then
        mkdir -p "$backup_dir/data/elasticsearch"
        
        # Создаем снапшот в Elasticsearch
        curl -X PUT "localhost:$ELASTICSEARCH_PORT/_snapshot/backup" \
            -H 'Content-Type: application/json' \
            -d '{
                "type": "fs",
                "settings": {
                    "location": "/backup"
                }
            }' 2>/dev/null || {
            log_warning "Не удалось создать репозиторий снапшотов"
        }
        
        # Создаем снапшот
        local snapshot_name="snapshot_$(date +%Y%m%d_%H%M%S)"
        curl -X PUT "localhost:$ELASTICSEARCH_PORT/_snapshot/backup/$snapshot_name" \
            -H 'Content-Type: application/json' \
            -d '{"indices": "dn-quest-logs-*"}' 2>/dev/null || {
            log_warning "Не удалось создать снапшот Elasticsearch"
        }
        
        # Копируем конфигурацию
        docker cp elasticsearch:/usr/share/elasticsearch/config "$backup_dir/config/elasticsearch/" 2>/dev/null || true
        
        log_success "Данные Elasticsearch сохранены"
    else
        log_warning "Контейнер Elasticsearch не найден"
    fi
}

# Резервное копирование данных AlertManager
backup_alertmanager_data() {
    local backup_dir=$1
    
    log_info "Резервное копирование данных AlertManager..."
    
    if docker ps | grep -q "alertmanager"; then
        mkdir -p "$backup_dir/data/alertmanager"
        
        # Копируем конфигурацию
        docker cp alertmanager:/etc/alertmanager "$backup_dir/config/alertmanager/" 2>/dev/null || true
        
        log_success "Данные AlertManager сохранены"
    else
        log_warning "Контейнер AlertManager не найден"
    fi
}

# Создание метаданных бэкапа
create_backup_metadata() {
    local backup_dir=$1
    
    log_info "Создание метаданных бэкапа..."
    
    cat > "$backup_dir/metadata.json" << EOF
{
  "backup_date": "$(date -Iseconds)",
  "backup_version": "1.0",
  "dn_quest_version": "$(git describe --tags --always 2>/dev/null || echo 'unknown')",
  "docker_compose_version": "$(docker compose --version)",
  "docker_version": "$(docker --version)",
  "system_info": {
    "hostname": "$(hostname)",
    "os": "$(uname -a)",
    "disk_usage": "$(df -h / | tail -1)",
    "memory_info": "$(free -h)"
  },
  "services": {
    "prometheus": $(docker ps --filter "name=prometheus" --format "{{.Status}}" 2>/dev/null | jq -R . || echo '"not running"'),
    "grafana": $(docker ps --filter "name=grafana" --format "{{.Status}}" 2>/dev/null | jq -R . || echo '"not running"'),
    "alertmanager": $(docker ps --filter "name=alertmanager" --format "{{.Status}}" 2>/dev/null | jq -R . || echo '"not running"'),
    "elasticsearch": $(docker ps --filter "name=elasticsearch" --format "{{.Status}}" 2>/dev/null | jq -R . || echo '"not running"'),
    "kibana": $(docker ps --filter "name=kibana" --format "{{.Status}}" 2>/dev/null | jq -R . || echo '"not running"'),
    "jaeger": $(docker ps --filter "name=jaeger" --format "{{.Status}}" 2>/dev/null | jq -R . || echo '"not running"')
  },
  "backup_contents": {
    "configurations": true,
    "prometheus_data": $(docker ps | grep -q "prometheus" && echo "true" || echo "false"),
    "grafana_data": $(docker ps | grep -q "grafana" && echo "true" || echo "false"),
    "elasticsearch_data": $(docker ps | grep -q "elasticsearch" && echo "true" || echo "false"),
    "alertmanager_data": $(docker ps | grep -q "alertmanager" && echo "true" || echo "false")
  }
}
EOF
    
    log_success "Метаданные бэкапа созданы"
}

# Создание архива бэкапа
create_backup_archive() {
    local backup_dir=$1
    
    log_info "Создание архива бэкапа..."
    
    local archive_name="${backup_dir}.tar.gz"
    
    # Создаем архив
    tar czf "$archive_name" -C "$(dirname "$backup_dir")" "$(basename "$backup_dir")"
    
    # Удаляем директорию после создания архива
    rm -rf "$backup_dir"
    
    log_success "Архив бэкапа создан: $archive_name"
    
    echo "$archive_name"
}

# Очистка старых бэкапов
cleanup_old_backups() {
    local keep_days=${1:-7}
    
    log_info "Очистка бэкапов старше $keep_days дней..."
    
    # Находим и удаляем старые бэкапы
    find backups -name "monitoring-*.tar.gz" -type f -mtime +$keep_days -delete 2>/dev/null || true
    
    # Удаляем пустые директории
    find backups -type d -empty -delete 2>/dev/null || true
    
    log_success "Очистка старых бэкапов завершена"
}

# Проверка целостности бэкапа
verify_backup() {
    local archive_file=$1
    
    log_info "Проверка целостности бэкапа..."
    
    if [ ! -f "$archive_file" ]; then
        log_error "Файл бэкапа не найден: $archive_file"
        return 1
    fi
    
    # Проверяем архив
    if ! tar tzf "$archive_file" > /dev/null 2>&1; then
        log_error "Архив поврежден: $archive_file"
        return 1
    fi
    
    # Проверяем наличие ключевых файлов
    local temp_dir=$(mktemp -d)
    tar xzf "$archive_file" -C "$temp_dir" 2>/dev/null || {
        log_error "Не удалось распаковать архив"
        rm -rf "$temp_dir"
        return 1
    }
    
    local backup_name=$(basename "$archive_file" .tar.gz)
    local metadata_file="$temp_dir/$backup_name/metadata.json"
    
    if [ ! -f "$metadata_file" ]; then
        log_warning "Метаданные бэкапа не найдены"
    else
        log_success "Метаданные бэкапа найдены"
    fi
    
    rm -rf "$temp_dir"
    log_success "Проверка целостности бэкапа завершена"
}

# Восстановление из бэкапа
restore_backup() {
    local archive_file=$1
    
    log_info "Восстановление из бэкапа: $archive_file"
    
    if [ ! -f "$archive_file" ]; then
        log_error "Файл бэкапа не найден: $archive_file"
        return 1
    fi
    
    # Создаем временную директорию
    local temp_dir=$(mktemp -d)
    
    # Распаковываем архив
    tar xzf "$archive_file" -C "$temp_dir" 2>/dev/null || {
        log_error "Не удалось распаковать архив"
        rm -rf "$temp_dir"
        return 1
    }
    
    local backup_name=$(basename "$archive_file" .tar.gz)
    local backup_dir="$temp_dir/$backup_name"
    
    # Восстанавливаем конфигурации
    if [ -d "$backup_dir/config" ]; then
        log_info "Восстановление конфигураций..."
        cp -r "$backup_dir/config/"* . 2>/dev/null || true
    fi
    
    # Восстанавливаем данные (требует остановки сервисов)
    log_warning "Для восстановления данных необходимо остановить сервисы мониторинга"
    log_info "Выполните: docker compose -f docker-compose.monitoring.yml down"
    log_info "Затем вручную восстановите данные из $backup_dir/data/"
    
    rm -rf "$temp_dir"
    log_success "Конфигурации восстановлены"
}

# Отображение информации о бэкапах
list_backups() {
    log_info "Список доступных бэкапов:"
    
    if [ ! -d "backups" ]; then
        log_warning "Директория бэкапов не найдена"
        return
    fi
    
    find backups -name "monitoring-*.tar.gz" -type f -exec ls -lh {} \; | while read line; do
        local file=$(echo "$line" | awk '{print $9}')
        local size=$(echo "$line" | awk '{print $5}')
        local date=$(echo "$line" | awk '{print $6, $7, $8}')
        
        echo "  $file - $size - $date"
    done
}

# Основная функция
main() {
    local action=${1:-"backup"}
    local backup_file=${2:-""}
    local keep_days=${3:-7}
    
    log_info "Запуск скрипта резервного копирования мониторинга DN Quest"
    
    load_environment
    
    case $action in
        "backup")
            local backup_dir=$(create_backup_directory)
            backup_configurations "$backup_dir"
            backup_prometheus_data "$backup_dir"
            backup_grafana_data "$backup_dir"
            backup_elasticsearch_data "$backup_dir"
            backup_alertmanager_data "$backup_dir"
            create_backup_metadata "$backup_dir"
            local archive=$(create_backup_archive "$backup_dir")
            verify_backup "$archive"
            cleanup_old_backups "$keep_days"
            log_success "Резервное копирование завершено: $archive"
            ;;
        "restore")
            if [ -z "$backup_file" ]; then
                log_error "Укажите файл бэкапа для восстановления"
                echo "Использование: $0 restore <backup_file>"
                exit 1
            fi
            restore_backup "$backup_file"
            ;;
        "list")
            list_backups
            ;;
        "verify")
            if [ -z "$backup_file" ]; then
                log_error "Укажите файл бэкапа для проверки"
                echo "Использование: $0 verify <backup_file>"
                exit 1
            fi
            verify_backup "$backup_file"
            ;;
        "cleanup")
            cleanup_old_backups "$keep_days"
            ;;
        *)
            log_error "Неизвестное действие: $action"
            echo "Доступные действия: backup, restore, list, verify, cleanup"
            echo "Использование:"
            echo "  $0 backup [keep_days] - Создать бэкап (хранить дней: $keep_days)"
            echo "  $0 restore <backup_file> - Восстановить из бэкапа"
            echo "  $0 list - Показать список бэкапов"
            echo "  $0 verify <backup_file> - Проверить целостность бэкапа"
            echo "  $0 cleanup [keep_days] - Очистить старые бэкапы"
            exit 1
            ;;
    esac
}

# Обработка сигналов
trap 'log_error "Скрипт прерван"; exit 1' INT TERM

# Запуск
main "$@"