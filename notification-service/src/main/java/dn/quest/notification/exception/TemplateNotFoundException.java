package dn.quest.notification.exception;

/**
 * Исключение для случаев, когда шаблон уведомления не найден
 */
public class TemplateNotFoundException extends NotificationException {
    
    public TemplateNotFoundException(String message) {
        super(message);
    }
    
    public TemplateNotFoundException(String templateId, String language) {
        super(String.format("Шаблон уведомления не найден: id=%s, language=%s", templateId, language));
    }
}