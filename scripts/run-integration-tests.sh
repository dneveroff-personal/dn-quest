#!/bin/bash

# Скрипт для запуска интеграционных тестов DN Quest
# Использование: ./scripts/run-integration-tests.sh [options]

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Переменные
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$PROJECT_ROOT/test-logs"
REPORT_DIR="$PROJECT_ROOT/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="$LOG_DIR/integration-tests-$TIMESTAMP.log"
REPORT_FILE="$REPORT_DIR/integration-test-summary-$TIMESTAMP.html"

# Параметры по умолчанию
SERVICES=("authentication-service" "quest-management-service" "game-engine-service" 
          "team-management-service" "file-storage-service" "notification-service" 
          "statistics-service" "user-management-service" "api-gateway")
PARALLEL=true
SKIP_BUILD=false
PROFILE="integration-test"
TIMEOUT=300
CLEANUP=true
GENERATE_REPORT=true

# Функции для вывода
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

# Создание директорий
create_directories() {
    log_info "Создание директорий для логов и отчетов..."
    mkdir -p "$LOG_DIR"
    mkdir -p "$REPORT_DIR"
}

# Проверка зависимостей
check_dependencies() {
    log_info "Проверка зависимостей..."
    
    if ! command -v java &> /dev/null; then
        log_error "Java не установлена"
        exit 1
    fi
    
    if ! command -v ./gradlew &> /dev/null; then
        log_error "Gradle wrapper не найден"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker не установлен"
        exit 1
    fi
    
    if ! command -v docker compose &> /dev/null; then
        log_error "Docker Compose не установлен"
        exit 1
    fi
    
    log_success "Зависимости проверены"
}

# Сборка проекта
build_project() {
    if [ "$SKIP_BUILD" = false ]; then
        log_info "Сборка проекта..."
        ./gradlew clean build -x test -Pprofile="$PROFILE" | tee -a "$LOG_FILE"
        log_success "Проект собран"
    else
        log_info "Сборка проекта пропущена"
    fi
}

# Запуск тестовой инфраструктуры
start_test_infrastructure() {
    log_info "Запуск тестовой инфраструктуры..."
    
    # Запуск внешних сервисов (PostgreSQL, Redis, Kafka, etc.)
    docker compose -f docker-compose.test.yml up -d
    
    log_info "Ожидание готовности тестовой инфраструктуры..."
    
    # Ожидание готовности PostgreSQL
    local postgres_ready=false
    local postgres_attempts=0
    while [ "$postgres_ready" = false ] && [ $postgres_attempts -lt 30 ]; do
        if docker compose -f docker-compose.test.yml exec -T postgres pg_isready -U postgres; then
            postgres_ready=true
            log_success "PostgreSQL готов"
        else
            sleep 2
            postgres_attempts=$((postgres_attempts + 1))
        fi
    done
    
    if [ "$postgres_ready" = false ]; then
        log_error "PostgreSQL не запустился за отведенное время"
        exit 1
    fi
    
    # Ожидание готовности Redis
    local redis_ready=false
    local redis_attempts=0
    while [ "$redis_ready" = false ] && [ $redis_attempts -lt 30 ]; do
        if docker compose -f docker-compose.test.yml exec -T redis redis-cli ping; then
            redis_ready=true
            log_success "Redis готов"
        else
            sleep 2
            redis_attempts=$((redis_attempts + 1))
        fi
    done
    
    if [ "$redis_ready" = false ]; then
        log_error "Redis не запустился за отведенное время"
        exit 1
    fi
    
    # Ожидание готовности Kafka
    local kafka_ready=false
    local kafka_attempts=0
    while [ "$kafka_ready" = false ] && [ $kafka_attempts -lt 60 ]; do
        if docker compose -f docker-compose.test.yml exec -T kafka kafka-topics.sh --bootstrap-server localhost:9092 --list &>/dev/null; then
            kafka_ready=true
            log_success "Kafka готов"
        else
            sleep 5
            kafka_attempts=$((kafka_attempts + 1))
        fi
    done
    
    if [ "$kafka_ready" = false ]; then
        log_error "Kafka не запустился за отведенное время"
        exit 1
    fi
    
    log_success "Тестовая инфраструктура готова"
}

# Запуск микросервисов
start_microservices() {
    log_info "Запуск микросервисов..."
    
    for service in "${SERVICES[@]}"; do
        log_info "Запуск сервиса: $service"
        
        # Запуск сервиса в фоновом режиме
        nohup java -jar "$service/build/libs/$service-*.jar" \
            --spring.profiles.active="$PROFILE" \
            > "$LOG_DIR/$service-$TIMESTAMP.log" 2>&1 &
        
        local service_pid=$!
        echo "$service_pid" > "$LOG_DIR/$service.pid"
        
        log_info "Сервис $service запущен с PID: $service_pid"
    done
    
    log_info "Ожидание готовности микросервисов..."
    sleep 30
    
    # Проверка здоровья сервисов
    for service in "${SERVICES[@]}"; do
        local health_url=""
        case $service in
            "api-gateway")
                health_url="http://localhost:8080/actuator/health"
                ;;
            "authentication-service")
                health_url="http://localhost:8081/actuator/health"
                ;;
            "quest-management-service")
                health_url="http://localhost:8082/actuator/health"
                ;;
            "game-engine-service")
                health_url="http://localhost:8083/actuator/health"
                ;;
            "team-management-service")
                health_url="http://localhost:8084/actuator/health"
                ;;
            "file-storage-service")
                health_url="http://localhost:8085/actuator/health"
                ;;
            "notification-service")
                health_url="http://localhost:8086/actuator/health"
                ;;
            "statistics-service")
                health_url="http://localhost:8087/actuator/health"
                ;;
            "user-management-service")
                health_url="http://localhost:8088/actuator/health"
                ;;
        esac
        
        local service_ready=false
        local attempts=0
        while [ "$service_ready" = false ] && [ $attempts -lt 30 ]; do
            if curl -f -s "$health_url" > /dev/null; then
                service_ready=true
                log_success "Сервис $service готов"
            else
                sleep 2
                attempts=$((attempts + 1))
            fi
        done
        
        if [ "$service_ready" = false ]; then
            log_error "Сервис $service не готов после $attempts попыток"
            log_error "Лог сервиса: $LOG_DIR/$service-$TIMESTAMP.log"
            exit 1
        fi
    done
    
    log_success "Все микросервисы готовы"
}

# Запуск интеграционных тестов
run_integration_tests() {
    log_info "Запуск интеграционных тестов..."
    
    local start_time=$(date +%s)
    local test_results=()
    
    if [ "$PARALLEL" = true ]; then
        log_info "Запуск тестов в параллельном режиме..."
        
        # Запуск тестов для каждого сервиса параллельно
        for service in "${SERVICES[@]}"; do
            (
                log_info "Запуск тестов для $service..."
                
                local service_start_time=$(date +%s)
                
                if ./gradlew ":$service:integrationTest" -Pprofile="$PROFILE" --continue >> "$LOG_FILE" 2>&1; then
                    local service_end_time=$(date +%s)
                    local duration=$((service_end_time - service_start_time))
                    test_results+=("$service:SUCCESS:$duration")
                    log_success "Тесты для $service завершены успешно за ${duration} секунд"
                else
                    local service_end_time=$(date +%s)
                    local duration=$((service_end_time - service_start_time))
                    test_results+=("$service:FAILED:$duration")
                    log_error "Тесты для $service завершены с ошибками за ${duration} секунд"
                fi
            ) &
        done
        
        # Ожидание завершения всех тестов
        wait
        
    else
        log_info "Запуск тестов в последовательном режиме..."
        
        for service in "${SERVICES[@]}"; do
            log_info "Запуск тестов для $service..."
            
            local service_start_time=$(date +%s)
            
            if ./gradlew ":$service:integrationTest" -Pprofile="$PROFILE" --continue >> "$LOG_FILE" 2>&1; then
                local service_end_time=$(date +%s)
                local duration=$((service_end_time - service_start_time))
                test_results+=("$service:SUCCESS:$duration")
                log_success "Тесты для $service завершены успешно за ${duration} секунд"
            else
                local service_end_time=$(date +%s)
                local duration=$((service_end_time - service_start_time))
                test_results+=("$service:FAILED:$duration")
                log_error "Тесты для $service завершены с ошибками за ${duration} секунд"
            fi
        done
    fi
    
    local end_time=$(date +%s)
    local total_duration=$((end_time - start_time))
    
    log_info "Все интеграционные тесты завершены за ${total_duration} секунд"
    
    # Сохранение результатов
    echo "TOTAL_DURATION=$total_duration" > "$REPORT_FILE.tmp"
    for result in "${test_results[@]}"; do
        echo "TEST_RESULT=$result" >> "$REPORT_FILE.tmp"
    done
}

# Сбор результатов тестов
collect_test_results() {
    log_info "Сбор результатов тестов..."
    
    local total_tests=0
    local passed_tests=0
    local failed_tests=0
    local skipped_tests=0
    
    # Сбор результатов из XML отчетов
    find . -name "*integrationTest*TEST-*.xml" -type f | while read -r file; do
        # Парсинг XML и подсчет результатов
        local tests=$(grep -o 'tests="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local failures=$(grep -o 'failures="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local errors=$(grep -o 'errors="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local skipped=$(grep -o 'skipped="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        
        total_tests=$((total_tests + tests))
        failed_tests=$((failed_tests + failures + errors))
        skipped_tests=$((skipped_tests + skipped))
    done
    
    passed_tests=$((total_tests - failed_tests - skipped_tests))
    
    echo "TOTAL_TESTS=$total_tests" >> "$REPORT_FILE.tmp"
    echo "PASSED_TESTS=$passed_tests" >> "$REPORT_FILE.tmp"
    echo "FAILED_TESTS=$failed_tests" >> "$REPORT_FILE.tmp"
    echo "SKIPPED_TESTS=$skipped_tests" >> "$REPORT_FILE.tmp"
}

# Остановка микросервисов
stop_microservices() {
    if [ "$CLEANUP" = true ]; then
        log_info "Остановка микросервисов..."
        
        for service in "${SERVICES[@]}"; do
            if [ -f "$LOG_DIR/$service.pid" ]; then
                local pid=$(cat "$LOG_DIR/$service.pid")
                if kill -0 "$pid" 2>/dev/null; then
                    log_info "Остановка сервиса $service (PID: $pid)"
                    kill "$pid"
                    sleep 5
                    
                    # Принудительное завершение если необходимо
                    if kill -0 "$pid" 2>/dev/null; then
                        log_warning "Принудительное завершение сервиса $service"
                        kill -9 "$pid"
                    fi
                fi
                rm -f "$LOG_DIR/$service.pid"
            fi
        done
        
        log_success "Микросервисы остановлены"
    fi
}

# Остановка тестовой инфраструктуры
stop_test_infrastructure() {
    if [ "$CLEANUP" = true ]; then
        log_info "Остановка тестовой инфраструктуры..."
        docker compose -f docker-compose.test.yml down
        log_success "Тестовая инфраструктура остановлена"
    fi
}

# Генерация HTML отчета
generate_html_report() {
    if [ "$GENERATE_REPORT" = true ]; then
        log_info "Генерация HTML отчета..."
        
        # Чтение временного файла с результатами
        if [ -f "$REPORT_FILE.tmp" ]; then
            source "$REPORT_FILE.tmp"
        else
            TOTAL_TESTS=0
            PASSED_TESTS=0
            FAILED_TESTS=0
            SKIPPED_TESTS=0
            TOTAL_DURATION=0
        fi
        
        cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Отчет об интеграционных тестах DN Quest</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 20px; border-radius: 5px; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
        .success { color: green; }
        .failure { color: red; }
        .warning { color: orange; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .progress-bar { width: 100%; background-color: #f0f0f0; border-radius: 5px; }
        .progress { height: 20px; background-color: #4CAF50; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Отчет об интеграционных тестах DN Quest</h1>
        <p>Дата и время: $(date)</p>
        <p>Профиль: $PROFILE</p>
        <p>Параллельный запуск: $PARALLEL</p>
    </div>
    
    <div class="section">
        <h2>Общая статистика</h2>
        <table>
            <tr><th>Метрика</th><th>Значение</th></tr>
            <tr><td>Всего тестов</td><td>$TOTAL_TESTS</td></tr>
            <tr><td>Пройдено</td><td class="success">$PASSED_TESTS</td></tr>
            <tr><td>Провалено</td><td class="failure">$FAILED_TESTS</td></tr>
            <tr><td>Пропущено</td><td class="warning">$SKIPPED_TESTS</td></tr>
            <tr><td>Общее время</td><td>$TOTAL_DURATION сек</td></tr>
        </table>
        
        <h3>Прогресс выполнения</h3>
        <div class="progress-bar">
            <div class="progress" style="width: $(( PASSED_TESTS * 100 / TOTAL_TESTS ))%"></div>
        </div>
        <p>$(( PASSED_TESTS * 100 / TOTAL_TESTS ))% тестов пройдено</p>
    </div>
    
    <div class="section">
        <h2>Результаты по сервисам</h2>
        <table>
            <tr><th>Сервис</th><th>Статус</th><th>Время (сек)</th></tr>
EOF
        
        # Добавление результатов по сервисам
        for result in "${test_results[@]}"; do
            IFS=':' read -r service status duration <<< "$result"
            local status_class=""
            if [ "$status" = "SUCCESS" ]; then
                status_class="success"
            else
                status_class="failure"
            fi
            
            echo "            <tr><td>$service</td><td class=\"$status_class\">$status</td><td>$duration</td></tr>" >> "$REPORT_FILE"
        done
        
        cat >> "$REPORT_FILE" << EOF
        </table>
    </div>
    
    <div class="section">
        <h2>Детальная информация</h2>
        <p>Полный лог выполнения: <a href="$LOG_FILE">$LOG_FILE</a></p>
        <p>XML отчеты: $(find . -name "*integrationTest*TEST-*.xml" | wc -l) файлов</p>
        <p>Логи сервисов: $LOG_DIR/*-$TIMESTAMP.log</p>
    </div>
</body>
</html>
EOF
        
        log_success "HTML отчет сгенерирован: $REPORT_FILE"
        rm -f "$REPORT_FILE.tmp"
    fi
}

# Очистка
cleanup() {
    stop_microservices
    stop_test_infrastructure
    rm -f "$REPORT_FILE.tmp"
}

# Обработка сигналов
trap cleanup EXIT INT TERM

# Парсинг аргументов
while [[ $# -gt 0 ]]; do
    case $1 in
        --no-parallel)
            PARALLEL=false
            shift
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --no-cleanup)
            CLEANUP=false
            shift
            ;;
        --no-report)
            GENERATE_REPORT=false
            shift
            ;;
        --profile)
            PROFILE="$2"
            shift 2
            ;;
        --timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        --services)
            IFS=',' read -ra SERVICES <<< "$2"
            shift 2
            ;;
        --help)
            echo "Использование: $0 [options]"
            echo "Options:"
            echo "  --no-parallel      Запускать тесты последовательно"
            echo "  --skip-build       Пропустить сборку проекта"
            echo "  --no-cleanup       Не останавливать сервисы после тестов"
            echo "  --no-report        Не генерировать HTML отчет"
            echo "  --profile PROFILE  Использовать указанный профиль (default: integration-test)"
            echo "  --timeout SECONDS  Таймаут ожидания сервисов (default: 300)"
            echo "  --services LIST    Список сервисов через запятую (default: все)"
            echo "  --help             Показать эту справку"
            exit 0
            ;;
        *)
            log_error "Неизвестный параметр: $1"
            exit 1
            ;;
    esac
done

# Основной выполнение
main() {
    log_info "Начало выполнения интеграционных тестов DN Quest"
    log_info "Лог выполнения: $LOG_FILE"
    
    create_directories
    check_dependencies
    build_project
    start_test_infrastructure
    start_microservices
    run_integration_tests
    collect_test_results
    generate_html_report
    
    log_success "Интеграционные тесты завершены!"
    log_info "Отчет доступен: $REPORT_FILE"
}

main