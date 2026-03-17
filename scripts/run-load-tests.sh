#!/bin/bash

# Скрипт для запуска нагрузочных тестов DN Quest
# Использование: ./scripts/run-load-tests.sh [options]

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
LOG_FILE="$LOG_DIR/load-tests-$TIMESTAMP.log"
REPORT_FILE="$REPORT_DIR/load-test-summary-$TIMESTAMP.html"

# Параметры по умолчанию
LOAD_TEST_SCENARIOS=("api-gateway" "kafka-events" "database" "file-storage")
CONCURRENT_USERS=50
TEST_DURATION=300
RAMP_UP_TIME=60
PROFILE="load-test"
SKIP_BUILD=false
GENERATE_REPORT=true
MONITOR_RESOURCES=true
CLEANUP=true

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
    
    # Проверка наличия утилит для мониторинга
    if [ "$MONITOR_RESOURCES" = true ]; then
        if ! command -v top &> /dev/null; then
            log_warning "top не установлен, мониторинг ресурсов будет ограничен"
        fi
        
        if ! command -v iostat &> /dev/null; then
            log_warning "iostat не установлен, мониторинг диска будет ограничен"
        fi
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

# Запуск тестовой инфраструктуры для нагрузочных тестов
start_load_test_infrastructure() {
    log_info "Запуск инфраструктуры для нагрузочных тестов..."
    
    # Запуск оптимизированной конфигурации для нагрузочных тестов
    docker compose -f docker-compose.load.yml up -d
    
    log_info "Ожидание готовности инфраструктуры..."
    
    # Ожидание готовности PostgreSQL
    local postgres_ready=false
    local postgres_attempts=0
    while [ "$postgres_ready" = false ] && [ $postgres_attempts -lt 30 ]; do
        if docker compose -f docker-compose.load.yml exec -T postgres pg_isready -U postgres; then
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
        if docker compose -f docker-compose.load.yml exec -T redis redis-cli ping; then
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
        if docker compose -f docker-compose.load.yml exec -T kafka kafka-topics.sh --bootstrap-server localhost:9092 --list &>/dev/null; then
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
    
    log_success "Инфраструктура для нагрузочных тестов готова"
}

# Запуск микросервисов для нагрузочных тестов
start_load_test_services() {
    log_info "Запуск микросервисов для нагрузочных тестов..."
    
    local services=("api-gateway" "authentication-service" "quest-management-service" 
                   "game-engine-service" "statistics-service")
    
    for service in "${services[@]}"; do
        log_info "Запуск сервиса: $service"
        
        # Запуск сервиса с оптимизированными настройками для нагрузочных тестов
        nohup java -jar "$service/build/libs/$service-*.jar" \
            --spring.profiles.active="$PROFILE" \
            -Xms512m -Xmx2g \
            -XX:+UseG1GC \
            -XX:MaxGCPauseMillis=200 \
            > "$LOG_DIR/$service-load-$TIMESTAMP.log" 2>&1 &
        
        local service_pid=$!
        echo "$service_pid" > "$LOG_DIR/$service-load.pid"
        
        log_info "Сервис $service запущен с PID: $service_pid"
    done
    
    log_info "Ожидание готовности микросервисов..."
    sleep 45
    
    # Проверка здоровья сервисов
    for service in "${services[@]}"; do
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
            "statistics-service")
                health_url="http://localhost:8087/actuator/health"
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
            log_error "Лог сервиса: $LOG_DIR/$service-load-$TIMESTAMP.log"
            exit 1
        fi
    done
    
    log_success "Все микросервисы готовы"
}

# Запуск мониторинга ресурсов
start_resource_monitoring() {
    if [ "$MONITOR_RESOURCES" = true ]; then
        log_info "Запуск мониторинга ресурсов..."
        
        # Мониторинг CPU и памяти
        nohup top -b -d 5 -n $((TEST_DURATION / 5)) > "$LOG_DIR/cpu-memory-$TIMESTAMP.log" 2>&1 &
        echo $! > "$LOG_DIR/cpu-monitor.pid"
        
        # Мониторинг диска
        if command -v iostat &> /dev/null; then
            nohup iostat -x 5 $((TEST_DURATION / 5)) > "$LOG_DIR/disk-io-$TIMESTAMP.log" 2>&1 &
            echo $! > "$LOG_DIR/disk-monitor.pid"
        fi
        
        # Мониторинг Docker контейнеров
        nohup docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}" \
            > "$LOG_DIR/docker-stats-$TIMESTAMP.log" 2>&1 &
        echo $! > "$LOG_DIR/docker-monitor.pid"
        
        log_success "Мониторинг ресурсов запущен"
    fi
}

# Запуск нагрузочных тестов API Gateway
run_api_gateway_load_tests() {
    if [[ " ${LOAD_TEST_SCENARIOS[@]} " =~ " api-gateway " ]]; then
        log_info "Запуск нагрузочных тестов API Gateway..."
        
        local start_time=$(date +%s)
        
        # Запуск тестов с параметрами
        ./gradlew :dn-quest-shared:loadTest \
            -PloadTest.concurrentUsers="$CONCURRENT_USERS" \
            -PloadTest.testDuration="$TEST_DURATION" \
            -PloadTest.rampUpTime="$RAMP_UP_TIME" \
            -PloadTest.targetService="api-gateway" \
            -Pprofile="$PROFILE" \
            --continue >> "$LOG_FILE" 2>&1
        
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        log_success "Нагрузочные тесты API Gateway завершены за ${duration} секунд"
        echo "API_GATEWAY_DURATION=$duration" >> "$REPORT_FILE.tmp"
    fi
}

# Запуск нагрузочных тестов Kafka
run_kafka_load_tests() {
    if [[ " ${LOAD_TEST_SCENARIOS[@]} " =~ " kafka-events " ]]; then
        log_info "Запуск нагрузочных тестов Kafka..."
        
        local start_time=$(date +%s)
        
        # Запуск тестов Kafka
        ./gradlew :dn-quest-shared:loadTest \
            -PloadTest.concurrentUsers="$CONCURRENT_USERS" \
            -PloadTest.testDuration="$TEST_DURATION" \
            -PloadTest.rampUpTime="$RAMP_UP_TIME" \
            -PloadTest.targetService="kafka" \
            -Pprofile="$PROFILE" \
            --continue >> "$LOG_FILE" 2>&1
        
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        log_success "Нагрузочные тесты Kafka завершены за ${duration} секунд"
        echo "KAFKA_DURATION=$duration" >> "$REPORT_FILE.tmp"
    fi
}

# Запуск нагрузочных тестов базы данных
run_database_load_tests() {
    if [[ " ${LOAD_TEST_SCENARIOS[@]} " =~ " database " ]]; then
        log_info "Запуск нагрузочных тестов базы данных..."
        
        local start_time=$(date +%s)
        
        # Запуск тестов базы данных
        ./gradlew :dn-quest-shared:loadTest \
            -PloadTest.concurrentUsers="$CONCURRENT_USERS" \
            -PloadTest.testDuration="$TEST_DURATION" \
            -PloadTest.rampUpTime="$RAMP_UP_TIME" \
            -PloadTest.targetService="database" \
            -Pprofile="$PROFILE" \
            --continue >> "$LOG_FILE" 2>&1
        
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        log_success "Нагрузочные тесты базы данных завершены за ${duration} секунд"
        echo "DATABASE_DURATION=$duration" >> "$REPORT_FILE.tmp"
    fi
}

# Запуск нагрузочных тестов файлового хранилища
run_file_storage_load_tests() {
    if [[ " ${LOAD_TEST_SCENARIOS[@]} " =~ " file-storage " ]]; then
        log_info "Запуск нагрузочных тестов файлового хранилища..."
        
        local start_time=$(date +%s)
        
        # Запуск файлового сервиса
        nohup java -jar "file-storage-service/build/libs/file-storage-service-*.jar" \
            --spring.profiles.active="$PROFILE" \
            -Xms512m -Xmx2g \
            > "$LOG_DIR/file-storage-load-$TIMESTAMP.log" 2>&1 &
        
        local file_storage_pid=$!
        echo "$file_storage_pid" > "$LOG_DIR/file-storage-load.pid"
        
        sleep 30
        
        # Запуск тестов файлового хранилища
        ./gradlew :dn-quest-shared:loadTest \
            -PloadTest.concurrentUsers="$CONCURRENT_USERS" \
            -PloadTest.testDuration="$TEST_DURATION" \
            -PloadTest.rampUpTime="$RAMP_UP_TIME" \
            -PloadTest.targetService="file-storage" \
            -Pprofile="$PROFILE" \
            --continue >> "$LOG_FILE" 2>&1
        
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        log_success "Нагрузочные тесты файлового хранилища завершены за ${duration} секунд"
        echo "FILE_STORAGE_DURATION=$duration" >> "$REPORT_FILE.tmp"
        
        # Остановка файлового сервиса
        if kill -0 "$file_storage_pid" 2>/dev/null; then
            kill "$file_storage_pid"
        fi
        rm -f "$LOG_DIR/file-storage-load.pid"
    fi
}

# Сбор результатов нагрузочных тестов
collect_load_test_results() {
    log_info "Сбор результатов нагрузочных тестов..."
    
    # Сбор метрик из логов
    local total_requests=0
    local successful_requests=0
    local failed_requests=0
    local avg_response_time=0
    local max_response_time=0
    local min_response_time=999999
    
    # Поиск файлов с результатами тестов
    find . -name "*loadTest*results.json" -type f | while read -r file; do
        # Парсинг JSON результатов (упрощенный)
        if command -v jq &> /dev/null; then
            local requests=$(jq -r '.totalRequests // 0' "$file")
            local successful=$(jq -r '.successfulRequests // 0' "$file")
            local failed=$(jq -r '.failedRequests // 0' "$file")
            local avg_time=$(jq -r '.averageResponseTime // 0' "$file")
            local max_time=$(jq -r '.maxResponseTime // 0' "$file")
            local min_time=$(jq -r '.minResponseTime // 999999' "$file")
            
            total_requests=$((total_requests + requests))
            successful_requests=$((successful_requests + successful))
            failed_requests=$((failed_requests + failed))
            
            # Расчет средних значений
            if [ "$avg_time" -gt "$avg_response_time" ]; then
                avg_response_time=$avg_time
            fi
            
            if [ "$max_time" -gt "$max_response_time" ]; then
                max_response_time=$max_time
            fi
            
            if [ "$min_time" -lt "$min_response_time" ]; then
                min_response_time=$min_time
            fi
        fi
    done
    
    echo "TOTAL_REQUESTS=$total_requests" >> "$REPORT_FILE.tmp"
    echo "SUCCESSFUL_REQUESTS=$successful_requests" >> "$REPORT_FILE.tmp"
    echo "FAILED_REQUESTS=$failed_requests" >> "$REPORT_FILE.tmp"
    echo "AVG_RESPONSE_TIME=$avg_response_time" >> "$REPORT_FILE.tmp"
    echo "MAX_RESPONSE_TIME=$max_response_time" >> "$REPORT_FILE.tmp"
    echo "MIN_RESPONSE_TIME=$min_response_time" >> "$REPORT_FILE.tmp"
}

# Остановка мониторинга ресурсов
stop_resource_monitoring() {
    if [ "$MONITOR_RESOURCES" = true ]; then
        log_info "Остановка мониторинга ресурсов..."
        
        # Остановка мониторинга CPU
        if [ -f "$LOG_DIR/cpu-monitor.pid" ]; then
            local pid=$(cat "$LOG_DIR/cpu-monitor.pid")
            if kill -0 "$pid" 2>/dev/null; then
                kill "$pid"
            fi
            rm -f "$LOG_DIR/cpu-monitor.pid"
        fi
        
        # Остановка мониторинга диска
        if [ -f "$LOG_DIR/disk-monitor.pid" ]; then
            local pid=$(cat "$LOG_DIR/disk-monitor.pid")
            if kill -0 "$pid" 2>/dev/null; then
                kill "$pid"
            fi
            rm -f "$LOG_DIR/disk-monitor.pid"
        fi
        
        # Остановка мониторинга Docker
        if [ -f "$LOG_DIR/docker-monitor.pid" ]; then
            local pid=$(cat "$LOG_DIR/docker-monitor.pid")
            if kill -0 "$pid" 2>/dev/null; then
                kill "$pid"
            fi
            rm -f "$LOG_DIR/docker-monitor.pid"
        fi
        
        log_success "Мониторинг ресурсов остановлен"
    fi
}

# Остановка микросервисов
stop_load_test_services() {
    if [ "$CLEANUP" = true ]; then
        log_info "Остановка микросервисов..."
        
        local services=("api-gateway" "authentication-service" "quest-management-service" 
                       "game-engine-service" "statistics-service")
        
        for service in "${services[@]}"; do
            if [ -f "$LOG_DIR/$service-load.pid" ]; then
                local pid=$(cat "$LOG_DIR/$service-load.pid")
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
                rm -f "$LOG_DIR/$service-load.pid"
            fi
        done
        
        log_success "Микросервисы остановлены"
    fi
}

# Остановка инфраструктуры
stop_load_test_infrastructure() {
    if [ "$CLEANUP" = true ]; then
        log_info "Остановка инфраструктуры..."
        docker compose -f docker-compose.load.yml down
        log_success "Инфраструктура остановлена"
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
            TOTAL_REQUESTS=0
            SUCCESSFUL_REQUESTS=0
            FAILED_REQUESTS=0
            AVG_RESPONSE_TIME=0
            MAX_RESPONSE_TIME=0
            MIN_RESPONSE_TIME=0
            API_GATEWAY_DURATION=0
            KAFKA_DURATION=0
            DATABASE_DURATION=0
            FILE_STORAGE_DURATION=0
        fi
        
        local success_rate=0
        if [ "$TOTAL_REQUESTS" -gt 0 ]; then
            success_rate=$((SUCCESSFUL_REQUESTS * 100 / TOTAL_REQUESTS))
        fi
        
        cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Отчет о нагрузочных тестах DN Quest</title>
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
        .metric { display: inline-block; margin: 10px; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Отчет о нагрузочных тестах DN Quest</h1>
        <p>Дата и время: $(date)</p>
        <p>Профиль: $PROFILE</p>
        <p>Параллельные пользователи: $CONCURRENT_USERS</p>
        <p>Длительность теста: $TEST_DURATION сек</p>
        <p>Время нарастания: $RAMP_UP_TIME сек</p>
    </div>
    
    <div class="section">
        <h2>Общая статистика</h2>
        <div class="metric">
            <h3>Всего запросов</h3>
            <p>$TOTAL_REQUESTS</p>
        </div>
        <div class="metric">
            <h3>Успешных</h3>
            <p class="success">$SUCCESSFUL_REQUESTS</p>
        </div>
        <div class="metric">
            <h3>Провалено</h3>
            <p class="failure">$FAILED_REQUESTS</p>
        </div>
        <div class="metric">
            <h3>Успешность</h3>
            <p>$success_rate%</p>
        </div>
        
        <h3>Прогресс выполнения</h3>
        <div class="progress-bar">
            <div class="progress" style="width: $success_rate%"></div>
        </div>
    </div>
    
    <div class="section">
        <h2>Время ответа</h2>
        <table>
            <tr><th>Метрика</th><th>Значение (мс)</th></tr>
            <tr><td>Среднее время</td><td>$AVG_RESPONSE_TIME</td></tr>
            <tr><td>Минимальное время</td><td>$MIN_RESPONSE_TIME</td></tr>
            <tr><td>Максимальное время</td><td>$MAX_RESPONSE_TIME</td></tr>
        </table>
    </div>
    
    <div class="section">
        <h2>Время выполнения по сценариям</h2>
        <table>
            <tr><th>Сценарий</th><th>Время (сек)</th></tr>
EOF
        
        # Добавление результатов по сценариям
        if [[ " ${LOAD_TEST_SCENARIOS[@]} " =~ " api-gateway " ]]; then
            echo "            <tr><td>API Gateway</td><td>$API_GATEWAY_DURATION</td></tr>" >> "$REPORT_FILE"
        fi
        
        if [[ " ${LOAD_TEST_SCENARIOS[@]} " =~ " kafka-events " ]]; then
            echo "            <tr><td>Kafka Events</td><td>$KAFKA_DURATION</td></tr>" >> "$REPORT_FILE"
        fi
        
        if [[ " ${LOAD_TEST_SCENARIOS[@]} " =~ " database " ]]; then
            echo "            <tr><td>Database</td><td>$DATABASE_DURATION</td></tr>" >> "$REPORT_FILE"
        fi
        
        if [[ " ${LOAD_TEST_SCENARIOS[@]} " =~ " file-storage " ]]; then
            echo "            <tr><td>File Storage</td><td>$FILE_STORAGE_DURATION</td></tr>" >> "$REPORT_FILE"
        fi
        
        cat >> "$REPORT_FILE" << EOF
        </table>
    </div>
    
    <div class="section">
        <h2>Детальная информация</h2>
        <p>Полный лог выполнения: <a href="$LOG_FILE">$LOG_FILE</a></p>
        <p>Мониторинг CPU: <a href="$LOG_DIR/cpu-memory-$TIMESTAMP.log">CPU/Memory Log</a></p>
        <p>Мониторинг диска: <a href="$LOG_DIR/disk-io-$TIMESTAMP.log">Disk I/O Log</a></p>
        <p>Статистика Docker: <a href="$LOG_DIR/docker-stats-$TIMESTAMP.log">Docker Stats</a></p>
        <p>Логи сервисов: $LOG_DIR/*-load-$TIMESTAMP.log</p>
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
    stop_resource_monitoring
    stop_load_test_services
    stop_load_test_infrastructure
    rm -f "$REPORT_FILE.tmp"
}

# Обработка сигналов
trap cleanup EXIT INT TERM

# Парсинг аргументов
while [[ $# -gt 0 ]]; do
    case $1 in
        --concurrent-users)
            CONCURRENT_USERS="$2"
            shift 2
            ;;
        --test-duration)
            TEST_DURATION="$2"
            shift 2
            ;;
        --ramp-up-time)
            RAMP_UP_TIME="$2"
            shift 2
            ;;
        --scenarios)
            IFS=',' read -ra LOAD_TEST_SCENARIOS <<< "$2"
            shift 2
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --no-monitoring)
            MONITOR_RESOURCES=false
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
        --help)
            echo "Использование: $0 [options]"
            echo "Options:"
            echo "  --concurrent-users NUM    Количество параллельных пользователей (default: 50)"
            echo "  --test-duration SECONDS   Длительность теста (default: 300)"
            echo "  --ramp-up-time SECONDS    Время нарастания нагрузки (default: 60)"
            echo "  --scenarios LIST          Список сценариев через запятую (default: все)"
            echo "  --skip-build              Пропустить сборку проекта"
            echo "  --no-monitoring           Отключить мониторинг ресурсов"
            echo "  --no-cleanup              Не останавливать сервисы после тестов"
            echo "  --no-report               Не генерировать HTML отчет"
            echo "  --profile PROFILE         Использовать указанный профиль (default: load-test)"
            echo "  --help                    Показать эту справку"
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
    log_info "Начало выполнения нагрузочных тестов DN Quest"
    log_info "Лог выполнения: $LOG_FILE"
    log_info "Параметры: $CONCURRENT_USERS пользователей, $TEST_DURATION сек, $RAMP_UP_TIME сек нарастание"
    
    create_directories
    check_dependencies
    build_project
    start_load_test_infrastructure
    start_load_test_services
    start_resource_monitoring
    
    run_api_gateway_load_tests
    run_kafka_load_tests
    run_database_load_tests
    run_file_storage_load_tests
    
    collect_load_test_results
    generate_html_report
    
    log_success "Нагрузочные тесты завершены!"
    log_info "Отчет доступен: $REPORT_FILE"
}

main