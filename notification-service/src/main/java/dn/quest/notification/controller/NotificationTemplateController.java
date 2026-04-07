package dn.quest.notification.controller;

import dn.quest.notification.dto.ApiResponse;
import dn.quest.notification.dto.PagedResponse;
import dn.quest.notification.entity.NotificationTemplate;
import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationType;
import dn.quest.notification.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST контроллер для управления шаблонами уведомлений
 */
@RestController
@RequestMapping("/api/notifications/templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Templates", description = "API для управления шаблонами уведомлений")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    @Operation(summary = "Создать шаблон", description = "Создать новый шаблон уведомления")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationTemplate>> createTemplate(
            @Valid @RequestBody NotificationTemplate template) {
        
        log.info("Creating notification template: {}", template.getTemplateId());
        
        try {
            NotificationTemplate created = templateService.createTemplate(template);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Template created successfully"));
            
        } catch (Exception e) {
            log.error("Error creating template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create template: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить шаблон", description = "Получить шаблон по ID")
    @GetMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<NotificationTemplate>> getTemplate(
            @Parameter(description = "ID шаблона") @PathVariable String templateId) {
        
        log.info("Getting notification template: {}", templateId);
        
        try {
            Optional<NotificationTemplate> template = templateService.getTemplate(templateId);
            
            if (template.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(template.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found: " + templateId));
            }
            
        } catch (Exception e) {
            log.error("Error getting template: {}", templateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get template: " + e.getMessage()));
        }
    }

    @Operation(summary = "Обновить шаблон", description = "Обновить существующий шаблон")
    @PutMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationTemplate>> updateTemplate(
            @Parameter(description = "ID шаблона") @PathVariable String templateId,
            @Valid @RequestBody NotificationTemplate template) {
        
        log.info("Updating notification template: {}", templateId);
        
        try {
            NotificationTemplate updated = templateService.updateTemplate(templateId, template);
            
            return ResponseEntity.ok(ApiResponse.success(updated, "Template updated successfully"));
            
        } catch (Exception e) {
            log.error("Error updating template: {}", templateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update template: " + e.getMessage()));
        }
    }

    @Operation(summary = "Удалить шаблон", description = "Удалить шаблон по ID")
    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @Parameter(description = "ID шаблона") @PathVariable String templateId) {
        
        log.info("Deleting notification template: {}", templateId);
        
        try {
            templateService.deleteTemplate(templateId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Template deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting template: {}", templateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete template: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить активный шаблон", description = "Получить активный шаблон по типу, категории и языку")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<NotificationTemplate>> getActiveTemplate(
            @Parameter(description = "Тип уведомления") @RequestParam NotificationType type,
            @Parameter(description = "Категория уведомления") @RequestParam NotificationCategory category,
            @Parameter(description = "Язык") @RequestParam(defaultValue = "ru") String language) {
        
        log.info("Getting active template: type={}, category={}, language={}", type, category, language);
        
        try {
            Optional<NotificationTemplate> template = templateService.getActiveTemplate(type, category, language);
            
            if (template.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(template.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Active template not found"));
            }
            
        } catch (Exception e) {
            log.error("Error getting active template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get active template: " + e.getMessage()));
        }
    }

    @Operation(summary = "Поиск шаблонов", description = "Поиск шаблонов по параметрам")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<NotificationTemplate>>> searchTemplates(
            @Parameter(description = "Тип уведомления") @RequestParam(required = false) NotificationType type,
            @Parameter(description = "Категория уведомления") @RequestParam(required = false) NotificationCategory category,
            @Parameter(description = "Язык") @RequestParam(required = false) String language,
            @Parameter(description = "Активен ли шаблон") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Поисковый запрос") @RequestParam(required = false) String search) {
        
        log.info("Searching templates: type={}, category={}, language={}, active={}, search={}", 
                type, category, language, active, search);
        
        try {
            List<NotificationTemplate> templates = templateService.searchTemplates(type, category, language, active, search);
            
            return ResponseEntity.ok(ApiResponse.success(templates));
            
        } catch (Exception e) {
            log.error("Error searching templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search templates: " + e.getMessage()));
        }
    }

    @Operation(summary = "Переключить статус шаблона", description = "Активировать или деактивировать шаблон")
    @PutMapping("/{templateId}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationTemplate>> toggleTemplateStatus(
            @Parameter(description = "ID шаблона") @PathVariable String templateId,
            @Parameter(description = "Новый статус") @RequestParam boolean active) {
        
        log.info("Toggling template {} status to: {}", templateId, active);
        
        try {
            NotificationTemplate template = templateService.toggleTemplateStatus(templateId, active);
            
            return ResponseEntity.ok(ApiResponse.success(template, "Template status updated successfully"));
            
        } catch (Exception e) {
            log.error("Error toggling template status: {}", templateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to toggle template status: " + e.getMessage()));
        }
    }

    @Operation(summary = "Создать новую версию шаблона", description = "Создать новую версию существующего шаблона")
    @PostMapping("/{templateId}/version")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationTemplate>> createNewVersion(
            @Parameter(description = "ID шаблона") @PathVariable String templateId,
            @Valid @RequestBody NotificationTemplate newVersion) {
        
        log.info("Creating new version for template: {}", templateId);
        
        try {
            NotificationTemplate template = templateService.createNewVersion(templateId, newVersion);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(template, "New version created successfully"));
            
        } catch (Exception e) {
            log.error("Error creating new version for template: {}", templateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create new version: " + e.getMessage()));
        }
    }

    @Operation(summary = "Предпросмотр шаблона", description = "Получить предпросмотр шаблона с тестовыми данными")
    @PostMapping("/{templateId}/preview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> previewTemplate(
            @Parameter(description = "ID шаблона") @PathVariable String templateId,
            @RequestBody Map<String, Object> testData) {
        
        log.info("Previewing template: {}", templateId);
        
        try {
            Map<String, String> preview = templateService.previewTemplate(templateId, testData);
            
            return ResponseEntity.ok(ApiResponse.success(preview));
            
        } catch (Exception e) {
            log.error("Error previewing template: {}", templateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to preview template: " + e.getMessage()));
        }
    }

    @Operation(summary = "Клонировать шаблон", description = "Создать копию существующего шаблона")
    @PostMapping("/{templateId}/clone")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationTemplate>> cloneTemplate(
            @Parameter(description = "ID шаблона") @PathVariable String templateId,
            @RequestBody CloneTemplateRequest request) {
        
        log.info("Cloning template: {} to {}", templateId, request.getNewTemplateId());
        
        try {
            NotificationTemplate cloned = templateService.cloneTemplate(templateId, request.getNewTemplateId(), request.getNewName());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(cloned, "Template cloned successfully"));
            
        } catch (Exception e) {
            log.error("Error cloning template: {}", templateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to clone template: " + e.getMessage()));
        }
    }

    @Operation(summary = "Экспортировать шаблоны", description = "Экспортировать шаблоны в JSON файл")
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportTemplates(
            @Parameter(description = "Список ID шаблонов для экспорта") @RequestParam(required = false) List<String> templateIds) {
        
        log.info("Exporting templates: {}", templateIds);
        
        try {
            byte[] data = templateService.exportTemplates(templateIds);
            ByteArrayResource resource = new ByteArrayResource(data);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=templates.json")
                    .body(resource);
            
        } catch (Exception e) {
            log.error("Error exporting templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(summary = "Импортировать шаблоны", description = "Импортировать шаблоны из JSON файла")
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationTemplate>>> importTemplates(
            @Parameter(description = "Файл с шаблонами") @RequestParam("file") MultipartFile file) {
        
        log.info("Importing templates from file: {}", file.getOriginalFilename());
        
        try {
            List<NotificationTemplate> templates = templateService.importTemplates(file.getBytes());
            
            return ResponseEntity.ok(ApiResponse.success(templates, "Templates imported successfully"));
            
        } catch (Exception e) {
            log.error("Error importing templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to import templates: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить статистику шаблонов", description = "Получить статистику по шаблонам")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> getTemplateStatistics() {
        
        log.info("Getting template statistics");
        
        try {
            Object statistics = templateService.getTemplateStatistics();
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            log.error("Error getting template statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get template statistics: " + e.getMessage()));
        }
    }

    /**
     * DTO для запроса клонирования шаблона
     */
    public static class CloneTemplateRequest {
        private String newTemplateId;
        private String newName;

        public String getNewTemplateId() {
            return newTemplateId;
        }

        public void setNewTemplateId(String newTemplateId) {
            this.newTemplateId = newTemplateId;
        }

        public String getNewName() {
            return newName;
        }

        public void setNewName(String newName) {
            this.newName = newName;
        }
    }
}