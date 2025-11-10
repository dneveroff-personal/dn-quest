package dn.quest.notification.service;

import dn.quest.notification.entity.NotificationTemplate;
import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для управления шаблонами уведомлений
 */
public interface NotificationTemplateService {

    /**
     * Создать новый шаблон
     */
    NotificationTemplate createTemplate(NotificationTemplate template);

    /**
     * Обновить существующий шаблон
     */
    NotificationTemplate updateTemplate(String templateId, NotificationTemplate template);

    /**
     * Удалить шаблон
     */
    void deleteTemplate(String templateId);

    /**
     * Получить шаблон по ID
     */
    Optional<NotificationTemplate> getTemplate(String templateId);

    /**
     * Получить активный шаблон по типу, категории и языку
     */
    Optional<NotificationTemplate> getActiveTemplate(NotificationType type, 
                                                   NotificationCategory category, 
                                                   String language);

    /**
     * Получить все шаблоны по типу и категории
     */
    List<NotificationTemplate> getTemplatesByTypeAndCategory(NotificationType type, 
                                                           NotificationCategory category);

    /**
     * Получить все шаблоны по типу
     */
    List<NotificationTemplate> getTemplatesByType(NotificationType type);

    /**
     * Получить все шаблоны по категории
     */
    List<NotificationTemplate> getTemplatesByCategory(NotificationCategory category);

    /**
     * Получить все активные шаблоны
     */
    List<NotificationTemplate> getActiveTemplates();

    /**
     * Поиск шаблонов
     */
    List<NotificationTemplate> searchTemplates(NotificationType type, 
                                              NotificationCategory category, 
                                              String language, 
                                              Boolean active, 
                                              String search);

    /**
     * Активировать/деактивировать шаблон
     */
    NotificationTemplate toggleTemplateStatus(String templateId, boolean active);

    /**
     * Создать новую версию шаблона
     */
    NotificationTemplate createNewVersion(String templateId, NotificationTemplate newVersion);

    /**
     * Обработать шаблон с подстановкой переменных
     */
    String processTemplate(String templateId, Map<String, Object> variables);

    /**
     * Обработать шаблон с подстановкой переменных (для HTML)
     */
    String processHtmlTemplate(String templateId, Map<String, Object> variables);

    /**
     * Валидировать шаблон
     */
    boolean validateTemplate(NotificationTemplate template);

    /**
     * Получить переменные шаблона
     */
    List<String> getTemplateVariables(String templateId);

    /**
     * Предпросмотр шаблона с тестовыми данными
     */
    Map<String, String> previewTemplate(String templateId, Map<String, Object> testData);

    /**
     * Клонировать шаблон
     */
    NotificationTemplate cloneTemplate(String templateId, String newTemplateId, String newName);

    /**
     * Экспортировать шаблоны
     */
    byte[] exportTemplates(List<String> templateIds);

    /**
     * Импортировать шаблоны
     */
    List<NotificationTemplate> importTemplates(byte[] data);

    /**
     * Получить статистику по шаблонам
     */
    Map<String, Object> getTemplateStatistics();
}