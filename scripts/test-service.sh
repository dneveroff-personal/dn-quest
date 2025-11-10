#!/bin/bash

# Скрипт для тестирования конкретного микросервиса
# Использование: ./scripts/test-service.sh [имя_сервиса] [тип_тестов]

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
SERVICE_NAME=${1:-"authentication-service"}
TEST_TYPE=${2:-"all"}

# Базовые переменные
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVICE_DIR="$PROJECT_ROOT/$SERVICE_NAME"
REPORTS_DIR="$PROJECT_ROOT/test-reports/$SERVICE_NAME"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Проверка существования сервиса
if [ ! -d "$SERVICE_DIR" ]; then
    log_error "Сервис $SERVICE_NAME не найден в директории $SERVICE_DIR"
    log_info "Доступные сервисы:"
    find "$PROJECT_ROOT" -maxdepth 2 -name "build.gradle.kts" -type f | grep -v "$PROJECT_ROOT/build.gradle.kts" | while read file; do
        dirname "$(dirname "$file")" | xargs basename
    done
    exit 1
fi

# Создание директории для отчетов
mkdir -p "$REPORTS_DIR"

log_info "Тестирование сервиса: $SERVICE_NAME"
log_info "Тип тестов: $TEST_TYPE"
log_info "Директория отчетов: $REPORTS_DIR"

# Функция для запуска unit тестов сервиса
run_service_unit_tests() {
    log_info "Запуск unit тестов для $SERVICE_NAME..."
    
    cd "$PROJECT_ROOT"
    
    ./gradlew :$SERVICE_NAME:test \
        --continue \
        --info \
        --stacktrace \
        -Dtestreports.dir="$REPORTS_DIR/unit" \
        | tee "$REPORTS_DIR/unit-test-$TIMESTAMP.log"
    
    if [ $? -eq 0 ]; then
        log_success "Unit тесты для $SERVICE_NAME успешно завершены"
    else
        log_error "Unit тесты для $SERVICE_NAME завершились с ошибками"
        return 1
    fi
}

# Функция для запуска интеграционных тестов сервиса
run_service_integration_tests() {
    log_info "Запуск интеграционных тестов для $SERVICE_NAME..."
    
    cd "$PROJECT_ROOT"
    
    ./gradlew :$SERVICE_NAME:test \
        -Dspring.profiles.active=test \
        --continue \
        --info \
        --stacktrace \
        -Dtestreports.dir="$REPORTS_DIR/integration" \
        -DincludeTags="integration" \
        | tee "$REPORTS_DIR/integration-test-$TIMESTAMP.log"
    
    if [ $? -eq 0 ]; then
        log_success "Интеграционные тесты для $SERVICE_NAME успешно завершены"
    else
        log_error "Интеграционные тесты для $SERVICE_NAME завершились с ошибками"
        return 1
    fi
}

# Функция для запуска тестов с покрытием кода
run_service_coverage_tests() {
    log_info "Запуск тестов с покрытием кода для $SERVICE_NAME..."
    
    cd "$PROJECT_ROOT"
    
    ./gradlew :$SERVICE_NAME:test :$SERVICE_NAME:jacocoTestReport \
        -Dspring.profiles.active=test \
        --continue \
        --info \
        --stacktrace \
        | tee "$REPORTS_DIR/coverage-test-$TIMESTAMP.log"
    
    if [ $? -eq 0 ]; then
        log_success "Тесты с покрытием кода для $SERVICE_NAME успешно завершены"
        log_info "Отчет Jacoco доступен в: $SERVICE_DIR/build/reports/jacoco/test/html/index.html"
    else
        log_error "Тесты с покрытием кода для $SERVICE_NAME завершились с ошибками"
        return 1
    fi
}

# Функция для запуска статического анализа кода
run_service_static_analysis() {
    log_info "Запуск статического анализа кода для $SERVICE_NAME..."
    
    cd "$PROJECT_ROOT"
    
    # Запуск Checkstyle
    ./gradlew :$SERVICE_NAME:checkstyleMain :$SERVICE_NAME:checkstyleTest \
        --continue \
        --info \
        | tee "$REPORTS_DIR/checkstyle-$TIMESTAMP.log"
    
    # Запуск SpotBugs
    ./gradlew :$SERVICE_NAME:spotbugsMain :$SERVICE_NAME:spotbugsTest \
        --continue \
        --info \
        | tee -a "$REPORTS_DIR/checkstyle-$TIMESTAMP.log"
    
    if [ $? -eq 0 ]; then
        log_success "Статический анализ кода для $SERVICE_NAME успешно завершен"
    else
        log_warning "Статический анализ кода для $SERVICE_NAME завершился с предупреждениями"
    fi
}

# Функция для запуска тестов производительности
run_service_performance_tests() {
    log_info "Запуск тестов производительности для $SERVICE_NAME..."
    
    cd "$PROJECT_ROOT"
    
    ./gradlew :$SERVICE_NAME:test \
        -Dspring.profiles.active=test \
        --continue \
        --info \
        --stacktrace \
        -Dtestreports.dir="$REPORTS_DIR/performance" \
        -DincludeTags="performance,load" \
        | tee "$REPORTS_DIR/performance-test-$TIMESTAMP.log"
    
    if [ $? -eq 0 ]; then
        log_success "Тесты производительности для $SERVICE_NAME успешно завершены"
    else
        log_error "Тесты производительности для $SERVICE_NAME завершились с ошибками"
        return 1
    fi
}

# Функция для сборки сервиса
build_service() {
    log_info "Сборка сервиса $SERVICE_NAME..."
    
    cd "$PROJECT_ROOT"
    
    ./gradlew :$SERVICE_NAME:build \
        --info \
        --stacktrace \
        | tee "$REPORTS_DIR/build-$TIMESTAMP.log"
    
    if [ $? -eq 0 ]; then
        log_success "Сборка сервиса $SERVICE_NAME успешно завершена"
    else
        log_error "Сборка сервиса $SERVICE_NAME завершилась с ошибками"
        return 1
    fi
}

# Функция для генерации отчета о тестировании сервиса
generate_service_report() {
    log_info "Генерация отчета о тестировании $SERVICE_NAME..."
    
    REPORT_FILE="$REPORTS_DIR/service-report-$TIMESTAMP.md"
    
    cat > "$REPORT_FILE" << EOF
# Отчет о тестировании сервиса: $SERVICE_NAME

**Дата:** $(date)
**Сервис:** $SERVICE_NAME
**Тип тестов:** $TEST_TYPE

## Результаты тестов

EOF
    
    # Добавление результатов unit тестов
    if [ -f "$REPORTS_DIR/unit-test-$TIMESTAMP.log" ]; then
        echo "### Unit тесты" >> "$REPORT_FILE"
        if grep -q "BUILD SUCCESSFUL" "$REPORTS_DIR/unit-test-$TIMESTAMP.log"; then
            echo "✅ Успешно завершены" >> "$REPORT_FILE"
        else
            echo "❌ Завершены с ошибками" >> "$REPORT_FILE"
        fi
        echo "" >> "$REPORT_FILE"
    fi
    
    # Добавление результатов интеграционных тестов
    if [ -f "$REPORTS_DIR/integration-test-$TIMESTAMP.log" ]; then
        echo "### Интеграционные тесты" >> "$REPORT_FILE"
        if grep -q "BUILD SUCCESSFUL" "$REPORTS_DIR/integration-test-$TIMESTAMP.log"; then
            echo "✅ Успешно завершены" >> "$REPORT_FILE"
        else
            echo "❌ Завершены с ошибками" >> "$REPORT_FILE"
        fi
        echo "" >> "$REPORT_FILE"
    fi
    
    # Добавление результатов тестов производительности
    if [ -f "$REPORTS_DIR/performance-test-$TIMESTAMP.log" ]; then
        echo "### Тесты производительности" >> "$REPORT_FILE"
        if grep -q "BUILD SUCCESSFUL" "$REPORTS_DIR/performance-test-$TIMESTAMP.log"; then
            echo "✅ Успешно завершены" >> "$REPORT_FILE"
        else
            echo "❌ Завершены с ошибками" >> "$REPORT_FILE"
        fi
        echo "" >> "$REPORT_FILE"
    fi
    
    cat >> "$REPORT_FILE" << EOF
## Дополнительная информация

- **Логи тестов:** \`$REPORTS_DIR\`
- **Отчеты Jacoco:** \`$SERVICE_DIR/build/reports/jacoco/test/html/index.html\`
- **Отчеты Checkstyle:** \`$SERVICE_DIR/build/reports/checkstyle/\`
- **Отчеты SpotBugs:** \`$SERVICE_DIR/build/reports/spotbugs/\`

EOF
    
    log_success "Отчет о тестировании сгенерирован: $REPORT_FILE"
}

# Функция для проверки зависимостей сервиса
check_service_dependencies() {
    log_info "Проверка зависимостей сервиса $SERVICE_NAME..."
    
    cd "$PROJECT_ROOT"
    
    ./gradlew :$SERVICE_NAME:dependencies \
        --configuration runtimeClasspath \
        | tee "$REPORTS_DIR/dependencies-$TIMESTAMP.log"
    
    if [ $? -eq 0 ]; then
        log_success "Проверка зависимостей успешно завершена"
    else
        log_error "Проверка зависимостей завершилась с ошибками"
        return 1
    fi
}

# Основная логика
main() {
    local exit_code=0
    
    # Проверка зависимостей
    check_service_dependencies || exit_code=1
    
    # Сборка сервиса
    build_service || exit_code=1
    
    case $TEST_TYPE in
        "unit")
            run_service_unit_tests || exit_code=1
            ;;
        "integration")
            run_service_integration_tests || exit_code=1
            ;;
        "coverage")
            run_service_coverage_tests || exit_code=1
            ;;
        "static")
            run_service_static_analysis || exit_code=1
            ;;
        "performance")
            run_service_performance_tests || exit_code=1
            ;;
        "all")
            run_service_unit_tests || exit_code=1
            run_service_integration_tests || exit_code=1
            run_service_coverage_tests || exit_code=1
            run_service_static_analysis || exit_code=1
            ;;
        *)
            log_error "Неизвестный тип тестов: $TEST_TYPE"
            log_info "Доступные типы: unit, integration, coverage, static, performance, all"
            exit 1
            ;;
    esac
    
    # Генерация отчета
    generate_service_report
    
    if [ $exit_code -eq 0 ]; then
        log_success "Все тесты для сервиса $SERVICE_NAME успешно завершены!"
    else
        log_error "Некоторые тесты для сервиса $SERVICE_NAME завершились с ошибками"
    fi
    
    exit $exit_code
}

# Запуск основной функции
main "$@"