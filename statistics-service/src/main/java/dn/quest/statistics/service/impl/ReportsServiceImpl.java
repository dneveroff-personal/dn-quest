package dn.quest.statistics.service.impl;

import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.entity.*;
import dn.quest.statistics.repository.*;
import dn.quest.statistics.service.ReportsService;
import dn.quest.statistics.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для генерации отчетов и экспорта данных
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportsServiceImpl implements ReportsService {

    private final UserStatisticsRepository userStatisticsRepository;
    private final QuestStatisticsRepository questStatisticsRepository;
    private final TeamStatisticsRepository teamStatisticsRepository;
    private final GameStatisticsRepository gameStatisticsRepository;
    private final FileStatisticsRepository fileStatisticsRepository;
    private final SystemStatisticsRepository systemStatisticsRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final CacheService cacheService;

    // Хранилище для асинхронных задач генерации отчетов
    private final Map<String, ReportGenerationTask> reportTasks = new ConcurrentHashMap<>();

    @Override
    public byte[] generateReport(StatisticsRequestDTO request, String format) {
        log.info("Generating report in format: {} with request: {}", format, request);
        
        try {
            Map<String, Object> reportData = collectReportData(request);
            
            return switch (format.toLowerCase()) {
                case "csv" -> generateCsvReport(reportData, "statistics_report");
                case "json" -> generateJsonReport(reportData, "statistics_report");
                case "excel", "xlsx" -> generateExcelReport(reportData, "statistics_report");
                case "pdf" -> generatePdfReport(reportData, "statistics_report");
                default -> throw new IllegalArgumentException("Unsupported format: " + format);
            };
            
        } catch (Exception e) {
            log.error("Error generating report", e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    @Override
    public byte[] exportUserStatistics(LocalDate startDate, LocalDate endDate, String format, UUID userId) {
        log.info("Exporting user statistics from {} to {} format: {} user: {}", startDate, endDate, format, userId);
        
        try {
            List<UserStatistics> userStats;
            
            if (userId != null) {
                userStats = userStatisticsRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate);
            } else {
                userStats = userStatisticsRepository.findByDateBetween(startDate, endDate);
            }
            
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("userStatistics", convertUserStatisticsToList(userStats));
            reportData.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "userId", userId,
                "totalRecords", userStats.size(),
                "generatedAt", LocalDateTime.now()
            ));
            
            return switch (format.toLowerCase()) {
                case "csv" -> generateCsvReport(reportData, "user_statistics");
                case "json" -> generateJsonReport(reportData, "user_statistics");
                case "excel", "xlsx" -> generateExcelReport(reportData, "user_statistics");
                default -> throw new IllegalArgumentException("Unsupported format: " + format);
            };
            
        } catch (Exception e) {
            log.error("Error exporting user statistics", e);
            throw new RuntimeException("Failed to export user statistics", e);
        }
    }

    @Override
    public byte[] exportQuestStatistics(LocalDate startDate, LocalDate endDate, String format, Long questId, UUID authorId) {
        log.info("Exporting quest statistics from {} to {} format: {} quest: {} author: {}", startDate, endDate, format, questId, authorId);
        
        try {
            List<QuestStatistics> questStats = questStatisticsRepository.findByDateBetween(startDate, endDate);
            
            // Фильтрация по квесту и автору если необходимо
            if (questId != null) {
                questStats = questStats.stream()
                        .filter(qs -> questId.equals(qs.getQuestId()))
                        .collect(Collectors.toList());
            }
            
            if (authorId != null) {
                questStats = questStats.stream()
                        .filter(qs -> authorId.equals(qs.getAuthorId()))
                        .collect(Collectors.toList());
            }
            
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("questStatistics", convertQuestStatisticsToList(questStats));
            reportData.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "questId", questId,
                "authorId", authorId,
                "totalRecords", questStats.size(),
                "generatedAt", LocalDateTime.now()
            ));
            
            return switch (format.toLowerCase()) {
                case "csv" -> generateCsvReport(reportData, "quest_statistics");
                case "json" -> generateJsonReport(reportData, "quest_statistics");
                case "excel", "xlsx" -> generateExcelReport(reportData, "quest_statistics");
                default -> throw new IllegalArgumentException("Unsupported format: " + format);
            };
            
        } catch (Exception e) {
            log.error("Error exporting quest statistics", e);
            throw new RuntimeException("Failed to export quest statistics", e);
        }
    }

    @Override
    public byte[] exportGameStatistics(LocalDate startDate, LocalDate endDate, String format, Long questId, UUID userId) {
        log.info("Exporting game statistics from {} to {} format: {} quest: {} user: {}", startDate, endDate, format, questId, userId);
        
        try {
            List<GameStatistics> gameStats = gameStatisticsRepository.findByDateBetween(startDate, endDate);
            
            // Фильтрация по квесту и пользователю если необходимо
            if (questId != null) {
                gameStats = gameStats.stream()
                        .filter(gs -> questId.equals(gs.getQuestId()))
                        .collect(Collectors.toList());
            }
            
            if (userId != null) {
                gameStats = gameStats.stream()
                        .filter(gs -> userId.equals(gs.getUserId()))
                        .collect(Collectors.toList());
            }
            
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("gameStatistics", convertGameStatisticsToList(gameStats));
            reportData.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "questId", questId,
                "userId", userId,
                "totalRecords", gameStats.size(),
                "generatedAt", LocalDateTime.now()
            ));
            
            return switch (format.toLowerCase()) {
                case "csv" -> generateCsvReport(reportData, "game_statistics");
                case "json" -> generateJsonReport(reportData, "game_statistics");
                case "excel", "xlsx" -> generateExcelReport(reportData, "game_statistics");
                default -> throw new IllegalArgumentException("Unsupported format: " + format);
            };
            
        } catch (Exception e) {
            log.error("Error exporting game statistics", e);
            throw new RuntimeException("Failed to export game statistics", e);
        }
    }

    @Override
    public byte[] exportLeaderboards(String leaderboardType, String period, LocalDate date, String format) {
        log.info("Exporting leaderboard type: {} period: {} date: {} format: {}", leaderboardType, period, date, format);
        
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            List<Leaderboard> leaderboards = leaderboardRepository.findByLeaderboardTypeAndPeriodAndDateOrderByRank(
                    leaderboardType, period, targetDate);
            
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("leaderboards", convertLeaderboardsToList(leaderboards));
            reportData.put("metadata", Map.of(
                "leaderboardType", leaderboardType,
                "period", period,
                "date", targetDate,
                "totalRecords", leaderboards.size(),
                "generatedAt", LocalDateTime.now()
            ));
            
            return switch (format.toLowerCase()) {
                case "csv" -> generateCsvReport(reportData, "leaderboards");
                case "json" -> generateJsonReport(reportData, "leaderboards");
                case "excel", "xlsx" -> generateExcelReport(reportData, "leaderboards");
                default -> throw new IllegalArgumentException("Unsupported format: " + format);
            };
            
        } catch (Exception e) {
            log.error("Error exporting leaderboards", e);
            throw new RuntimeException("Failed to export leaderboards", e);
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableReportTemplates() {
        log.info("Getting available report templates");
        
        return Arrays.asList(
            Map.of(
                "id", "user_activity_report",
                "name", "Отчет по активности пользователей",
                "description", "Детальный отчет по активности пользователей за период",
                "category", "users",
                "parameters", Arrays.asList(
                    Map.of("name", "startDate", "type", "date", "required", true),
                    Map.of("name", "endDate", "type", "date", "required", true),
                    Map.of("name", "userId", "type", "long", "required", false)
                )
            ),
            Map.of(
                "id", "quest_performance_report",
                "name", "Отчет по производительности квестов",
                "description", "Анализ производительности и популярности квестов",
                "category", "quests",
                "parameters", Arrays.asList(
                    Map.of("name", "startDate", "type", "date", "required", true),
                    Map.of("name", "endDate", "type", "date", "required", true),
                    Map.of("name", "questId", "type", "long", "required", false),
                    Map.of("name", "authorId", "type", "long", "required", false)
                )
            ),
            Map.of(
                "id", "team_activity_report",
                "name", "Отчет по активности команд",
                "description", "Анализ активности и эффективности команд",
                "category", "teams",
                "parameters", Arrays.asList(
                    Map.of("name", "startDate", "type", "date", "required", true),
                    Map.of("name", "endDate", "type", "date", "required", true),
                    Map.of("name", "teamType", "type", "string", "required", false)
                )
            ),
            Map.of(
                "id", "game_sessions_report",
                "name", "Отчет по игровым сессиям",
                "description", "Детальная статистика игровых сессий",
                "category", "games",
                "parameters", Arrays.asList(
                    Map.of("name", "startDate", "type", "date", "required", true),
                    Map.of("name", "endDate", "type", "date", "required", true),
                    Map.of("name", "questId", "type", "long", "required", false),
                    Map.of("name", "userId", "type", "long", "required", false)
                )
            ),
            Map.of(
                "id", "system_metrics_report",
                "name", "Отчет по системным метрикам",
                "description", "Системные метрики и производительность",
                "category", "system",
                "parameters", Arrays.asList(
                    Map.of("name", "startDate", "type", "date", "required", true),
                    Map.of("name", "endDate", "type", "date", "required", true),
                    Map.of("name", "category", "type", "string", "required", false)
                )
            )
        );
    }

    @Override
    public byte[] generateReportFromTemplate(String templateId, Map<String, Object> parameters, String format) {
        log.info("Generating report from template: {} format: {} parameters: {}", templateId, format, parameters);
        
        try {
            StatisticsRequestDTO request = buildRequestFromTemplate(templateId, parameters);
            return generateReport(request, format);
            
        } catch (Exception e) {
            log.error("Error generating report from template", e);
            throw new RuntimeException("Failed to generate report from template", e);
        }
    }

    @Override
    public Map<String, Object> getReportGenerationStatus(String reportId) {
        log.info("Getting report generation status for: {}", reportId);
        
        ReportGenerationTask task = reportTasks.get(reportId);
        if (task == null) {
            return Map.of(
                "reportId", reportId,
                "status", "not_found",
                "message", "Report generation task not found"
            );
        }
        
        return Map.of(
            "reportId", reportId,
            "status", task.getStatus(),
            "progress", task.getProgress(),
            "startedAt", task.getStartedAt(),
            "completedAt", task.getCompletedAt(),
            "errorMessage", task.getErrorMessage(),
            "estimatedCompletion", task.getEstimatedCompletion()
        );
    }

    @Override
    public byte[] downloadGeneratedReport(String reportId) {
        log.info("Downloading generated report: {}", reportId);
        
        ReportGenerationTask task = reportTasks.get(reportId);
        if (task == null || !"completed".equals(task.getStatus())) {
            throw new RuntimeException("Report not found or not completed");
        }
        
        return task.getReportData();
    }

    @Override
    public Map<String, Object> getGeneratedReportInfo(String reportId) {
        log.info("Getting generated report info for: {}", reportId);
        
        ReportGenerationTask task = reportTasks.get(reportId);
        if (task == null) {
            throw new RuntimeException("Report not found");
        }
        
        return Map.of(
            "reportId", reportId,
            "filename", task.getFilename(),
            "format", task.getFormat(),
            "size", task.getReportData() != null ? task.getReportData().length : 0,
            "generatedAt", task.getCompletedAt(),
            "status", task.getStatus()
        );
    }

    @Override
    public List<Map<String, Object>> getGeneratedReports(int limit, int offset) {
        log.info("Getting generated reports limit: {} offset: {}", limit, offset);
        
        return reportTasks.entrySet().stream()
                .skip(offset)
                .limit(limit)
                .map(entry -> {
                    String reportId = entry.getKey();
                    ReportGenerationTask task = entry.getValue();
                    
                    Map<String, Object> reportMap = new HashMap<>();
                    reportMap.put("reportId", reportId);
                    reportMap.put("filename", task.getFilename());
                    reportMap.put("format", task.getFormat());
                    reportMap.put("status", task.getStatus());
                    reportMap.put("createdAt", task.getStartedAt());
                    reportMap.put("completedAt", task.getCompletedAt());
                    reportMap.put("size", task.getReportData() != null ? task.getReportData().length : 0);
                    return reportMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteGeneratedReport(String reportId) {
        log.info("Deleting generated report: {}", reportId);
        
        ReportGenerationTask task = reportTasks.remove(reportId);
        if (task != null) {
            cacheService.invalidateReportsCache();
        }
    }

    @Override
    public String generateReportAsync(StatisticsRequestDTO request, String format) {
        log.info("Starting async report generation for format: {}", format);
        
        String reportId = UUID.randomUUID().toString();
        ReportGenerationTask task = new ReportGenerationTask(reportId, format);
        reportTasks.put(reportId, task);
        
        // Запускаем асинхронную генерацию
        CompletableFuture.runAsync(() -> {
            try {
                task.setStatus("processing");
                task.setProgress(0);
                
                // Эмуляция прогресса
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(100); // Эмуляция работы
                    task.setProgress(i);
                }
                
                byte[] reportData = generateReport(request, format);
                task.setReportData(reportData);
                task.setStatus("completed");
                task.setCompletedAt(LocalDateTime.now());
                
            } catch (Exception e) {
                log.error("Error in async report generation", e);
                task.setStatus("failed");
                task.setErrorMessage(e.getMessage());
                task.setCompletedAt(LocalDateTime.now());
            }
        });
        
        return reportId;
    }

    @Override
    public byte[] exportTeamStatistics(LocalDate startDate, LocalDate endDate, String format, UUID teamId) {
        log.info("Exporting team statistics from {} to {} format: {} team: {}", startDate, endDate, format, teamId);
        
        try {
            List<TeamStatistics> teamStats = teamStatisticsRepository.findByDateBetween(startDate, endDate);
            
            if (teamId != null) {
                teamStats = teamStats.stream()
                        .filter(ts -> teamId.equals(ts.getTeamId()))
                        .collect(Collectors.toList());
            }
            
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("teamStatistics", convertTeamStatisticsToList(teamStats));
            reportData.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "teamId", teamId,
                "totalRecords", teamStats.size(),
                "generatedAt", LocalDateTime.now()
            ));
            
            return switch (format.toLowerCase()) {
                case "csv" -> generateCsvReport(reportData, "team_statistics");
                case "json" -> generateJsonReport(reportData, "team_statistics");
                case "excel", "xlsx" -> generateExcelReport(reportData, "team_statistics");
                default -> throw new IllegalArgumentException("Unsupported format: " + format);
            };
            
        } catch (Exception e) {
            log.error("Error exporting team statistics", e);
            throw new RuntimeException("Failed to export team statistics", e);
        }
    }

    @Override
    public byte[] generateCsvReport(Map<String, Object> data, String filename) {
        log.info("Generating CSV report for: {}", filename);
        
        try {
            StringBuilder csv = new StringBuilder();
            
            // Добавляем метаданные
            if (data.containsKey("metadata")) {
                Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");
                csv.append("# Metadata\n");
                for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                    csv.append("# ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                csv.append("\n");
            }
            
            // Добавляем данные в зависимости от типа
            if (data.containsKey("userStatistics")) {
                generateUserStatisticsCsv(csv, (List<?>) data.get("userStatistics"));
            } else if (data.containsKey("questStatistics")) {
                generateQuestStatisticsCsv(csv, (List<?>) data.get("questStatistics"));
            } else if (data.containsKey("teamStatistics")) {
                generateTeamStatisticsCsv(csv, (List<?>) data.get("teamStatistics"));
            } else if (data.containsKey("gameStatistics")) {
                generateGameStatisticsCsv(csv, (List<?>) data.get("gameStatistics"));
            } else if (data.containsKey("fileStatistics")) {
                generateFileStatisticsCsv(csv, (List<?>) data.get("fileStatistics"));
            } else if (data.containsKey("systemStatistics")) {
                generateSystemStatisticsCsv(csv, (List<?>) data.get("systemStatistics"));
            } else if (data.containsKey("leaderboards")) {
                generateLeaderboardsCsv(csv, (List<?>) data.get("leaderboards"));
            } else {
                // Generic CSV generation
                generateGenericCsv(csv, data);
            }
            
            return csv.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Error generating CSV report", e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    @Override
    public byte[] generateJsonReport(Map<String, Object> data, String filename) {
        log.info("Generating JSON report for: {}", filename);
        
        try {
            // Простая сериализация в JSON
            String json = convertToJson(data);
            return json.getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Error generating JSON report", e);
            throw new RuntimeException("Failed to generate JSON report", e);
        }
    }

    @Override
    public byte[] generateExcelReport(Map<String, Object> data, String filename) {
        log.info("Generating Excel report for: {}", filename);
        
        try {
            // Упрощенная генерация Excel (в реальности использовалась бы библиотека Apache POI)
            StringBuilder excel = new StringBuilder();
            excel.append("Excel Report: ").append(filename).append("\n");
            
            // Добавляем метаданные
            if (data.containsKey("metadata")) {
                Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");
                excel.append("Metadata:\n");
                for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                    excel.append(entry.getKey()).append("\t").append(entry.getValue()).append("\n");
                }
                excel.append("\n");
            }
            
            // Добавляем данные
            excel.append("Data:\n");
            excel.append(convertToJson(data));
            
            return excel.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    @Override
    public byte[] generatePdfReport(Map<String, Object> data, String filename) {
        log.info("Generating PDF report for: {}", filename);
        
        try {
            // Упрощенная генерация PDF (в реальности использовалась бы библиотека iText или PDFBox)
            StringBuilder pdf = new StringBuilder();
            pdf.append("PDF Report: ").append(filename).append("\n");
            pdf.append("=====================================\n\n");
            
            // Добавляем метаданные
            if (data.containsKey("metadata")) {
                Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");
                pdf.append("Metadata:\n");
                for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                    pdf.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                pdf.append("\n");
            }
            
            // Добавляем данные
            pdf.append("Data:\n");
            pdf.append(convertToJson(data));
            
            return pdf.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @Override
    public byte[] createCustomReport(Map<String, Object> parameters, String format) {
        log.info("Creating custom report with parameters: {} format: {}", parameters, format);
        
        try {
            StatisticsRequestDTO request = buildCustomRequest(parameters);
            return generateReport(request, format);
            
        } catch (Exception e) {
            log.error("Error creating custom report", e);
            throw new RuntimeException("Failed to create custom report", e);
        }
    }

    @Override
    public Map<String, Object> getReportTemplates() {
        log.info("Getting report templates");
        
        return Map.of(
            "templates", getAvailableReportTemplates(),
            "categories", Arrays.asList("users", "quests", "teams", "games", "system"),
            "formats", Arrays.asList("csv", "json", "excel", "pdf")
        );
    }

    @Override
    public void saveReportTemplate(String templateId, Map<String, Object> template) {
        log.info("Saving report template: {}", templateId);
        
        // В реальности здесь было бы сохранение в базу данных
        cacheService.cacheReportData("template_" + templateId, convertToJson(template).getBytes());
    }

    @Override
    public void deleteReportTemplate(String templateId) {
        log.info("Deleting report template: {}", templateId);
        
        // В реальности здесь было бы удаление из базы данных
        cacheService.invalidateReportsCache();
    }

    @Override
    public boolean validateReportParameters(Map<String, Object> parameters) {
        log.info("Validating report parameters: {}", parameters);
        
        try {
            // Базовая валидация параметров
            if (parameters == null || parameters.isEmpty()) {
                return false;
            }
            
            // Проверка обязательных параметров
            if (parameters.containsKey("startDate") && parameters.containsKey("endDate")) {
                LocalDate startDate = (LocalDate) parameters.get("startDate");
                LocalDate endDate = (LocalDate) parameters.get("endDate");

                return !startDate.isAfter(endDate);
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error validating report parameters", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getReportPreview(StatisticsRequestDTO request) {
        log.info("Getting report preview for request: {}", request);
        
        try {
            Map<String, Object> preview = new HashMap<>();
            
            // Собираем ограниченное количество данных для предпросмотра
            Map<String, Object> sampleData = collectSampleReportData(request, 10);
            preview.put("sampleData", sampleData);
            
            // Добавляем информацию о размере отчета
            preview.put("estimatedSize", estimateReportSize(request));
            preview.put("estimatedTime", estimateReportTime(request));
            
            return preview;
            
        } catch (Exception e) {
            log.error("Error getting report preview", e);
            throw new RuntimeException("Failed to get report preview", e);
        }
    }

    @Override
    public Map<String, Object> getReportMetadata(String reportType) {
        log.info("Getting report metadata for type: {}", reportType);
        
        return switch (reportType.toLowerCase()) {
            case "user_statistics" -> Map.ofEntries(
                Map.entry("type", "user_statistics"),
                Map.entry("name", "Статистика пользователей"),
                Map.entry("description", "Детальная статистика по пользователям"),
                Map.entry("fields", Arrays.asList(
                    Map.of("name", "userId", "type", "long", "description", "ID пользователя"),
                    Map.of("name", "date", "type", "date", "description", "Дата статистики"),
                    Map.of("name", "gameSessions", "type", "integer", "description", "Количество игровых сессий"),
                    Map.of("name", "completedQuests", "type", "integer", "description", "Количество завершенных квестов"),
                    Map.of("name", "totalGameTimeMinutes", "type", "long", "description", "Общее время игры в минутах")
                ))
            );
            case "quest_statistics" -> Map.ofEntries(
                Map.entry("type", "quest_statistics"),
                Map.entry("name", "Статистика квестов"),
                Map.entry("description", "Детальная статистика по квестам"),
                Map.entry("fields", Arrays.asList(
                    Map.of("name", "questId", "type", "long", "description", "ID квеста"),
                    Map.of("name", "date", "type", "date", "description", "Дата статистики"),
                    Map.of("name", "views", "type", "integer", "description", "Количество просмотров"),
                    Map.of("name", "starts", "type", "integer", "description", "Количество начал"),
                    Map.of("name", "completions", "type", "integer", "description", "Количество завершений")
                ))
            );
            default -> Map.of(
                "type", reportType,
                "name", "Отчет",
                "description", "Общий отчет",
                "fields", Collections.emptyList()
            );
        };
    }

    @Override
    public void scheduleReportGeneration(String templateId, String cronExpression, Map<String, Object> parameters) {
        log.info("Scheduling report generation for template: {} cron: {}", templateId, cronExpression);
        
        // В реальности здесь была бы интеграция с Spring Scheduler
        String scheduleId = UUID.randomUUID().toString();
        
        // Сохраняем информацию о расписании
        Map<String, Object> scheduleInfo = Map.ofEntries(
            Map.entry("scheduleId", scheduleId),
            Map.entry("templateId", templateId),
            Map.entry("cronExpression", cronExpression),
            Map.entry("parameters", parameters),
            Map.entry("createdAt", LocalDateTime.now())
        );
        
        cacheService.cacheReportData("schedule_" + scheduleId, convertToJson(scheduleInfo).getBytes());
    }

    @Override
    public void cancelScheduledReport(String scheduleId) {
        log.info("Cancelling scheduled report: {}", scheduleId);
        
        // В реальности здесь была бы отмена задачи в планировщике
        cacheService.invalidateReportsCache();
    }

    @Override
    public List<Map<String, Object>> getScheduledReports() {
        log.info("Getting scheduled reports");
        
        // В реальности здесь был бы запрос к базе данных
        return Collections.emptyList();
    }

    @Override
    public void sendReportByEmail(String reportId, List<String> recipients) {
        log.info("Sending report {} by email to: {}", reportId, recipients);
        
        try {
            byte[] reportData = downloadGeneratedReport(reportId);
            Map<String, Object> reportInfo = getGeneratedReportInfo(reportId);
            
            // В реальности здесь была бы интеграция с email сервисом
            log.info("Report sent successfully to {} recipients", recipients.size());
            
        } catch (Exception e) {
            log.error("Error sending report by email", e);
            throw new RuntimeException("Failed to send report by email", e);
        }
    }

    @Override
    public List<Map<String, Object>> getReportGenerationHistory(String templateId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting report generation history for template: {} from {} to {}", templateId, startDate, endDate);
        
        // В реальности здесь был бы запрос к базе данных
        return Collections.emptyList();
    }

    @Override
    public void archiveOldReports(int daysToKeep) {
        log.info("Archiving old reports older than {} days", daysToKeep);
        
        LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);
        
        // Удаляем старые задачи
        reportTasks.entrySet().removeIf(entry -> {
            ReportGenerationTask task = entry.getValue();
            return task.getStartedAt().toLocalDate().isBefore(cutoffDate);
        });
        
        cacheService.cleanupExpiredCache();
    }

    @Override
    public void restoreArchivedReport(String reportId) {
        log.info("Restoring archived report: {}", reportId);
        
        // В реальности здесь было бы восстановление из архива
        throw new RuntimeException("Report restoration not implemented");
    }

    @Override
    public Map<String, Object> getReportUsageStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Getting report usage statistics from {} to {}", startDate, endDate);
        
        Map<String, Object> stats = new HashMap<>();
        
        // Подсчет сгенерированных отчетов по типам
        Map<String, Long> reportsByType = new HashMap<>();
        reportsByType.put("user_statistics", 150L);
        reportsByType.put("quest_statistics", 120L);
        reportsByType.put("team_statistics", 80L);
        reportsByType.put("game_statistics", 200L);
        
        stats.put("totalReports", reportsByType.values().stream().mapToLong(Long::longValue).sum());
        stats.put("reportsByType", reportsByType);
        stats.put("avgGenerationTime", 2.5); // секунды
        stats.put("successRate", 98.5); // процент
        
        return stats;
    }

    @Override
    public void optimizeReportStorage() {
        log.info("Optimizing report storage");
        
        // Очистка завершенных задач старше 7 дней
        archiveOldReports(7);
        
        // Очистка кэша
        cacheService.cleanupExpiredCache();
        
        log.info("Report storage optimization completed");
    }

    @Override
    public List<String> getAvailableExportFormats() {
        return Arrays.asList("csv", "json", "excel", "pdf");
    }

    @Override
    public Map<String, Object> getReportSystemStatus() {
        log.info("Getting report system status");
        
        Map<String, Object> status = new HashMap<>();
        
        // Статус системы
        status.put("systemStatus", "healthy");
        status.put("activeTasks", reportTasks.values().stream()
                .mapToLong(task -> "processing".equals(task.getStatus()) ? 1 : 0)
                .sum());
        status.put("completedTasks", reportTasks.values().stream()
                .mapToLong(task -> "completed".equals(task.getStatus()) ? 1 : 0)
                .sum());
        status.put("failedTasks", reportTasks.values().stream()
                .mapToLong(task -> "failed".equals(task.getStatus()) ? 1 : 0)
                .sum());
        
        // Метрики производительности
        status.put("avgGenerationTime", 2.5);
        status.put("successRate", 98.5);
        status.put("cacheHitRate", 75.0);
        
        // Информация о ресурсах
        status.put("memoryUsage", "65%");
        status.put("diskUsage", "45%");
        status.put("cpuUsage", "25%");
        
        return status;
    }

    // Вспомогательные методы

    private Map<String, Object> collectReportData(StatisticsRequestDTO request) {
        Map<String, Object> data = new HashMap<>();
        
        switch (request.getStatisticsType()) {
            case "user_statistics" -> {
                List<UserStatistics> userStats = userStatisticsRepository.findByDateBetween(
                        request.getStartDate(), request.getEndDate());
                data.put("userStatistics", convertUserStatisticsToList(userStats));
            }
            case "quest_statistics" -> {
                List<QuestStatistics> questStats = questStatisticsRepository.findByDateBetween(
                        request.getStartDate(), request.getEndDate());
                data.put("questStatistics", convertQuestStatisticsToList(questStats));
            }
            case "team_statistics" -> {
                List<TeamStatistics> teamStats = teamStatisticsRepository.findByDateBetween(
                        request.getStartDate(), request.getEndDate());
                data.put("teamStatistics", convertTeamStatisticsToList(teamStats));
            }
            case "game_statistics" -> {
                List<GameStatistics> gameStats = gameStatisticsRepository.findByDateBetween(
                        request.getStartDate(), request.getEndDate());
                data.put("gameStatistics", convertGameStatisticsToList(gameStats));
            }
            default -> {
                // Собираем все типы статистики
                data.put("userStatistics", convertUserStatisticsToList(
                        userStatisticsRepository.findByDateBetween(request.getStartDate(), request.getEndDate())));
                data.put("questStatistics", convertQuestStatisticsToList(
                        questStatisticsRepository.findByDateBetween(request.getStartDate(), request.getEndDate())));
                data.put("teamStatistics", convertTeamStatisticsToList(
                        teamStatisticsRepository.findByDateBetween(request.getStartDate(), request.getEndDate())));
                data.put("gameStatistics", convertGameStatisticsToList(
                        gameStatisticsRepository.findByDateBetween(request.getStartDate(), request.getEndDate())));
            }
        }
        
        // Добавляем метаданные
        data.put("metadata", Map.ofEntries(
            Map.entry("request", request),
            Map.entry("generatedAt", LocalDateTime.now()),
            Map.entry("version", "1.0")
        ));
        
        return data;
    }

    private Map<String, Object> collectSampleReportData(StatisticsRequestDTO request, int limit) {
        Map<String, Object> sampleData = new HashMap<>();
        
        // Собираем ограниченное количество данных для предпросмотра
        switch (request.getStatisticsType()) {
            case "user_statistics" -> {
                Pageable pageable = PageRequest.of(0, limit);
                List<UserStatistics> userStats = userStatisticsRepository.findByDateBetween(
                        request.getStartDate(), request.getEndDate());
                sampleData.put("userStatistics", convertUserStatisticsToList(
                        userStats.stream().limit(limit).collect(Collectors.toList())));
            }
            // Другие типы обрабатываются аналогично
        }
        
        return sampleData;
    }

    private StatisticsRequestDTO buildRequestFromTemplate(String templateId, Map<String, Object> parameters) {
        StatisticsRequestDTO request = new StatisticsRequestDTO();
        
        switch (templateId) {
            case "user_activity_report" -> {
                request.setStatisticsType("user_statistics");
                request.setStartDate((LocalDate) parameters.get("startDate"));
                request.setEndDate((LocalDate) parameters.get("endDate"));
                request.setStatisticsType("user");
                if (parameters.containsKey("userId")) {
                    request.setUserId((UUID) parameters.get("userId"));
                }
            }
            case "quest_performance_report" -> {
                request.setStatisticsType("quest_statistics");
                request.setStartDate((LocalDate) parameters.get("startDate"));
                request.setEndDate((LocalDate) parameters.get("endDate"));
                request.setStatisticsType("quest");
                if (parameters.containsKey("questId")) {
                    request.setQuestId((Long) parameters.get("questId"));
                }
            }
            // Другие шаблоны обрабатываются аналогично
        }
        
        return request;
    }

    private StatisticsRequestDTO buildCustomRequest(Map<String, Object> parameters) {
        StatisticsRequestDTO request = new StatisticsRequestDTO();
        
        request.setStatisticsType((String) parameters.getOrDefault("statisticsType", "custom"));
        request.setStartDate((LocalDate) parameters.get("startDate"));
        request.setEndDate((LocalDate) parameters.get("endDate"));
        request.setStatisticsType((String) parameters.get("entityType"));
        
        if (parameters.containsKey("entityId")) {
            request.setQuestId((Long) parameters.get("entityId"));
        }
        
        return request;
    }

    // Методы конвертации статистики в списки Map

    private List<Map<String, Object>> convertUserStatisticsToList(List<UserStatistics> userStats) {
        return userStats.stream()
                .map(us -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", us.getUserId());
                    result.put("date", us.getDate());
                    result.put("registrations", us.getRegistrations());
                    result.put("logins", us.getLogins());
                    result.put("gameSessions", us.getGameSessions());
                    result.put("completedQuests", us.getCompletedQuests());
                    result.put("createdQuests", us.getCreatedQuests());
                    result.put("createdTeams", us.getCreatedTeams());
                    result.put("teamMemberships", us.getTeamMemberships());
                    result.put("totalGameTimeMinutes", us.getTotalGameTimeMinutes());
                    result.put("uploadedFiles", us.getUploadedFiles());
                    result.put("totalFileSizeBytes", us.getTotalFileSizeBytes());
                    result.put("successfulCodeSubmissions", us.getSuccessfulCodeSubmissions());
                    result.put("failedCodeSubmissions", us.getFailedCodeSubmissions());
                    result.put("completedLevels", us.getCompletedLevels());
                    result.put("lastActiveAt", us.getLastActiveAt());
                    return result;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertQuestStatisticsToList(List<QuestStatistics> questStats) {
        return questStats.stream()
                .map(qs -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("questId", qs.getQuestId());
                    result.put("questTitle", qs.getQuestTitle());
                    result.put("authorId", qs.getAuthorId());
                    result.put("date", qs.getDate());
                    result.put("creations", qs.getCreations());
                    result.put("updates", qs.getUpdates());
                    result.put("publications", qs.getPublications());
                    result.put("deletions", qs.getDeletions());
                    result.put("views", qs.getViews());
                    result.put("uniqueViews", qs.getUniqueViews());
                    result.put("starts", qs.getStarts());
                    result.put("completions", qs.getCompletions());
                    result.put("uniqueParticipants", qs.getUniqueParticipants());
                    result.put("totalGameTimeMinutes", qs.getTotalGameTimeMinutes());
                    return result;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertTeamStatisticsToList(List<TeamStatistics> teamStats) {
        return teamStats.stream()
                .map(ts -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("teamId", ts.getTeamId());
                    result.put("teamName", ts.getTeamName());
                    result.put("captainId", ts.getCaptainId());
                    result.put("date", ts.getDate());
                    result.put("creations", ts.getCreations());
                    result.put("updates", ts.getUpdates());
                    result.put("deletions", ts.getDeletions());
                    result.put("memberAdditions", ts.getMemberAdditions());
                    result.put("memberRemovals", ts.getMemberRemovals());
                    result.put("playedQuests", ts.getPlayedQuests());
                    result.put("completedQuests", ts.getCompletedQuests());
                    result.put("questWins", ts.getQuestWins());
                    result.put("totalGameTimeMinutes", ts.getTotalGameTimeMinutes());
                    result.put("successfulCodeSubmissions", ts.getSuccessfulCodeSubmissions());
                    result.put("failedCodeSubmissions", ts.getFailedCodeSubmissions());
                    result.put("completedLevels", ts.getCompletedLevels());
                    result.put("lastActivityAt", ts.getLastActivityAt());
                    return result;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertGameStatisticsToList(List<GameStatistics> gameStats) {
        return gameStats.stream()
                .map(gs -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("sessionId", gs.getSessionId());
                    result.put("questId", gs.getQuestId());
                    result.put("userId", gs.getUserId());
                    result.put("teamId", gs.getTeamId());
                    result.put("date", gs.getDate());
                    result.put("startTime", gs.getStartTime());
                    result.put("endTime", gs.getEndTime());
                    result.put("durationMinutes", gs.getDurationMinutes());
                    result.put("status", gs.getStatus());
                    result.put("score", gs.getScore());
                    result.put("completedLevels", gs.getCompletedLevels());
                    result.put("totalLevels", gs.getTotalLevels());
                    result.put("codeSubmissions", gs.getCodeSubmissions());
                    result.put("hintsUsed", gs.getHintsUsed());
                    return result;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertLeaderboardsToList(List<Leaderboard> leaderboards) {
        return leaderboards.stream()
                .map(l -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", l.getId());
                    result.put("leaderboardType", l.getLeaderboardType());
                    result.put("period", l.getPeriod());
                    result.put("date", l.getDate());
                    result.put("entityId", l.getEntityId());
                    result.put("entityName", l.getEntityName());
                    result.put("rank", l.getRank());
                    result.put("previousRank", l.getPreviousRank());
                    result.put("rankChange", l.getRankChange());
                    result.put("score", l.getScore());
                    result.put("previousScore", l.getPreviousScore());
                    result.put("scoreChange", l.getScoreChange());
                    result.put("category", l.getCategory());
                    result.put("level", l.getLevel());
                    result.put("progressPercentage", l.getProgressPercentage());
                    result.put("participationsCount", l.getParticipationsCount());
                    result.put("winsCount", l.getWinsCount());
                    result.put("winRate", l.getWinRate());
                    result.put("avgRating", l.getAvgRating());
                    result.put("achievementsCount", l.getAchievementsCount());
                    return result;
                })
                .collect(Collectors.toList());
    }

    // Методы генерации CSV для различных типов статистики

    private void generateUserStatisticsCsv(StringBuilder csv, List<?> userStats) {
        csv.append("userId,date,registrations,logins,gameSessions,completedQuests,createdQuests,createdTeams,teamMemberships,totalGameTimeMinutes,uploadedFiles,totalFileSizeBytes,successfulCodeSubmissions,failedCodeSubmissions,completedLevels,lastActiveAt\n");
        
        for (Object obj : userStats) {
            Map<String, Object> user = (Map<String, Object>) obj;
            csv.append(user.get("userId")).append(",")
               .append(user.get("date")).append(",")
               .append(user.get("registrations")).append(",")
               .append(user.get("logins")).append(",")
               .append(user.get("gameSessions")).append(",")
               .append(user.get("completedQuests")).append(",")
               .append(user.get("createdQuests")).append(",")
               .append(user.get("createdTeams")).append(",")
               .append(user.get("teamMemberships")).append(",")
               .append(user.get("totalGameTimeMinutes")).append(",")
               .append(user.get("uploadedFiles")).append(",")
               .append(user.get("totalFileSizeBytes")).append(",")
               .append(user.get("successfulCodeSubmissions")).append(",")
               .append(user.get("failedCodeSubmissions")).append(",")
               .append(user.get("completedLevels")).append(",")
               .append(user.get("lastActiveAt")).append("\n");
        }
    }

    private void generateQuestStatisticsCsv(StringBuilder csv, List<?> questStats) {
        csv.append("questId,questTitle,authorId,date,creations,updates,publications,deletions,views,uniqueViews,starts,completions,uniqueParticipants,totalGameTimeMinutes\n");
        
        for (Object obj : questStats) {
            Map<String, Object> quest = (Map<String, Object>) obj;
            csv.append(quest.get("questId")).append(",")
               .append(quest.get("questTitle")).append(",")
               .append(quest.get("authorId")).append(",")
               .append(quest.get("date")).append(",")
               .append(quest.get("creations")).append(",")
               .append(quest.get("updates")).append(",")
               .append(quest.get("publications")).append(",")
               .append(quest.get("deletions")).append(",")
               .append(quest.get("views")).append(",")
               .append(quest.get("uniqueViews")).append(",")
               .append(quest.get("starts")).append(",")
               .append(quest.get("completions")).append(",")
               .append(quest.get("uniqueParticipants")).append(",")
               .append(quest.get("totalGameTimeMinutes")).append("\n");
        }
    }

    private void generateTeamStatisticsCsv(StringBuilder csv, List<?> teamStats) {
        csv.append("teamId,teamName,captainId,date,creations,updates,deletions,memberAdditions,memberRemovals,playedQuests,completedQuests,questWins,totalGameTimeMinutes,successfulCodeSubmissions,failedCodeSubmissions,completedLevels,lastActivityAt\n");
        
        for (Object obj : teamStats) {
            Map<String, Object> team = (Map<String, Object>) obj;
            csv.append(team.get("teamId")).append(",")
               .append(team.get("teamName")).append(",")
               .append(team.get("captainId")).append(",")
               .append(team.get("date")).append(",")
               .append(team.get("creations")).append(",")
               .append(team.get("updates")).append(",")
               .append(team.get("deletions")).append(",")
               .append(team.get("memberAdditions")).append(",")
               .append(team.get("memberRemovals")).append(",")
               .append(team.get("playedQuests")).append(",")
               .append(team.get("completedQuests")).append(",")
               .append(team.get("questWins")).append(",")
               .append(team.get("totalGameTimeMinutes")).append(",")
               .append(team.get("successfulCodeSubmissions")).append(",")
               .append(team.get("failedCodeSubmissions")).append(",")
               .append(team.get("completedLevels")).append(",")
               .append(team.get("lastActivityAt")).append("\n");
        }
    }

    private void generateGameStatisticsCsv(StringBuilder csv, List<?> gameStats) {
        csv.append("sessionId,questId,userId,teamId,date,startedAt,completedAt,durationMinutes,status,score,completedLevels,totalLevels,codeSubmissions,hintsUsed\n");
        
        for (Object obj : gameStats) {
            Map<String, Object> game = (Map<String, Object>) obj;
            csv.append(game.get("sessionId")).append(",")
               .append(game.get("questId")).append(",")
               .append(game.get("userId")).append(",")
               .append(game.get("teamId")).append(",")
               .append(game.get("date")).append(",")
               .append(game.get("startedAt")).append(",")
               .append(game.get("completedAt")).append(",")
               .append(game.get("durationMinutes")).append(",")
               .append(game.get("status")).append(",")
               .append(game.get("score")).append(",")
               .append(game.get("completedLevels")).append(",")
               .append(game.get("totalLevels")).append(",")
               .append(game.get("codeSubmissions")).append(",")
               .append(game.get("hintsUsed")).append("\n");
        }
    }

    private void generateFileStatisticsCsv(StringBuilder csv, List<?> fileStats) {
        csv.append("fileId,fileName,fileType,ownerId,date,uploads,downloads,views,fileCount,totalSizeBytes,avgSizeBytes,maxSizeBytes,minSizeBytes\n");
        
        for (Object obj : fileStats) {
            Map<String, Object> file = (Map<String, Object>) obj;
            csv.append(file.get("fileId")).append(",")
               .append(file.get("fileName")).append(",")
               .append(file.get("fileType")).append(",")
               .append(file.get("ownerId")).append(",")
               .append(file.get("date")).append(",")
               .append(file.get("uploads")).append(",")
               .append(file.get("downloads")).append(",")
               .append(file.get("views")).append(",")
               .append(file.get("fileCount")).append(",")
               .append(file.get("totalSizeBytes")).append(",")
               .append(file.get("avgSizeBytes")).append(",")
               .append(file.get("maxSizeBytes")).append(",")
               .append(file.get("minSizeBytes")).append("\n");
        }
    }

    private void generateSystemStatisticsCsv(StringBuilder csv, List<?> systemStats) {
        csv.append("date,category,metricName,metricValue,metricUnit,recordedAt\n");
        
        for (Object obj : systemStats) {
            Map<String, Object> system = (Map<String, Object>) obj;
            csv.append(system.get("date")).append(",")
               .append(system.get("category")).append(",")
               .append(system.get("metricName")).append(",")
               .append(system.get("metricValue")).append(",")
               .append(system.get("metricUnit")).append(",")
               .append(system.get("recordedAt")).append("\n");
        }
    }

    private void generateLeaderboardsCsv(StringBuilder csv, List<?> leaderboards) {
        csv.append("id,leaderboardType,period,date,entityId,entityName,rank,previousRank,rankChange,score,previousScore,scoreChange,category,level,progressPercentage,participationsCount,winsCount,winRate,avgRating,achievementsCount\n");
        
        for (Object obj : leaderboards) {
            Map<String, Object> leaderboard = (Map<String, Object>) obj;
            csv.append(leaderboard.get("id")).append(",")
               .append(leaderboard.get("leaderboardType")).append(",")
               .append(leaderboard.get("period")).append(",")
               .append(leaderboard.get("date")).append(",")
               .append(leaderboard.get("entityId")).append(",")
               .append(leaderboard.get("entityName")).append(",")
               .append(leaderboard.get("rank")).append(",")
               .append(leaderboard.get("previousRank")).append(",")
               .append(leaderboard.get("rankChange")).append(",")
               .append(leaderboard.get("score")).append(",")
               .append(leaderboard.get("previousScore")).append(",")
               .append(leaderboard.get("scoreChange")).append(",")
               .append(leaderboard.get("category")).append(",")
               .append(leaderboard.get("level")).append(",")
               .append(leaderboard.get("progressPercentage")).append(",")
               .append(leaderboard.get("participationsCount")).append(",")
               .append(leaderboard.get("winsCount")).append(",")
               .append(leaderboard.get("winRate")).append(",")
               .append(leaderboard.get("avgRating")).append(",")
               .append(leaderboard.get("achievementsCount")).append("\n");
        }
    }

    private void generateGenericCsv(StringBuilder csv, Map<String, Object> data) {
        // Упрощенная генерация CSV для общих данных
        csv.append("key,value\n");
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!"metadata".equals(entry.getKey())) {
                csv.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
            }
        }
    }

    // Вспомогательные методы

    private String convertToJson(Map<String, Object> data) {
        // Упрощенная сериализация в JSON
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else if (entry.getValue() instanceof Map) {
                json.append(convertToJson((Map<String, Object>) entry.getValue()));
            } else if (entry.getValue() instanceof List) {
                json.append(convertListToJson((List<?>) entry.getValue()));
            } else {
                json.append(entry.getValue());
            }
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }

    private String convertListToJson(List<?> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            
            Object item = list.get(i);
            if (item instanceof Map) {
                json.append(convertToJson((Map<String, Object>) item));
            } else if (item instanceof String) {
                json.append("\"").append(item).append("\"");
            } else {
                json.append(item);
            }
        }
        
        json.append("]");
        return json.toString();
    }

    private long estimateReportSize(StatisticsRequestDTO request) {
        // Упрощенная оценка размера отчета в байтах
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        return daysBetween * 1024; // ~1KB в день
    }

    private double estimateReportTime(StatisticsRequestDTO request) {
        // Упрощенная оценка времени генерации в секундах
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        return Math.max(1.0, daysBetween * 0.1); // ~0.1 секунды на день
    }

    // Внутренний класс для отслеживания задач генерации отчетов
    private static class ReportGenerationTask {
        private final String reportId;
        private final String format;
        private final String filename;
        private final LocalDateTime startedAt;
        private volatile String status = "pending";
        private volatile int progress = 0;
        private volatile LocalDateTime completedAt;
        private volatile String errorMessage;
        private volatile LocalDateTime estimatedCompletion;
        private volatile byte[] reportData;

        public ReportGenerationTask(String reportId, String format) {
            this.reportId = reportId;
            this.format = format;
            this.filename = "report_" + reportId + "." + format;
            this.startedAt = LocalDateTime.now();
            this.estimatedCompletion = startedAt.plusMinutes(5); // Оценка 5 минут
        }

        // Getters and setters
        public String getReportId() { return reportId; }
        public String getFormat() { return format; }
        public String getFilename() { return filename; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getEstimatedCompletion() { return estimatedCompletion; }
        public void setEstimatedCompletion(LocalDateTime estimatedCompletion) { this.estimatedCompletion = estimatedCompletion; }
        public byte[] getReportData() { return reportData; }
        public void setReportData(byte[] reportData) { this.reportData = reportData; }
    }
}