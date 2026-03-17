#!/bin/bash

# Скрипт для запуска всех тестов DN Quest
# Использование: ./scripts/run-tests.sh [тип_тестов] [профиль]

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для вывода colored сообщений
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

# Параметры
TEST_TYPE=${1:-"all"}
SPRING_PROFILE=${2:-"test"}

# Базовые переменные
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPORTS_DIR="$PROJECT_ROOT/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Создание директории для отчетов
mkdir -p "$REPORTS_DIR"

log_info "Запуск тестов DN Quest"
log_info "Тип тестов: $TEST_TYPE"
log_info "Профиль Spring: $SPRING_PROFILE"
log_info "Директория отчетов: $REPORTS_DIR"

# Функция для запуска unit тестов
run_unit_tests() {
    log_info "Запуск unit тестов..."
    
    cd "$PROJECT_ROOT"
    
    # Запуск unit тестов для всех модулей
    ./gradlew test \
        -Dspring.profiles.active=$SPRING_PROFILE \
        --continue \
        --info \
        --stacktrace \
        -Dtestreports.dir="$REPORTS_DIR/unit" \
        | tee "$REPORTS_DIR/unit-test.log"
    
    if [ $? -eq 0 ]; then
        log_success "Unit тесты успешно завершены"
    else
        log_error "Unit тесты завершились с ошибками"
        return 1
    fi
}

# Функция для запуска интеграционных тестов
run_integration_tests() {
    log_info "Запуск интеграционных тестов..."
    
    cd "$PROJECT_ROOT"
    
    # Запуск интеграционных тестов с TestContainers
    ./gradlew test \
        -Dspring.profiles.active=$SPRING_PROFILE \
        --continue \
        --info \
        --stacktrace \
        -Dtestreports.dir="$REPORTS_DIR/integration" \
        -DincludeTags="integration" \
        | tee "$REPORTS_DIR/integration-test.log"
    
    if [ $? -eq 0 ]; then
        log_success "Интеграционные тесты успешно завершены"
    else
        log_error "Интеграционные тесты завершились с ошибками"
        return 1
    fi
}

# Функция для запуска end-to-end тестов
run_e2e_tests() {
    log_info "Запуск end-to-end тестов..."
    
    cd "$PROJECT_ROOT"
    
    # Запуск E2E тестов
    ./gradlew test \
        -Dspring.profiles.active=$SPRING_PROFILE \
        --continue \
        --info \
        --stacktrace \
        -Dtestreports.dir="$REPORTS_DIR/e2e" \
        -DincludeTags="e2e" \
        | tee "$REPORTS_DIR/e2e-test.log"
    
    if [ $? -eq 0 ]; then
        log_success "E2E тесты успешно завершены"
    else
        log_error "E2E тесты завершились с ошибками"
        return 1
    fi
}

# Функция для запуска нагрузочных тестов
run_load_tests() {
    log_info "Запуск нагрузочных тестов..."
    
    cd "$PROJECT_ROOT"
    
    # Запуск нагрузочных тестов
    ./gradlew test \
        -Dspring.profiles.active=$SPRING_PROFILE \
        --continue \
        --info \
        --stacktrace \
        -Dtestreports.dir="$REPORTS_DIR/load" \
        -DincludeTags="load" \
        | tee "$REPORTS_DIR/load-test.log"
    
    if [ $? -eq 0 ]; then
        log_success "Нагрузочные тесты успешно завершены"
    else
        log_error "Нагрузочные тесты завершились с ошибками"
        return 1
    fi
}

# Функция для запуска тестов фронтенда
run_frontend_tests() {
    log_info "Запуск тестов фронтенда..."
    
    cd "$PROJECT_ROOT/frontend"
    
    # Установка зависимостей если нужно
    if [ ! -d "node_modules" ]; then
        log_info "Установка зависимостей фронтенда..."
        npm install
    fi
    
    # Запуск unit тестов
    npm run test:unit \
        -- --reporter=junit \
        --outputFile="$REPORTS_DIR/frontend-unit-junit.xml" \
        | tee "$REPORTS_DIR/frontend-unit-test.log"
    
    if [ $? -eq 0 ]; then
        log_success "Unit тесты фронтенда успешно завершены"
    else
        log_error "Unit тесты фронтенда завершились с ошибками"
        return 1
    fi
    
    # Запуск интеграционных тестов
    npm run test:integration \
        -- --reporter=junit \
        --outputFile="$REPORTS_DIR/frontend-integration-junit.xml" \
        | tee "$REPORTS_DIR/frontend-integration-test.log"
    
    if [ $? -eq 0 ]; then
        log_success "Интеграционные тесты фронтенда успешно завершены"
    else
        log_error "Интеграционные тесты фронтенда завершились с ошибками"
        return 1
    fi
}

# Функция для запуска тестов с покрытием кода
run_tests_with_coverage() {
    log_info "Запуск тестов с покрытием кода..."
    
    cd "$PROJECT_ROOT"
    
    # Запуск тестов с Jacoco
    ./gradlew test jacocoTestReport \
        -Dspring.profiles.active=$SPRING_PROFILE \
        --continue \
        --info \
        --stacktrace \
        | tee "$REPORTS_DIR/coverage-test.log"
    
    if [ $? -eq 0 ]; then
        log_success "Тесты с покрытием кода успешно завершены"
        log_info "Отчет Jacoco доступен в: $PROJECT_ROOT/build/reports/jacoco/test/html/index.html"
    else
        log_error "Тесты с покрытием кода завершились с ошибками"
        return 1
    fi
}

# Функция для генерации сводного отчета
generate_summary_report() {
    log_info "Генерация сводного отчета..."
    
    SUMMARY_FILE="$REPORTS_DIR/test-summary-$TIMESTAMP.md"
    
    cat > "$SUMMARY_FILE" << EOF
# Сводный отчет о тестировании DN Quest

**Дата:** $(date)
**Тип тестов:** $TEST_TYPE
**Профиль:** $SPRING_PROFILE

## Результаты тестов

EOF
    
    # Добавление результатов unit тестов
    if [ -f "$REPORTS_DIR/unit-test.log" ]; then
        echo "### Unit тесты" >> "$SUMMARY_FILE"
        if grep -q "BUILD SUCCESSFUL" "$REPORTS_DIR/unit-test.log"; then
            echo "✅ Успешно завершены" >> "$SUMMARY_FILE"
        else
            echo "❌ Завершены с ошибками" >> "$SUMMARY_FILE"
        fi
        echo "" >> "$SUMMARY_FILE"
    fi
    
    # Добавление результатов интеграционных тестов
    if [ -f "$REPORTS_DIR/integration-test.log" ]; then
        echo "### Интеграционные тесты" >> "$SUMMARY_FILE"
        if grep -q "BUILD SUCCESSFUL" "$REPORTS_DIR/integration-test.log"; then
            echo "✅ Успешно завершены" >> "$SUMMARY_FILE"
        else
            echo "❌ Завершены с ошибками" >> "$SUMMARY_FILE"
        fi
        echo "" >> "$SUMMARY_FILE"
    fi
    
    # Добавление результатов E2E тестов
    if [ -f "$REPORTS_DIR/e2e-test.log" ]; then
        echo "### E2E тесты" >> "$SUMMARY_FILE"
        if grep -q "BUILD SUCCESSFUL" "$REPORTS_DIR/e2e-test.log"; then
            echo "✅ Успешно завершены" >> "$SUMMARY_FILE"
        else
            echo "❌ Завершены с ошибками" >> "$SUMMARY_FILE"
        fi
        echo "" >> "$SUMMARY_FILE"
    fi
    
    # Добавление результатов нагрузочных тестов
    if [ -f "$REPORTS_DIR/load-test.log" ]; then
        echo "### Нагрузочные тесты" >> "$SUMMARY_FILE"
        if grep -q "BUILD SUCCESSFUL" "$REPORTS_DIR/load-test.log"; then
            echo "✅ Успешно завершены" >> "$SUMMARY_FILE"
        else
            echo "❌ Завершены с ошибками" >> "$SUMMARY_FILE"
        fi
        echo "" >> "$SUMMARY_FILE"
    fi
    
    # Добавление результатов тестов фронтенда
    if [ -f "$REPORTS_DIR/frontend-unit-test.log" ]; then
        echo "### Тесты фронтенда" >> "$SUMMARY_FILE"
        if grep -q "PASS" "$REPORTS_DIR/frontend-unit-test.log"; then
            echo "✅ Успешно завершены" >> "$SUMMARY_FILE"
        else
            echo "❌ Завершены с ошибками" >> "$SUMMARY_FILE"
        fi
        echo "" >> "$SUMMARY_FILE"
    fi
    
    cat >> "$SUMMARY_FILE" << EOF
## Дополнительная информация

- **Логи тестов:** \`$REPORTS_DIR\`
- **Отчеты Jacoco:** \`$PROJECT_ROOT/build/reports/jacoco/test/html/index.html\`
- **Отчеты фронтенда:** \`$PROJECT_ROOT/frontend/coverage/\`

EOF
    
    log_success "Сводный отчет сгенерирован: $SUMMARY_FILE"
}

# Функция для очистки тестовых данных
cleanup_test_data() {
    log_info "Очистка тестовых данных..."
    
    # Остановка тестовых контейнеров
    docker compose -f docker-compose.test.yml down -v --remove-orphans 2>/dev/null || true
    
    # Очистка временных файлов
    find "$PROJECT_ROOT" -name "*.tmp" -delete 2>/dev/null || true
    find "$PROJECT_ROOT" -name ".test-tmp" -type d -exec rm -rf {} + 2>/dev/null || true
    
    log_success "Очистка тестовых данных завершена"
}

# Основная логика
main() {
    local exit_code=0
    
    # Очистка перед запуском
    cleanup_test_data
    
    case $TEST_TYPE in
        "unit")
            run_unit_tests || exit_code=1
            ;;
        "integration")
            run_integration_tests || exit_code=1
            ;;
        "e2e")
            run_e2e_tests || exit_code=1
            ;;
        "load")
            run_load_tests || exit_code=1
            ;;
        "frontend")
            run_frontend_tests || exit_code=1
            ;;
        "coverage")
            run_tests_with_coverage || exit_code=1
            ;;
        "all")
            run_unit_tests || exit_code=1
            run_integration_tests || exit_code=1
            run_e2e_tests || exit_code=1
            run_frontend_tests || exit_code=1
            ;;
        *)
            log_error "Неизвестный тип тестов: $TEST_TYPE"
            log_info "Доступные типы: unit, integration, e2e, load, frontend, coverage, all"
            exit 1
            ;;
    esac
    
    # Генерация сводного отчета
    generate_summary_report
    
    # Очистка после тестов
    cleanup_test_data
    
    if [ $exit_code -eq 0 ]; then
        log_success "Все тесты успешно завершены!"
    else
        log_error "Некоторые тесты завершились с ошибками"
    fi
    
    exit $exit_code
}

# Обработка сигналов для корректной остановки
trap cleanup_test_data EXIT INT TERM

# Запуск основной функции
main "$@"