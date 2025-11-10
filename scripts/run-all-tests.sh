#!/bin/bash

# Скрипт для запуска всех тестов DN Quest
# Использование: ./scripts/run-all-tests.sh [options]

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
LOG_FILE="$LOG_DIR/all-tests-$TIMESTAMP.log"
REPORT_FILE="$REPORT_DIR/test-summary-$TIMESTAMP.html"

# Параметры по умолчанию
RUN_UNIT_TESTS=true
RUN_INTEGRATION_TESTS=true
RUN_E2E_TESTS=true
RUN_LOAD_TESTS=false
PARALLEL=true
GENERATE_REPORT=true
SKIP_BUILD=false
PROFILE="test"

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
        log_warning "Docker не установлен, тесты с TestContainers могут не работать"
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

# Запуск unit тестов
run_unit_tests() {
    if [ "$RUN_UNIT_TESTS" = true ]; then
        log_info "Запуск unit тестов..."
        
        local start_time=$(date +%s)
        
        if [ "$PARALLEL" = true ]; then
            ./gradlew test -Pprofile="$PROFILE" --parallel --max-worker-count=4 \
                --continue | tee -a "$LOG_FILE"
        else
            ./gradlew test -Pprofile="$PROFILE" --continue | tee -a "$LOG_FILE"
        fi
        
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        log_success "Unit тесты завершены за ${duration} секунд"
        echo "UNIT_TESTS_DURATION=$duration" >> "$REPORT_FILE.tmp"
    else
        log_info "Unit тесты пропущены"
    fi
}

# Запуск интеграционных тестов
run_integration_tests() {
    if [ "$RUN_INTEGRATION_TESTS" = true ]; then
        log_info "Запуск интеграционных тестов..."
        
        local start_time=$(date +%s)
        
        # Запуск тестовых контейнеров
        log_info "Запуск тестовых контейнеров..."
        docker-compose -f docker-compose.test.yml up -d
        
        # Ожидание готовности сервисов
        log_info "Ожидание готовности сервисов..."
        sleep 30
        
        if [ "$PARALLEL" = true ]; then
            ./gradlew integrationTest -Pprofile="$PROFILE" --parallel --max-worker-count=3 \
                --continue | tee -a "$LOG_FILE"
        else
            ./gradlew integrationTest -Pprofile="$PROFILE" --continue | tee -a "$LOG_FILE"
        fi
        
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        log_success "Интеграционные тесты завершены за ${duration} секунд"
        echo "INTEGRATION_TESTS_DURATION=$duration" >> "$REPORT_FILE.tmp"
        
        # Остановка тестовых контейнеров
        log_info "Остановка тестовых контейнеров..."
        docker-compose -f docker-compose.test.yml down
    else
        log_info "Интеграционные тесты пропущены"
    fi
}

# Запуск E2E тестов
run_e2e_tests() {
    if [ "$RUN_E2E_TESTS" = true ]; then
        log_info "Запуск End-to-End тестов..."
        
        local start_time=$(date +%s)
        
        # Запуск всех сервисов для E2E тестов
        log_info "Запуск всех сервисов для E2E тестов..."
        docker-compose -f docker-compose.yml up -d
        
        # Ожидание готовности всех сервисов
        log_info "Ожидание готовности всех сервисов..."
        sleep 60
        
        ./gradlew e2eTest -Pprofile="$PROFILE" --continue | tee -a "$LOG_FILE"
        
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        log_success "E2E тесты завершены за ${duration} секунд"
        echo "E2E_TESTS_DURATION=$duration" >> "$REPORT_FILE.tmp"
        
        # Остановка всех сервисов
        log_info "Остановка всех сервисов..."
        docker-compose -f docker-compose.yml down
    else
        log_info "E2E тесты пропущены"
    fi
}

# Запуск нагрузочных тестов
run_load_tests() {
    if [ "$RUN_LOAD_TESTS" = true ]; then
        log_info "Запуск нагрузочных тестов..."
        
        local start_time=$(date +%s)
        
        # Запуск минимальной конфигурации для нагрузочных тестов
        log_info "Запуск конфигурации для нагрузочных тестов..."
        docker-compose -f docker-compose.load.yml up -d
        
        # Ожидание готовности
        sleep 30
        
        ./gradlew loadTest -Pprofile="load-test" --continue | tee -a "$LOG_FILE"
        
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        log_success "Нагрузочные тесты завершены за ${duration} секунд"
        echo "LOAD_TESTS_DURATION=$duration" >> "$REPORT_FILE.tmp"
        
        # Остановка сервисов
        docker-compose -f docker-compose.load.yml down
    else
        log_info "Нагрузочные тесты пропущены"
    fi
}

# Сбор результатов тестов
collect_test_results() {
    log_info "Сбор результатов тестов..."
    
    local total_tests=0
    local passed_tests=0
    local failed_tests=0
    local skipped_tests=0
    
    # Сбор результатов из XML отчетов
    find . -name "TEST-*.xml" -type f | while read -r file; do
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
            UNIT_TESTS_DURATION=0
            INTEGRATION_TESTS_DURATION=0
            E2E_TESTS_DURATION=0
            LOAD_TESTS_DURATION=0
        fi
        
        cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Отчет о тестировании DN Quest</title>
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
        <h1>Отчет о тестировании DN Quest</h1>
        <p>Дата и время: $(date)</p>
        <p>Профиль: $PROFILE</p>
    </div>
    
    <div class="section">
        <h2>Общая статистика</h2>
        <table>
            <tr><th>Метрика</th><th>Значение</th></tr>
            <tr><td>Всего тестов</td><td>$TOTAL_TESTS</td></tr>
            <tr><td>Пройдено</td><td class="success">$PASSED_TESTS</td></tr>
            <tr><td>Провалено</td><td class="failure">$FAILED_TESTS</td></tr>
            <tr><td>Пропущено</td><td class="warning">$SKIPPED_TESTS</td></tr>
        </table>
        
        <h3>Прогресс выполнения</h3>
        <div class="progress-bar">
            <div class="progress" style="width: $(( PASSED_TESTS * 100 / TOTAL_TESTS ))%"></div>
        </div>
        <p>$(( PASSED_TESTS * 100 / TOTAL_TESTS ))% тестов пройдено</p>
    </div>
    
    <div class="section">
        <h2>Время выполнения</h2>
        <table>
            <tr><th>Тип тестов</th><th>Время (сек)</th></tr>
            <tr><td>Unit тесты</td><td>$UNIT_TESTS_DURATION</td></tr>
            <tr><td>Интеграционные тесты</td><td>$INTEGRATION_TESTS_DURATION</td></tr>
            <tr><td>E2E тесты</td><td>$E2E_TESTS_DURATION</td></tr>
            <tr><td>Нагрузочные тесты</td><td>$LOAD_TESTS_DURATION</td></tr>
        </table>
    </div>
    
    <div class="section">
        <h2>Детальная информация</h2>
        <p>Полный лог выполнения: <a href="$LOG_FILE">$LOG_FILE</a></p>
        <p>XML отчеты: $(find . -name "TEST-*.xml" | wc -l) файлов</p>
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
    log_info "Очистка временных файлов..."
    rm -f "$REPORT_FILE.tmp"
    
    # Остановка всех контейнеров
    docker-compose -f docker-compose.yml down 2>/dev/null || true
    docker-compose -f docker-compose.test.yml down 2>/dev/null || true
    docker-compose -f docker-compose.load.yml down 2>/dev/null || true
}

# Обработка сигналов
trap cleanup EXIT INT TERM

# Парсинг аргументов
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-unit)
            RUN_UNIT_TESTS=false
            shift
            ;;
        --skip-integration)
            RUN_INTEGRATION_TESTS=false
            shift
            ;;
        --skip-e2e)
            RUN_E2E_TESTS=false
            shift
            ;;
        --load-tests)
            RUN_LOAD_TESTS=true
            shift
            ;;
        --no-parallel)
            PARALLEL=false
            shift
            ;;
        --no-report)
            GENERATE_REPORT=false
            shift
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --profile)
            PROFILE="$2"
            shift 2
            ;;
        --help)
            echo "Использование: $0 [options]"
            echo "Options:"
            echo "  --skip-unit        Пропустить unit тесты"
            echo "  --skip-integration Пропустить интеграционные тесты"
            echo "  --skip-e2e         Пропустить E2E тесты"
            echo "  --load-tests       Запустить нагрузочные тесты"
            echo "  --no-parallel      Запускать тесты последовательно"
            echo "  --no-report        Не генерировать HTML отчет"
            echo "  --skip-build       Пропустить сборку проекта"
            echo "  --profile PROFILE  Использовать указанный профиль (default: test)"
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
    log_info "Начало выполнения всех тестов DN Quest"
    log_info "Лог выполнения: $LOG_FILE"
    
    create_directories
    check_dependencies
    build_project
    
    run_unit_tests
    run_integration_tests
    run_e2e_tests
    run_load_tests
    
    collect_test_results
    generate_html_report
    
    log_success "Все тесты завершены успешно!"
    log_info "Отчет доступен: $REPORT_FILE"
}

main