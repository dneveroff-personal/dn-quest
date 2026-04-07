package dn.quest.notification.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.notification.entity.NotificationTemplate;
import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationType;
import dn.quest.notification.repository.NotificationTemplateRepository;
import dn.quest.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления шаблонами уведомлений
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    @Value("${app.notification.template.default-language:ru}")
    private String defaultLanguage;

    @Value("${app.notification.template.max-versions:10}")
    private int maxVersions;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    @Override
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        log.info("Creating new template: {}", template.getTemplateId());

        // Проверка на существование
        if (templateRepository.existsByTemplateId(template.getTemplateId())) {
            throw new IllegalArgumentException("Template with ID " + template.getTemplateId() + " already exists");
        }

        // Валидация
        if (!validateTemplate(template)) {
            throw new IllegalArgumentException("Template validation failed");
        }

        // Установка значений по умолчанию
        if (template.getVersion() == null) {
            template.setVersion(1);
        }
        if (template.getActive() == null) {
            template.setActive(false);
        }
        if (template.getLanguage() == null) {
            template.setLanguage(defaultLanguage);
        }

        // Извлечение переменных из шаблона
        List<String> variables = extractVariables(template.getContentTemplate());
        if (template.getHtmlTemplate() != null) {
            variables.addAll(extractVariables(template.getHtmlTemplate()));
        }
        
        try {
            template.setTemplateVariables(objectMapper.writeValueAsString(variables));
        } catch (Exception e) {
            log.warn("Failed to serialize template variables", e);
            template.setTemplateVariables("[]");
        }

        NotificationTemplate saved = templateRepository.save(template);
        log.info("Template created successfully: {}", saved.getTemplateId());
        
        return saved;
    }

    @Override
    public NotificationTemplate updateTemplate(String templateId, NotificationTemplate template) {
        log.info("Updating template: {}", templateId);

        NotificationTemplate existing = templateRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        // Обновление полей
        existing.setName(template.getName());
        existing.setDescription(template.getDescription());
        existing.setSubjectTemplate(template.getSubjectTemplate());
        existing.setContentTemplate(template.getContentTemplate());
        existing.setHtmlTemplate(template.getHtmlTemplate());
        existing.setActive(template.getActive());
        existing.setUpdatedBy(template.getUpdatedBy());

        // Валидация
        if (!validateTemplate(existing)) {
            throw new IllegalArgumentException("Updated template validation failed");
        }

        // Извлечение переменных
        List<String> variables = extractVariables(existing.getContentTemplate());
        if (existing.getHtmlTemplate() != null) {
            variables.addAll(extractVariables(existing.getHtmlTemplate()));
        }
        
        try {
            existing.setTemplateVariables(objectMapper.writeValueAsString(variables));
        } catch (Exception e) {
            log.warn("Failed to serialize template variables", e);
        }

        NotificationTemplate saved = templateRepository.save(existing);
        log.info("Template updated successfully: {}", saved.getTemplateId());
        
        return saved;
    }

    @Override
    public void deleteTemplate(String templateId) {
        log.info("Deleting template: {}", templateId);
        
        NotificationTemplate template = templateRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        templateRepository.delete(template);
        log.info("Template deleted successfully: {}", templateId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationTemplate> getTemplate(String templateId) {
        return templateRepository.findByTemplateId(templateId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationTemplate> getActiveTemplate(NotificationType type, 
                                                           NotificationCategory category, 
                                                           String language) {
        return templateRepository.findByTypeAndCategoryAndLanguageAndActiveTrueOrderByVersionDesc(
                type, category, language != null ? language : defaultLanguage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getTemplatesByTypeAndCategory(NotificationType type, 
                                                                   NotificationCategory category) {
        return templateRepository.findByTypeAndCategoryOrderByVersionDesc(type, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getTemplatesByType(NotificationType type) {
        return templateRepository.findByTypeOrderByVersionDesc(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getTemplatesByCategory(NotificationCategory category) {
        return templateRepository.findByCategoryOrderByVersionDesc(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getActiveTemplates() {
        return templateRepository.findByActiveTrueOrderByVersionDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> searchTemplates(NotificationType type, 
                                                     NotificationCategory category, 
                                                     String language, 
                                                     Boolean active, 
                                                     String search) {
        return templateRepository.searchTemplates(type, category, language, active, search);
    }

    @Override
    public NotificationTemplate toggleTemplateStatus(String templateId, boolean active) {
        log.info("Toggling template {} status to: {}", templateId, active);
        
        NotificationTemplate template = templateRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        template.setActive(active);
        NotificationTemplate saved = templateRepository.save(template);
        
        log.info("Template {} status updated to: {}", templateId, active);
        return saved;
    }

    @Override
    public NotificationTemplate createNewVersion(String templateId, NotificationTemplate newVersion) {
        log.info("Creating new version for template: {}", templateId);
        
        NotificationTemplate existing = templateRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        // Деактивируем предыдущую версию
        existing.setActive(false);
        templateRepository.save(existing);

        // Создаем новую версию
        newVersion.setTemplateId(templateId);
        newVersion.setVersion(existing.getVersion() + 1);
        newVersion.setCreatedBy(existing.getCreatedBy());
        newVersion.setActive(true);

        return createTemplate(newVersion);
    }

    @Override
    @Transactional(readOnly = true)
    public String processTemplate(String templateId, Map<String, Object> variables) {
        NotificationTemplate template = getTemplate(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        return processTemplateContent(template.getContentTemplate(), variables);
    }

    @Override
    @Transactional(readOnly = true)
    public String processHtmlTemplate(String templateId, Map<String, Object> variables) {
        NotificationTemplate template = getTemplate(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        if (template.getHtmlTemplate() == null || template.getHtmlTemplate().isEmpty()) {
            return processTemplate(templateId, variables);
        }

        return processTemplateContent(template.getHtmlTemplate(), variables);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateTemplate(NotificationTemplate template) {
        if (template.getName() == null || template.getName().isEmpty()) {
            log.warn("Template validation failed: missing name");
            return false;
        }

        if (template.getType() == null) {
            log.warn("Template validation failed: missing type");
            return false;
        }

        if (template.getCategory() == null) {
            log.warn("Template validation failed: missing category");
            return false;
        }

        if (template.getContentTemplate() == null || template.getContentTemplate().isEmpty()) {
            log.warn("Template validation failed: missing content template");
            return false;
        }

        // Проверка синтаксиса шаблона
        try {
            Context context = new Context();
            templateEngine.process(template.getContentTemplate(), context);
            
            if (template.getHtmlTemplate() != null && !template.getHtmlTemplate().isEmpty()) {
                templateEngine.process(template.getHtmlTemplate(), context);
            }
        } catch (Exception e) {
            log.warn("Template validation failed: invalid template syntax", e);
            return false;
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTemplateVariables(String templateId) {
        NotificationTemplate template = getTemplate(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        try {
            return objectMapper.readValue(template.getTemplateVariables(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse template variables for: {}", templateId, e);
            return extractVariables(template.getContentTemplate());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> previewTemplate(String templateId, Map<String, Object> testData) {
        NotificationTemplate template = getTemplate(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        Map<String, String> preview = new HashMap<>();
        
        // Обработка темы
        if (template.getSubjectTemplate() != null) {
            preview.put("subject", processTemplateContent(template.getSubjectTemplate(), testData));
        }
        
        // Обработка текстового содержимого
        preview.put("content", processTemplateContent(template.getContentTemplate(), testData));
        
        // Обработка HTML содержимого
        if (template.getHtmlTemplate() != null && !template.getHtmlTemplate().isEmpty()) {
            preview.put("html", processTemplateContent(template.getHtmlTemplate(), testData));
        }

        return preview;
    }

    @Override
    public NotificationTemplate cloneTemplate(String templateId, String newTemplateId, String newName) {
        log.info("Cloning template {} to {}", templateId, newTemplateId);
        
        NotificationTemplate original = getTemplate(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        if (templateRepository.existsByTemplateId(newTemplateId)) {
            throw new IllegalArgumentException("Template with ID " + newTemplateId + " already exists");
        }

        NotificationTemplate cloned = NotificationTemplate.builder()
                .templateId(newTemplateId)
                .name(newName)
                .description(original.getDescription() + " (cloned)")
                .type(original.getType())
                .category(original.getCategory())
                .language(original.getLanguage())
                .subjectTemplate(original.getSubjectTemplate())
                .contentTemplate(original.getContentTemplate())
                .htmlTemplate(original.getHtmlTemplate())
                .templateVariables(original.getTemplateVariables())
                .active(false) // Клон неактивен по умолчанию
                .version(1)
                .createdBy(original.getCreatedBy())
                .metadata(original.getMetadata())
                .build();

        return createTemplate(cloned);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTemplates(List<String> templateIds) {
        try {
            List<NotificationTemplate> templates;
            if (templateIds == null || templateIds.isEmpty()) {
                templates = getActiveTemplates();
            } else {
                templates = templateIds.stream()
                        .map(templateId -> getTemplate(templateId).orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }

            return objectMapper.writeValueAsBytes(templates);
        } catch (Exception e) {
            log.error("Failed to export templates", e);
            throw new RuntimeException("Export failed", e);
        }
    }

    @Override
    public List<NotificationTemplate> importTemplates(byte[] data) {
        try {
            List<NotificationTemplate> templates = objectMapper.readValue(data, 
                    new TypeReference<List<NotificationTemplate>>() {});

            List<NotificationTemplate> imported = new ArrayList<>();
            for (NotificationTemplate template : templates) {
                if (!templateRepository.existsByTemplateId(template.getTemplateId())) {
                    template.setId(null); // Сброс ID для создания новой записи
                    template.setActive(false); // Импортированные шаблоны неактивны
                    imported.add(createTemplate(template));
                }
            }

            log.info("Imported {} templates", imported.size());
            return imported;
        } catch (IOException e) {
            log.error("Failed to import templates", e);
            throw new RuntimeException("Import failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTemplateStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalTemplates", templateRepository.count());
        stats.put("activeTemplates", templateRepository.countActiveTemplates());
        
        // Статистика по типам
        Map<String, Long> typeStats = Arrays.stream(NotificationType.values())
                .collect(Collectors.toMap(
                        NotificationType::getValue,
                        type -> templateRepository.countByType(type)
                ));
        stats.put("templatesByType", typeStats);
        
        // Статистика по категориям
        Map<String, Long> categoryStats = Arrays.stream(NotificationCategory.values())
                .collect(Collectors.toMap(
                        NotificationCategory::getValue,
                        category -> templateRepository.countByCategory(category)
                ));
        stats.put("templatesByCategory", categoryStats);

        return stats;
    }

    /**
     * Обработка содержимого шаблона с подстановкой переменных
     */
    private String processTemplateContent(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        try {
            Context context = new Context();
            variables.forEach(context::setVariable);
            return templateEngine.process(template, context);
        } catch (Exception e) {
            log.warn("Failed to process template with Thymeleaf, using simple replacement", e);
            return replaceSimpleVariables(template, variables);
        }
    }

    /**
     * Простая замена переменных вида {{variable}}
     */
    private String replaceSimpleVariables(String template, Map<String, Object> variables) {
        String result = template;
        
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }

    /**
     * Извлечение переменных из шаблона
     */
    private List<String> extractVariables(String template) {
        if (template == null || template.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> variables = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return new ArrayList<>(variables);
    }
}