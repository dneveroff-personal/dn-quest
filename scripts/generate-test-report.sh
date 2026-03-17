#!/bin/bash

# Скрипт для генерации отчетов о тестировании DN Quest
# Использование: ./scripts/generate-test-report.sh [options]

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Переменные
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPORT_DIR="$PROJECT_ROOT/test-reports"
LOG_DIR="$PROJECT_ROOT/test-logs"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="$REPORT_DIR/comprehensive-test-report-$TIMESTAMP.html"

# Параметры по умолчанию
REPORT_TYPE="comprehensive"
INCLUDE_CHARTS=true
INCLUDE_LOGS=true
INCLUDE_METRICS=true
INCLUDE_TRENDS=false
DAYS_FOR_TRENDS=7
FORMAT="html"

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

# Создание директорий
create_directories() {
    log_info "Создание директории для отчетов..."
    mkdir -p "$REPORT_DIR"
}

# Сбор результатов unit тестов
collect_unit_test_results() {
    log_info "Сбор результатов unit тестов..."
    
    local total_tests=0
    local passed_tests=0
    local failed_tests=0
    local skipped_tests=0
    local total_time=0
    
    # Поиск XML отчетов unit тестов
    find . -name "TEST-*.xml" -not -path "*/integrationTest/*" -not -path "*/e2eTest/*" -not -path "*/loadTest/*" -type f | while read -r file; do
        # Парсинг XML
        local tests=$(grep -o 'tests="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local failures=$(grep -o 'failures="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local errors=$(grep -o 'errors="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local skipped=$(grep -o 'skipped="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local time=$(grep -o 'time="[0-9.]*"' "$file" | grep -o '[0-9.]*' | head -1)
        
        tests=${tests:-0}
        failures=${failures:-0}
        errors=${errors:-0}
        skipped=${skipped:-0}
        time=${time:-0}
        
        total_tests=$((total_tests + tests))
        failed_tests=$((failed_tests + failures + errors))
        skipped_tests=$((skipped_tests + skipped))
        total_time=$(echo "$total_time + $time" | bc -l)
    done
    
    passed_tests=$((total_tests - failed_tests - skipped_tests))
    
    echo "UNIT_TOTAL_TESTS=$total_tests" >> "$REPORT_FILE.tmp"
    echo "UNIT_PASSED_TESTS=$passed_tests" >> "$REPORT_FILE.tmp"
    echo "UNIT_FAILED_TESTS=$failed_tests" >> "$REPORT_FILE.tmp"
    echo "UNIT_SKIPPED_TESTS=$skipped_tests" >> "$REPORT_FILE.tmp"
    echo "UNIT_TOTAL_TIME=$total_time" >> "$REPORT_FILE.tmp"
}

# Сбор результатов интеграционных тестов
collect_integration_test_results() {
    log_info "Сбор результатов интеграционных тестов..."
    
    local total_tests=0
    local passed_tests=0
    local failed_tests=0
    local skipped_tests=0
    local total_time=0
    
    # Поиск XML отчетов интеграционных тестов
    find . -name "*integrationTest*TEST-*.xml" -type f | while read -r file; do
        # Парсинг XML
        local tests=$(grep -o 'tests="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local failures=$(grep -o 'failures="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local errors=$(grep -o 'errors="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local skipped=$(grep -o 'skipped="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local time=$(grep -o 'time="[0-9.]*"' "$file" | grep -o '[0-9.]*' | head -1)
        
        tests=${tests:-0}
        failures=${failures:-0}
        errors=${errors:-0}
        skipped=${skipped:-0}
        time=${time:-0}
        
        total_tests=$((total_tests + tests))
        failed_tests=$((failed_tests + failures + errors))
        skipped_tests=$((skipped_tests + skipped))
        total_time=$(echo "$total_time + $time" | bc -l)
    done
    
    passed_tests=$((total_tests - failed_tests - skipped_tests))
    
    echo "INTEGRATION_TOTAL_TESTS=$total_tests" >> "$REPORT_FILE.tmp"
    echo "INTEGRATION_PASSED_TESTS=$passed_tests" >> "$REPORT_FILE.tmp"
    echo "INTEGRATION_FAILED_TESTS=$failed_tests" >> "$REPORT_FILE.tmp"
    echo "INTEGRATION_SKIPPED_TESTS=$skipped_tests" >> "$REPORT_FILE.tmp"
    echo "INTEGRATION_TOTAL_TIME=$total_time" >> "$REPORT_FILE.tmp"
}

# Сбор результатов E2E тестов
collect_e2e_test_results() {
    log_info "Сбор результатов E2E тестов..."
    
    local total_tests=0
    local passed_tests=0
    local failed_tests=0
    local skipped_tests=0
    local total_time=0
    
    # Поиск XML отчетов E2E тестов
    find . -name "*e2eTest*TEST-*.xml" -type f | while read -r file; do
        # Парсинг XML
        local tests=$(grep -o 'tests="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local failures=$(grep -o 'failures="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local errors=$(grep -o 'errors="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local skipped=$(grep -o 'skipped="[0-9]*"' "$file" | grep -o '[0-9]*' | head -1)
        local time=$(grep -o 'time="[0-9.]*"' "$file" | grep -o '[0-9.]*' | head -1)
        
        tests=${tests:-0}
        failures=${failures:-0}
        errors=${errors:-0}
        skipped=${skipped:-0}
        time=${time:-0}
        
        total_tests=$((total_tests + tests))
        failed_tests=$((failed_tests + failures + errors))
        skipped_tests=$((skipped_tests + skipped))
        total_time=$(echo "$total_time + $time" | bc -l)
    done
    
    passed_tests=$((total_tests - failed_tests - skipped_tests))
    
    echo "E2E_TOTAL_TESTS=$total_tests" >> "$REPORT_FILE.tmp"
    echo "E2E_PASSED_TESTS=$passed_tests" >> "$REPORT_FILE.tmp"
    echo "E2E_FAILED_TESTS=$failed_tests" >> "$REPORT_FILE.tmp"
    echo "E2E_SKIPPED_TESTS=$skipped_tests" >> "$REPORT_FILE.tmp"
    echo "E2E_TOTAL_TIME=$total_time" >> "$REPORT_FILE.tmp"
}

# Сбор результатов нагрузочных тестов
collect_load_test_results() {
    log_info "Сбор результатов нагрузочных тестов..."
    
    local total_requests=0
    local successful_requests=0
    local failed_requests=0
    local avg_response_time=0
    local max_response_time=0
    local min_response_time=999999
    local throughput=0
    
    # Поиск JSON отчетов нагрузочных тестов
    find . -name "*loadTest*results.json" -type f | while read -r file; do
        if command -v jq &> /dev/null; then
            local requests=$(jq -r '.totalRequests // 0' "$file")
            local successful=$(jq -r '.successfulRequests // 0' "$file")
            local failed=$(jq -r '.failedRequests // 0' "$file")
            local avg_time=$(jq -r '.averageResponseTime // 0' "$file")
            local max_time=$(jq -r '.maxResponseTime // 0' "$file")
            local min_time=$(jq -r '.minResponseTime // 999999' "$file")
            local test_throughput=$(jq -r '.throughput // 0' "$file")
            
            total_requests=$((total_requests + requests))
            successful_requests=$((successful_requests + successful))
            failed_requests=$((failed_requests + failed))
            
            if [ "$avg_time" -gt "$avg_response_time" ]; then
                avg_response_time=$avg_time
            fi
            
            if [ "$max_time" -gt "$max_response_time" ]; then
                max_response_time=$max_time
            fi
            
            if [ "$min_time" -lt "$min_response_time" ]; then
                min_response_time=$min_time
            fi
            
            if [ "$test_throughput" -gt "$throughput" ]; then
                throughput=$test_throughput
            fi
        fi
    done
    
    echo "LOAD_TOTAL_REQUESTS=$total_requests" >> "$REPORT_FILE.tmp"
    echo "LOAD_SUCCESSFUL_REQUESTS=$successful_requests" >> "$REPORT_FILE.tmp"
    echo "LOAD_FAILED_REQUESTS=$failed_requests" >> "$REPORT_FILE.tmp"
    echo "LOAD_AVG_RESPONSE_TIME=$avg_response_time" >> "$REPORT_FILE.tmp"
    echo "LOAD_MAX_RESPONSE_TIME=$max_response_time" >> "$REPORT_FILE.tmp"
    echo "LOAD_MIN_RESPONSE_TIME=$min_response_time" >> "$REPORT_FILE.tmp"
    echo "LOAD_THROUGHPUT=$throughput" >> "$REPORT_FILE.tmp"
}

# Сбор метрик покрытия кода
collect_coverage_metrics() {
    log_info "Сбор метрик покрытия кода..."
    
    local instruction_coverage=0
    local branch_coverage=0
    local line_coverage=0
    local complexity_coverage=0
    
    # Поиск отчетов JaCoCo
    find . -name "jacoco.xml" -type f | while read -r file; do
        if command -v xmllint &> /dev/null; then
            local instruction=$(xmllint --xpath "string(/report/counter[@type='INSTRUCTION']/@covered)" "$file" 2>/dev/null || echo "0")
            local instruction_total=$(xmllint --xpath "string(/report/counter[@type='INSTRUCTION']/@missed)" "$file" 2>/dev/null || echo "0")
            instruction_total=$((instruction + instruction_total))
            
            if [ "$instruction_total" -gt 0 ]; then
                instruction_coverage=$((instruction * 100 / instruction_total))
            fi
            
            local branch=$(xmllint --xpath "string(/report/counter[@type='BRANCH']/@covered)" "$file" 2>/dev/null || echo "0")
            local branch_total=$(xmllint --xpath "string(/report/counter[@type='BRANCH']/@missed)" "$file" 2>/dev/null || echo "0")
            branch_total=$((branch + branch_total))
            
            if [ "$branch_total" -gt 0 ]; then
                branch_coverage=$((branch * 100 / branch_total))
            fi
            
            local line=$(xmllint --xpath "string(/report/counter[@type='LINE']/@covered)" "$file" 2>/dev/null || echo "0")
            local line_total=$(xmllint --xpath "string(/report/counter[@type='LINE']/@missed)" "$file" 2>/dev/null || echo "0")
            line_total=$((line + line_total))
            
            if [ "$line_total" -gt 0 ]; then
                line_coverage=$((line * 100 / line_total))
            fi
            
            local complexity=$(xmllint --xpath "string(/report/counter[@type='COMPLEXITY']/@covered)" "$file" 2>/dev/null || echo "0")
            local complexity_total=$(xmllint --xpath "string(/report/counter[@type='COMPLEXITY']/@missed)" "$file" 2>/dev/null || echo "0")
            complexity_total=$((complexity + complexity_total))
            
            if [ "$complexity_total" -gt 0 ]; then
                complexity_coverage=$((complexity * 100 / complexity_total))
            fi
        fi
    done
    
    echo "COVERAGE_INSTRUCTION=$instruction_coverage" >> "$REPORT_FILE.tmp"
    echo "COVERAGE_BRANCH=$branch_coverage" >> "$REPORT_FILE.tmp"
    echo "COVERAGE_LINE=$line_coverage" >> "$REPORT_FILE.tmp"
    echo "COVERAGE_COMPLEXITY=$complexity_coverage" >> "$REPORT_FILE.tmp"
}

# Сбор метрик качества кода
collect_quality_metrics() {
    log_info "Сбор метрик качества кода..."
    
    local total_classes=0
    local total_lines=0
    local duplicated_lines=0
    local technical_debt=0
    local code_smells=0
    local vulnerabilities=0
    
    # Подсчет классов и строк кода
    find src -name "*.java" -type f | while read -r file; do
        local lines=$(wc -l < "$file")
        total_lines=$((total_lines + lines))
        
        if grep -q "class " "$file"; then
            total_classes=$((total_classes + 1))
        fi
    done
    
    # Поиск отчетов SonarQube (если доступны)
    find . -name "sonar-report.json" -type f | while read -r file; do
        if command -v jq &> /dev/null; then
            local duplicated=$(jq -r '.component.measures[] | select(.metric=="duplicated_lines") | .value' "$file" 2>/dev/null || echo "0")
            local debt=$(jq -r '.component.measures[] | select(.metric=="sqale_index") | .value' "$file" 2>/dev/null || echo "0")
            local smells=$(jq -r '.component.measures[] | select(.metric=="code_smells") | .value' "$file" 2>/dev/null || echo "0")
            local vulns=$(jq -r '.component.measures[] | select(.metric=="vulnerabilities") | .value' "$file" 2>/dev/null || echo "0")
            
            duplicated_lines=$duplicated
            technical_debt=$debt
            code_smells=$smells
            vulnerabilities=$vulns
        fi
    done
    
    echo "QUALITY_TOTAL_CLASSES=$total_classes" >> "$REPORT_FILE.tmp"
    echo "QUALITY_TOTAL_LINES=$total_lines" >> "$REPORT_FILE.tmp"
    echo "QUALITY_DUPLICATED_LINES=$duplicated_lines" >> "$REPORT_FILE.tmp"
    echo "QUALITY_TECHNICAL_DEBT=$technical_debt" >> "$REPORT_FILE.tmp"
    echo "QUALITY_CODE_SMELLS=$code_smells" >> "$REPORT_FILE.tmp"
    echo "QUALITY_VULNERABILITIES=$vulnerabilities" >> "$REPORT_FILE.tmp"
}

# Генерация HTML отчета
generate_html_report() {
    log_info "Генерация HTML отчета..."
    
    # Чтение временного файла с результатами
    if [ -f "$REPORT_FILE.tmp" ]; then
        source "$REPORT_FILE.tmp"
    else
        # Значения по умолчанию
        UNIT_TOTAL_TESTS=0
        UNIT_PASSED_TESTS=0
        UNIT_FAILED_TESTS=0
        UNIT_SKIPPED_TESTS=0
        UNIT_TOTAL_TIME=0
        
        INTEGRATION_TOTAL_TESTS=0
        INTEGRATION_PASSED_TESTS=0
        INTEGRATION_FAILED_TESTS=0
        INTEGRATION_SKIPPED_TESTS=0
        INTEGRATION_TOTAL_TIME=0
        
        E2E_TOTAL_TESTS=0
        E2E_PASSED_TESTS=0
        E2E_FAILED_TESTS=0
        E2E_SKIPPED_TESTS=0
        E2E_TOTAL_TIME=0
        
        LOAD_TOTAL_REQUESTS=0
        LOAD_SUCCESSFUL_REQUESTS=0
        LOAD_FAILED_REQUESTS=0
        LOAD_AVG_RESPONSE_TIME=0
        LOAD_MAX_RESPONSE_TIME=0
        LOAD_MIN_RESPONSE_TIME=0
        LOAD_THROUGHPUT=0
        
        COVERAGE_INSTRUCTION=0
        COVERAGE_BRANCH=0
        COVERAGE_LINE=0
        COVERAGE_COMPLEXITY=0
        
        QUALITY_TOTAL_CLASSES=0
        QUALITY_TOTAL_LINES=0
        QUALITY_DUPLICATED_LINES=0
        QUALITY_TECHNICAL_DEBT=0
        QUALITY_CODE_SMELLS=0
        QUALITY_VULNERABILITIES=0
    fi
    
    # Расчет общих метрик
    local total_tests=$((UNIT_TOTAL_TESTS + INTEGRATION_TOTAL_TESTS + E2E_TOTAL_TESTS))
    local total_passed=$((UNIT_PASSED_TESTS + INTEGRATION_PASSED_TESTS + E2E_PASSED_TESTS))
    local total_failed=$((UNIT_FAILED_TESTS + INTEGRATION_FAILED_TESTS + E2E_FAILED_TESTS))
    local total_skipped=$((UNIT_SKIPPED_TESTS + INTEGRATION_SKIPPED_TESTS + E2E_SKIPPED_TESTS))
    local total_time=$(echo "$UNIT_TOTAL_TIME + $INTEGRATION_TOTAL_TIME + $E2E_TOTAL_TIME" | bc -l)
    
    local overall_success_rate=0
    if [ "$total_tests" -gt 0 ]; then
        overall_success_rate=$((total_passed * 100 / total_tests))
    fi
    
    cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Комплексный отчет о тестировании DN Quest</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 8px; margin-bottom: 30px; text-align: center; }
        .header h1 { margin: 0; font-size: 2.5em; }
        .header p { margin: 10px 0 0 0; opacity: 0.9; }
        .section { margin: 30px 0; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; }
        .section h2 { color: #333; margin-top: 0; border-bottom: 2px solid #667eea; padding-bottom: 10px; }
        .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 20px 0; }
        .metric-card { background: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; border-left: 4px solid #667eea; }
        .metric-value { font-size: 2em; font-weight: bold; color: #333; }
        .metric-label { color: #666; margin-top: 5px; }
        .success { color: #28a745; }
        .warning { color: #ffc107; }
        .danger { color: #dc3545; }
        .progress-bar { width: 100%; background-color: #e9ecef; border-radius: 4px; overflow: hidden; margin: 10px 0; }
        .progress { height: 20px; background: linear-gradient(90deg, #28a745, #20c997); transition: width 0.3s ease; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f8f9fa; font-weight: bold; }
        .chart-container { position: relative; height: 400px; margin: 20px 0; }
        .tabs { display: flex; border-bottom: 1px solid #ddd; margin-bottom: 20px; }
        .tab { padding: 10px 20px; cursor: pointer; border: none; background: none; border-bottom: 2px solid transparent; }
        .tab.active { border-bottom-color: #667eea; color: #667eea; }
        .tab-content { display: none; }
        .tab-content.active { display: block; }
        .log-section { background: #f8f9fa; padding: 15px; border-radius: 4px; font-family: monospace; font-size: 0.9em; max-height: 300px; overflow-y: auto; }
        .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }
        .summary-card { background: white; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; }
        .summary-card h3 { margin-top: 0; color: #667eea; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Комплексный отчет о тестировании DN Quest</h1>
            <p>Дата и время: $(date)</p>
            <p>Версия: $(git describe --tags --always 2>/dev/null || echo "N/A")</p>
        </div>
        
        <div class="section">
            <h2>Общая статистика</h2>
            <div class="metrics-grid">
                <div class="metric-card">
                    <div class="metric-value">$total_tests</div>
                    <div class="metric-label">Всего тестов</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value success">$total_passed</div>
                    <div class="metric-label">Пройдено</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value danger">$total_failed</div>
                    <div class="metric-label">Провалено</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value warning">$total_skipped</div>
                    <div class="metric-label">Пропущено</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value">${overall_success_rate}%</div>
                    <div class="metric-label">Успешность</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value">$(printf "%.1f" "$total_time")</div>
                    <div class="metric-label">Общее время (сек)</div>
                </div>
            </div>
            
            <div class="progress-bar">
                <div class="progress" style="width: ${overall_success_rate}%"></div>
            </div>
            <p>Общий прогресс выполнения тестов: ${overall_success_rate}%</p>
        </div>
        
        <div class="tabs">
            <button class="tab active" onclick="showTab('unit-tests')">Unit тесты</button>
            <button class="tab" onclick="showTab('integration-tests')">Интеграционные тесты</button>
            <button class="tab" onclick="showTab('e2e-tests')">E2E тесты</button>
            <button class="tab" onclick="showTab('load-tests')">Нагрузочные тесты</button>
            <button class="tab" onclick="showTab('coverage')">Покрытие кода</button>
            <button class="tab" onclick="showTab('quality')">Качество кода</button>
        </div>
        
        <div id="unit-tests" class="tab-content active">
            <div class="section">
                <h2>Unit тесты</h2>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-value">$UNIT_TOTAL_TESTS</div>
                        <div class="metric-label">Всего тестов</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value success">$UNIT_PASSED_TESTS</div>
                        <div class="metric-label">Пройдено</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value danger">$UNIT_FAILED_TESTS</div>
                        <div class="metric-label">Провалено</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">$(printf "%.2f" "$UNIT_TOTAL_TIME")</div>
                        <div class="metric-label">Время (сек)</div>
                    </div>
                </div>
                <div class="chart-container">
                    <canvas id="unitTestsChart"></canvas>
                </div>
            </div>
        </div>
        
        <div id="integration-tests" class="tab-content">
            <div class="section">
                <h2>Интеграционные тесты</h2>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-value">$INTEGRATION_TOTAL_TESTS</div>
                        <div class="metric-label">Всего тестов</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value success">$INTEGRATION_PASSED_TESTS</div>
                        <div class="metric-label">Пройдено</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value danger">$INTEGRATION_FAILED_TESTS</div>
                        <div class="metric-label">Провалено</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">$(printf "%.2f" "$INTEGRATION_TOTAL_TIME")</div>
                        <div class="metric-label">Время (сек)</div>
                    </div>
                </div>
                <div class="chart-container">
                    <canvas id="integrationTestsChart"></canvas>
                </div>
            </div>
        </div>
        
        <div id="e2e-tests" class="tab-content">
            <div class="section">
                <h2>E2E тесты</h2>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-value">$E2E_TOTAL_TESTS</div>
                        <div class="metric-label">Всего тестов</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value success">$E2E_PASSED_TESTS</div>
                        <div class="metric-label">Пройдено</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value danger">$E2E_FAILED_TESTS</div>
                        <div class="metric-label">Провалено</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">$(printf "%.2f" "$E2E_TOTAL_TIME")</div>
                        <div class="metric-label">Время (сек)</div>
                    </div>
                </div>
                <div class="chart-container">
                    <canvas id="e2eTestsChart"></canvas>
                </div>
            </div>
        </div>
        
        <div id="load-tests" class="tab-content">
            <div class="section">
                <h2>Нагрузочные тесты</h2>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-value">$LOAD_TOTAL_REQUESTS</div>
                        <div class="metric-label">Всего запросов</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value success">$LOAD_SUCCESSFUL_REQUESTS</div>
                        <div class="metric-label">Успешных</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value danger">$LOAD_FAILED_REQUESTS</div>
                        <div class="metric-label">Провалено</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">$LOAD_THROUGHPUT</div>
                        <div class="metric-label">Пропускная способность (req/s)</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">${LOAD_AVG_RESPONSE_TIME}ms</div>
                        <div class="metric-label">Среднее время ответа</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">${LOAD_MAX_RESPONSE_TIME}ms</div>
                        <div class="metric-label">Макс. время ответа</div>
                    </div>
                </div>
                <div class="chart-container">
                    <canvas id="loadTestsChart"></canvas>
                </div>
            </div>
        </div>
        
        <div id="coverage" class="tab-content">
            <div class="section">
                <h2>Покрытие кода</h2>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-value">${COVERAGE_INSTRUCTION}%</div>
                        <div class="metric-label">Покрытие инструкций</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">${COVERAGE_BRANCH}%</div>
                        <div class="metric-label">Покрытие ветвлений</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">${COVERAGE_LINE}%</div>
                        <div class="metric-label">Покрытие строк</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">${COVERAGE_COMPLEXITY}%</div>
                        <div class="metric-label">Покрытие сложности</div>
                    </div>
                </div>
                <div class="chart-container">
                    <canvas id="coverageChart"></canvas>
                </div>
            </div>
        </div>
        
        <div id="quality" class="tab-content">
            <div class="section">
                <h2>Качество кода</h2>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-value">$QUALITY_TOTAL_CLASSES</div>
                        <div class="metric-label">Всего классов</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">$QUALITY_TOTAL_LINES</div>
                        <div class="metric-label">Всего строк кода</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value warning">$QUALITY_DUPLICATED_LINES</div>
                        <div class="metric-label">Дублированных строк</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value warning">$QUALITY_TECHNICAL_DEBT</div>
                        <div class="metric-label">Технический долг</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value danger">$QUALITY_CODE_SMELLS</div>
                        <div class="metric-label">Code smells</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value danger">$QUALITY_VULNERABILITIES</div>
                        <div class="metric-label">Уязвимости</div>
                    </div>
                </div>
                <div class="chart-container">
                    <canvas id="qualityChart"></canvas>
                </div>
            </div>
        </div>
        
        <div class="section">
            <h2>Детальная информация</h2>
            <div class="summary-grid">
                <div class="summary-card">
                    <h3>XML отчеты</h3>
                    <p>Unit тесты: $(find . -name "TEST-*.xml" -not -path "*/integrationTest/*" -not -path "*/e2eTest/*" -not -path "*/loadTest/*" | wc -l) файлов</p>
                    <p>Интеграционные тесты: $(find . -name "*integrationTest*TEST-*.xml" | wc -l) файлов</p>
                    <p>E2E тесты: $(find . -name "*e2eTest*TEST-*.xml" | wc -l) файлов</p>
                </div>
                <div class="summary-card">
                    <h3>Логи выполнения</h3>
                    <p>Логи тестов: $LOG_DIR</p>
                    <p>Последний отчет: $(ls -t "$REPORT_DIR" | head -1)</p>
                </div>
                <div class="summary-card">
                    <h3>Репозиторий</h3>
                    <p>Ветка: $(git branch --show-current 2>/dev/null || echo "N/A")</p>
                    <p>Коммит: $(git rev-parse --short HEAD 2>/dev/null || echo "N/A")</p>
                    <p>Статус: $(git status --porcelain 2>/dev/null | wc -l) измененных файлов</p>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        function showTab(tabId) {
            // Скрыть все вкладки
            const tabContents = document.querySelectorAll('.tab-content');
            tabContents.forEach(content => content.classList.remove('active'));
            
            // Убрать активный класс со всех кнопок
            const tabs = document.querySelectorAll('.tab');
            tabs.forEach(tab => tab.classList.remove('active'));
            
            // Показать выбранную вкладку
            document.getElementById(tabId).classList.add('active');
            event.target.classList.add('active');
        }
        
        // График Unit тестов
        const unitTestsCtx = document.getElementById('unitTestsChart').getContext('2d');
        new Chart(unitTestsCtx, {
            type: 'doughnut',
            data: {
                labels: ['Пройдено', 'Провалено', 'Пропущено'],
                datasets: [{
                    data: [$UNIT_PASSED_TESTS, $UNIT_FAILED_TESTS, $UNIT_SKIPPED_TESTS],
                    backgroundColor: ['#28a745', '#dc3545', '#ffc107']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Распределение результатов Unit тестов'
                    }
                }
            }
        });
        
        // График интеграционных тестов
        const integrationTestsCtx = document.getElementById('integrationTestsChart').getContext('2d');
        new Chart(integrationTestsCtx, {
            type: 'doughnut',
            data: {
                labels: ['Пройдено', 'Провалено', 'Пропущено'],
                datasets: [{
                    data: [$INTEGRATION_PASSED_TESTS, $INTEGRATION_FAILED_TESTS, $INTEGRATION_SKIPPED_TESTS],
                    backgroundColor: ['#28a745', '#dc3545', '#ffc107']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Распределение результатов интеграционных тестов'
                    }
                }
            }
        });
        
        // График E2E тестов
        const e2eTestsCtx = document.getElementById('e2eTestsChart').getContext('2d');
        new Chart(e2eTestsCtx, {
            type: 'doughnut',
            data: {
                labels: ['Пройдено', 'Провалено', 'Пропущено'],
                datasets: [{
                    data: [$E2E_PASSED_TESTS, $E2E_FAILED_TESTS, $E2E_SKIPPED_TESTS],
                    backgroundColor: ['#28a745', '#dc3545', '#ffc107']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Распределение результатов E2E тестов'
                    }
                }
            }
        });
        
        // График нагрузочных тестов
        const loadTestsCtx = document.getElementById('loadTestsChart').getContext('2d');
        new Chart(loadTestsCtx, {
            type: 'bar',
            data: {
                labels: ['Успешные', 'Проваленные'],
                datasets: [{
                    label: 'Количество запросов',
                    data: [$LOAD_SUCCESSFUL_REQUESTS, $LOAD_FAILED_REQUESTS],
                    backgroundColor: ['#28a745', '#dc3545']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Результаты нагрузочных тестов'
                    }
                }
            }
        });
        
        // График покрытия кода
        const coverageCtx = document.getElementById('coverageChart').getContext('2d');
        new Chart(coverageCtx, {
            type: 'radar',
            data: {
                labels: ['Инструкции', 'Ветвления', 'Строки', 'Сложность'],
                datasets: [{
                    label: 'Покрытие (%)',
                    data: [$COVERAGE_INSTRUCTION, $COVERAGE_BRANCH, $COVERAGE_LINE, $COVERAGE_COMPLEXITY],
                    backgroundColor: 'rgba(102, 126, 234, 0.2)',
                    borderColor: 'rgba(102, 126, 234, 1)',
                    pointBackgroundColor: 'rgba(102, 126, 234, 1)'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    r: {
                        beginAtZero: true,
                        max: 100
                    }
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Метрики покрытия кода'
                    }
                }
            }
        });
        
        // График качества кода
        const qualityCtx = document.getElementById('qualityChart').getContext('2d');
        new Chart(qualityCtx, {
            type: 'bar',
            data: {
                labels: ['Дублированные строки', 'Code smells', 'Уязвимости'],
                datasets: [{
                    label: 'Количество',
                    data: [$QUALITY_DUPLICATED_LINES, $QUALITY_CODE_SMELLS, $QUALITY_VULNERABILITIES],
                    backgroundColor: ['#ffc107', '#fd7e14', '#dc3545']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Метрики качества кода'
                    }
                }
            }
        });
    </script>
</body>
</html>
EOF
    
    log_success "HTML отчет сгенерирован: $REPORT_FILE"
    rm -f "$REPORT_FILE.tmp"
}

# Генерация JSON отчета
generate_json_report() {
    if [ "$FORMAT" = "json" ] || [ "$FORMAT" = "both" ]; then
        log_info "Генерация JSON отчета..."
        
        local json_report_file="${REPORT_FILE%.html}.json"
        
        if [ -f "$REPORT_FILE.tmp" ]; then
            source "$REPORT_FILE.tmp"
        fi
        
        cat > "$json_report_file" << EOF
{
    "timestamp": "$(date -Iseconds)",
    "project": "DN Quest",
    "version": "$(git describe --tags --always 2>/dev/null || echo "N/A")",
    "branch": "$(git branch --show-current 2>/dev/null || echo "N/A")",
    "commit": "$(git rev-parse --short HEAD 2>/dev/null || echo "N/A")",
    "summary": {
        "total_tests": $((UNIT_TOTAL_TESTS + INTEGRATION_TOTAL_TESTS + E2E_TOTAL_TESTS)),
        "total_passed": $((UNIT_PASSED_TESTS + INTEGRATION_PASSED_TESTS + E2E_PASSED_TESTS)),
        "total_failed": $((UNIT_FAILED_TESTS + INTEGRATION_FAILED_TESTS + E2E_FAILED_TESTS)),
        "total_skipped": $((UNIT_SKIPPED_TESTS + INTEGRATION_SKIPPED_TESTS + E2E_SKIPPED_TESTS)),
        "total_time": $(echo "$UNIT_TOTAL_TIME + $INTEGRATION_TOTAL_TIME + $E2E_TOTAL_TIME" | bc -l)
    },
    "unit_tests": {
        "total": $UNIT_TOTAL_TESTS,
        "passed": $UNIT_PASSED_TESTS,
        "failed": $UNIT_FAILED_TESTS,
        "skipped": $UNIT_SKIPPED_TESTS,
        "time": $UNIT_TOTAL_TIME
    },
    "integration_tests": {
        "total": $INTEGRATION_TOTAL_TESTS,
        "passed": $INTEGRATION_PASSED_TESTS,
        "failed": $INTEGRATION_FAILED_TESTS,
        "skipped": $INTEGRATION_SKIPPED_TESTS,
        "time": $INTEGRATION_TOTAL_TIME
    },
    "e2e_tests": {
        "total": $E2E_TOTAL_TESTS,
        "passed": $E2E_PASSED_TESTS,
        "failed": $E2E_FAILED_TESTS,
        "skipped": $E2E_SKIPPED_TESTS,
        "time": $E2E_TOTAL_TIME
    },
    "load_tests": {
        "total_requests": $LOAD_TOTAL_REQUESTS,
        "successful_requests": $LOAD_SUCCESSFUL_REQUESTS,
        "failed_requests": $LOAD_FAILED_REQUESTS,
        "average_response_time": $LOAD_AVG_RESPONSE_TIME,
        "max_response_time": $LOAD_MAX_RESPONSE_TIME,
        "min_response_time": $LOAD_MIN_RESPONSE_TIME,
        "throughput": $LOAD_THROUGHPUT
    },
    "coverage": {
        "instruction": $COVERAGE_INSTRUCTION,
        "branch": $COVERAGE_BRANCH,
        "line": $COVERAGE_LINE,
        "complexity": $COVERAGE_COMPLEXITY
    },
    "quality": {
        "total_classes": $QUALITY_TOTAL_CLASSES,
        "total_lines": $QUALITY_TOTAL_LINES,
        "duplicated_lines": $QUALITY_DUPLICATED_LINES,
        "technical_debt": $QUALITY_TECHNICAL_DEBT,
        "code_smells": $QUALITY_CODE_SMELLS,
        "vulnerabilities": $QUALITY_VULNERABILITIES
    }
}
EOF
        
        log_success "JSON отчет сгенерирован: $json_report_file"
    fi
}

# Очистка
cleanup() {
    rm -f "$REPORT_FILE.tmp"
}

# Обработка сигналов
trap cleanup EXIT

# Парсинг аргументов
while [[ $# -gt 0 ]]; do
    case $1 in
        --type)
            REPORT_TYPE="$2"
            shift 2
            ;;
        --no-charts)
            INCLUDE_CHARTS=false
            shift
            ;;
        --no-logs)
            INCLUDE_LOGS=false
            shift
            ;;
        --no-metrics)
            INCLUDE_METRICS=false
            shift
            ;;
        --trends)
            INCLUDE_TRENDS=true
            shift
            ;;
        --days)
            DAYS_FOR_TRENDS="$2"
            shift 2
            ;;
        --format)
            FORMAT="$2"
            shift 2
            ;;
        --output)
            REPORT_FILE="$2"
            shift 2
            ;;
        --help)
            echo "Использование: $0 [options]"
            echo "Options:"
            echo "  --type TYPE          Тип отчета (comprehensive, summary, default: comprehensive)"
            echo "  --no-charts          Не включать графики"
            echo "  --no-logs            Не включать логи"
            echo "  --no-metrics         Не включать метрики"
            echo "  --trends             Включить тренды"
            echo "  --days DAYS          Количество дней для трендов (default: 7)"
            echo "  --format FORMAT      Формат отчета (html, json, both, default: html)"
            echo "  --output FILE        Путь к файлу отчета"
            echo "  --help               Показать эту справку"
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
    log_info "Генерация отчета о тестировании DN Quest"
    
    create_directories
    
    if [ "$INCLUDE_METRICS" = true ]; then
        collect_unit_test_results
        collect_integration_test_results
        collect_e2e_test_results
        collect_load_test_results
        collect_coverage_metrics
        collect_quality_metrics
    fi
    
    if [ "$FORMAT" = "html" ] || [ "$FORMAT" = "both" ]; then
        generate_html_report
    fi
    
    if [ "$FORMAT" = "json" ] || [ "$FORMAT" = "both" ]; then
        generate_json_report
    fi
    
    log_success "Отчет о тестировании сгенерирован успешно!"
}

main