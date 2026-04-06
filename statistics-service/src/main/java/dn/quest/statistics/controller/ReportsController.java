package dn.quest.statistics.controller;

import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для работы с отчетами и экспортом данных
 */
@RestController
@RequestMapping("/api/stats/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports API", description = "API для работы с отчетами и экспортом данных")
@SecurityRequirement(name = "bearerAuth")
public class ReportsController {

    private final ReportsService reportsService;

    /**
     * Сгенерировать и экспортировать отчет
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Сгенерировать и экспортировать отчет", description = "Генерирует отчет в указанном формате и возвращает его для скачивания")
    public ResponseEntity<byte[]> generateAndExportReport(
            @Valid @RequestBody StatisticsRequestDTO request,
            @Parameter(description = "Формат отчета (csv, json, excel, pdf)") 
            @RequestParam(defaultValue = "csv") String format) {
        
        log.info("Generating report in format: {} with request: {}", format, request);
        
        byte[] reportData = reportsService.generateReport(request, format);
        String filename = generateFilename(request, format);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaTypeForFormat(format));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(reportData.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
    }

    /**
     * Экспортировать статистику пользователей
     */
    @GetMapping("/export/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Экспортировать статистику пользователей", description = "Экспортирует статистику пользователей в указанном формате")
    public ResponseEntity<byte[]> exportUserStatistics(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Формат экспорта (csv, json, excel)") 
            @RequestParam(defaultValue = "csv") String format,
            
            @Parameter(description = "ID пользователя для фильтрации") 
            @RequestParam(required = false) UUID userId) {
        
        log.info("Exporting user statistics from {} to {} format: {} user: {}", 
                startDate, endDate, format, userId);
        
        byte[] data = reportsService.exportUserStatistics(startDate, endDate, format, userId);
        String filename = String.format("user_statistics_%s_to_%s.%s", 
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE),
                format);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaTypeForFormat(format));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * Экспортировать статистику квестов
     */
    @GetMapping("/export/quests")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Экспортировать статистику квестов", description = "Экспортирует статистику квестов в указанном формате")
    public ResponseEntity<byte[]> exportQuestStatistics(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Формат экспорта (csv, json, excel)") 
            @RequestParam(defaultValue = "csv") String format,
            
            @Parameter(description = "ID квеста для фильтрации") 
            @RequestParam(required = false) UUID questId,
            
            @Parameter(description = "ID автора для фильтрации") 
            @RequestParam(required = false) UUID authorId) {
        
        log.info("Exporting quest statistics from {} to {} format: {} quest: {} author: {}", 
                startDate, endDate, format, questId, authorId);
        
        byte[] data = reportsService.exportQuestStatistics(startDate, endDate, format, questId, authorId);
        String filename = String.format("quest_statistics_%s_to_%s.%s", 
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE),
                format);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaTypeForFormat(format));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * Экспортировать игровую статистику
     */
    @GetMapping("/export/games")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Экспортировать игровую статистику", description = "Экспортирует игровую статистику в указанном формате")
    public ResponseEntity<byte[]> exportGameStatistics(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Формат экспорта (csv, json, excel)") 
            @RequestParam(defaultValue = "csv") String format,
            
            @Parameter(description = "ID квеста для фильтрации") 
            @RequestParam(required = false) UUID questId,
            
            @Parameter(description = "ID пользователя для фильтрации") 
            @RequestParam(required = false) UUID userId) {
        
        log.info("Exporting game statistics from {} to {} format: {} quest: {} user: {}", 
                startDate, endDate, format, questId, userId);
        
        byte[] data = reportsService.exportGameStatistics(startDate, endDate, format, questId, userId);
        String filename = String.format("game_statistics_%s_to_%s.%s", 
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE),
                format);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaTypeForFormat(format));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * Экспортировать лидерборды
     */
    @GetMapping("/export/leaderboards")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Экспортировать лидерборды", description = "Экспортирует лидерборды в указанном формате")
    public ResponseEntity<byte[]> exportLeaderboards(
            @Parameter(description = "Тип лидерборда (users, quests, teams)") 
            @RequestParam String leaderboardType,
            
            @Parameter(description = "Период (daily, weekly, monthly, all_time)") 
            @RequestParam(defaultValue = "all_time") String period,
            
            @Parameter(description = "Дата для периодических лидербордов") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            
            @Parameter(description = "Формат экспорта (csv, json, excel)") 
            @RequestParam(defaultValue = "csv") String format) {
        
        log.info("Exporting leaderboard type: {} period: {} date: {} format: {}", 
                leaderboardType, period, date, format);
        
        byte[] data = reportsService.exportLeaderboards(leaderboardType, period, date, format);
        String filename = String.format("%s_leaderboard_%s_%s.%s", 
                leaderboardType,
                period,
                date != null ? date.format(DateTimeFormatter.ISO_DATE) : "all_time",
                format);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaTypeForFormat(format));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * Получить список доступных отчетов
     */
    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить список доступных отчетов", description = "Возвращает список доступных шаблонов отчетов")
    public ResponseEntity<List<Map<String, Object>>> getAvailableReportTemplates() {
        log.info("Getting available report templates");
        List<Map<String, Object>> templates = reportsService.getAvailableReportTemplates();
        return ResponseEntity.ok(templates);
    }

    /**
     * Создать отчет по шаблону
     */
    @PostMapping("/templates/{templateId}/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Создать отчет по шаблону", description = "Создает отчет на основе предустановленного шаблона")
    public ResponseEntity<byte[]> generateReportFromTemplate(
            @Parameter(description = "ID шаблона") 
            @PathVariable String templateId,
            
            @Parameter(description = "Параметры шаблона") 
            @RequestBody Map<String, Object> parameters,
            
            @Parameter(description = "Формат отчета") 
            @RequestParam(defaultValue = "csv") String format) {
        
        log.info("Generating report from template: {} format: {} parameters: {}", templateId, format, parameters);
        
        byte[] data = reportsService.generateReportFromTemplate(templateId, parameters, format);
        String filename = String.format("report_%s_%s.%s", templateId, 
                LocalDate.now().format(DateTimeFormatter.ISO_DATE), format);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaTypeForFormat(format));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * Получить статус генерации отчета
     */
    @GetMapping("/status/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить статус генерации отчета", description = "Возвращает статус асинхронной генерации отчета")
    public ResponseEntity<Map<String, Object>> getReportGenerationStatus(
            @Parameter(description = "ID отчета") 
            @PathVariable String reportId) {
        
        log.info("Getting report generation status for: {}", reportId);
        Map<String, Object> status = reportsService.getReportGenerationStatus(reportId);
        return ResponseEntity.ok(status);
    }

    /**
     * Скачать ранее сгенерированный отчет
     */
    @GetMapping("/download/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Скачать ранее сгенерированный отчет", description = "Скачивает ранее сгенерированный отчет по ID")
    public ResponseEntity<byte[]> downloadGeneratedReport(
            @Parameter(description = "ID отчета") 
            @PathVariable String reportId) {
        
        log.info("Downloading generated report: {}", reportId);
        
        Map<String, Object> reportInfo = reportsService.getGeneratedReportInfo(reportId);
        byte[] data = reportsService.downloadGeneratedReport(reportId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaTypeForFormat((String) reportInfo.get("format")));
        headers.setContentDispositionFormData("attachment", (String) reportInfo.get("filename"));
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * Получить список сгенерированных отчетов
     */
    @GetMapping("/generated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить список сгенерированных отчетов", description = "Возвращает список ранее сгенерированных отчетов")
    public ResponseEntity<List<Map<String, Object>>> getGeneratedReports(
            @Parameter(description = "Лимит записей") 
            @RequestParam(defaultValue = "50") int limit,
            
            @Parameter(description = "Смещение") 
            @RequestParam(defaultValue = "0") int offset) {
        
        log.info("Getting generated reports limit: {} offset: {}", limit, offset);
        List<Map<String, Object>> reports = reportsService.getGeneratedReports(limit, offset);
        return ResponseEntity.ok(reports);
    }

    /**
     * Удалить сгенерированный отчет
     */
    @DeleteMapping("/generated/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Удалить сгенерированный отчет", description = "Удаляет ранее сгенерированный отчет")
    public ResponseEntity<Void> deleteGeneratedReport(
            @Parameter(description = "ID отчета") 
            @PathVariable String reportId) {
        
        log.info("Deleting generated report: {}", reportId);
        reportsService.deleteGeneratedReport(reportId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Вспомогательный метод для получения MediaType в зависимости от формата
     */
    private MediaType getMediaTypeForFormat(String format) {
        return switch (format.toLowerCase()) {
            case "json" -> MediaType.APPLICATION_JSON;
            case "excel", "xlsx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "csv" -> MediaType.parseMediaType("text/csv");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    /**
     * Вспомогательный метод для генерации имени файла
     */
    private String generateFilename(StatisticsRequestDTO request, String format) {
        StringBuilder filename = new StringBuilder();
        filename.append("statistics_report");
        
        if (request.getStatisticsType() != null) {
            filename.append("_").append(request.getStatisticsType());
        }
        
        if (request.getStartDate() != null && request.getEndDate() != null) {
            filename.append("_")
                    .append(request.getStartDate().format(DateTimeFormatter.ISO_DATE))
                    .append("_to_")
                    .append(request.getEndDate().format(DateTimeFormatter.ISO_DATE));
        }
        
        filename.append("_")
                .append(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                .append(".")
                .append(format);
        
        return filename.toString();
    }
}