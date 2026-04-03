package dn.quest.statistics.service;

import dn.quest.statistics.dto.StatisticsRequestDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для генерации отчетов и экспорта данных
 */
public interface ReportsService {

    /**
     * Сгенерировать отчет
     */
    byte[] generateReport(StatisticsRequestDTO request, String format);

    /**
     * Экспортировать статистику пользователей
     */
    byte[] exportUserStatistics(LocalDate startDate, LocalDate endDate, String format, UUID userId);

    /**
     * Экспортировать статистику квестов
     */
    byte[] exportQuestStatistics(LocalDate startDate, LocalDate endDate, String format, Long questId, UUID authorId);

    /**
     * Экспортировать игровую статистику
     */
    byte[] exportGameStatistics(LocalDate startDate, LocalDate endDate, String format, Long questId, UUID userId);

    /**
     * Экспортировать лидерборды
     */
    byte[] exportLeaderboards(String leaderboardType, String period, LocalDate date, String format);

    /**
     * Получить список доступных отчетов
     */
    List<Map<String, Object>> getAvailableReportTemplates();

    /**
     * Создать отчет по шаблону
     */
    byte[] generateReportFromTemplate(String templateId, Map<String, Object> parameters, String format);

    /**
     * Получить статус генерации отчета
     */
    Map<String, Object> getReportGenerationStatus(String reportId);

    /**
     * Скачать ранее сгенерированный отчет
     */
    byte[] downloadGeneratedReport(String reportId);

    /**
     * Получить информацию о сгенерированном отчете
     */
    Map<String, Object> getGeneratedReportInfo(String reportId);

    /**
     * Получить список сгенерированных отчетов
     */
    List<Map<String, Object>> getGeneratedReports(int limit, int offset);

    /**
     * Удалить сгенерированный отчет
     */
    void deleteGeneratedReport(String reportId);

    /**
     * Асинхронно сгенерировать отчет
     */
    String generateReportAsync(StatisticsRequestDTO request, String format);

    /**
     * Экспортировать командную статистику
     */
    byte[] exportTeamStatistics(LocalDate startDate, LocalDate endDate, String format, UUID teamId);

    /**
     * Сгенерировать CSV отчет
     */
    byte[] generateCsvReport(Map<String, Object> data, String filename);

    /**
     * Сгенерировать JSON отчет
     */
    byte[] generateJsonReport(Map<String, Object> data, String filename);

    /**
     * Сгенерировать Excel отчет
     */
    byte[] generateExcelReport(Map<String, Object> data, String filename);

    /**
     * Сгенерировать PDF отчет
     */
    byte[] generatePdfReport(Map<String, Object> data, String filename);

    /**
     * Создать кастомный отчет
     */
    byte[] createCustomReport(Map<String, Object> parameters, String format);

    /**
     * Получить шаблоны отчетов
     */
    Map<String, Object> getReportTemplates();

    /**
     * Сохранить шаблон отчета
     */
    void saveReportTemplate(String templateId, Map<String, Object> template);

    /**
     * Удалить шаблон отчета
     */
    void deleteReportTemplate(String templateId);

    /**
     * Валидировать параметры отчета
     */
    boolean validateReportParameters(Map<String, Object> parameters);

    /**
     * Получить предпросмотр отчета
     */
    Map<String, Object> getReportPreview(StatisticsRequestDTO request);

    /**
     * Получить метаданные отчета
     */
    Map<String, Object> getReportMetadata(String reportType);

    /**
     * Расписание генерации отчетов
     */
    void scheduleReportGeneration(String templateId, String cronExpression, Map<String, Object> parameters);

    /**
     * Отменить расписание генерации отчетов
     */
    void cancelScheduledReport(String scheduleId);

    /**
     * Получить список запланированных отчетов
     */
    List<Map<String, Object>> getScheduledReports();

    /**
     * Отправить отчет по email
     */
    void sendReportByEmail(String reportId, List<String> recipients);

    /**
     * Получить историю генерации отчетов
     */
    List<Map<String, Object>> getReportGenerationHistory(String templateId, LocalDate startDate, LocalDate endDate);

    /**
     * Архивировать старые отчеты
     */
    void archiveOldReports(int daysToKeep);

    /**
     * Восстановить архивированный отчет
     */
    void restoreArchivedReport(String reportId);

    /**
     * Получить статистику использования отчетов
     */
    Map<String, Object> getReportUsageStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Оптимизировать хранилище отчетов
     */
    void optimizeReportStorage();

    /**
     * Получить доступные форматы экспорта
     */
    List<String> getAvailableExportFormats();

    /**
     * Проверить статус системы отчетов
     */
    Map<String, Object> getReportSystemStatus();
}