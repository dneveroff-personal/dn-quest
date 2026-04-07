package dn.quest.notification.service.channel;

import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * Реализация Email канала доставки уведомлений
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationChannel implements NotificationChannel {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.notification.email.from-name:DN Quest}")
    private String fromName;

    @Override
    public String getChannelType() {
        return "email";
    }

    @Override
    public boolean isAvailable() {
        return mailSender != null && fromEmail != null && !fromEmail.isEmpty();
    }

    @Override
    public boolean canSend(Notification notification) {
        if (notification.getType() != NotificationType.EMAIL) {
            return false;
        }

        String recipientEmail = notification.getRecipientEmail();
        return recipientEmail != null && !recipientEmail.isEmpty() && isValidEmail(recipientEmail);
    }

    @Override
    public boolean validate(Notification notification) {
        if (notification.getSubject() == null || notification.getSubject().isEmpty()) {
            log.warn("Email notification validation failed: missing subject");
            return false;
        }

        if (notification.getContent() == null || notification.getContent().isEmpty()) {
            log.warn("Email notification validation failed: missing content");
            return false;
        }

        return canSend(notification);
    }

    @Override
    public NotificationChannelResult send(Notification notification) {
        if (!validate(notification)) {
            return NotificationChannelResult.failure("Email notification validation failed");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Настройка отправителя и получателя
            helper.setFrom(fromEmail, fromName);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getSubject());

            // Определение содержимого письма
            if (notification.getHtmlContent() != null && !notification.getHtmlContent().isEmpty()) {
                // Если есть HTML шаблон с переменными, обрабатываем его
                String processedHtml = processTemplate(notification.getHtmlContent(), notification);
                helper.setText(processedHtml, true);
            } else if (notification.getHtmlContent() != null) {
                // Если есть готовый HTML
                helper.setText(notification.getHtmlContent(), true);
            } else {
                // Только текстовое содержимое
                helper.setText(notification.getContent(), false);
            }

            // Отправка письма
            mailSender.send(message);

            log.info("Email sent successfully to: {} for notification: {}", 
                    notification.getRecipientEmail(), notification.getNotificationId());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("recipient", notification.getRecipientEmail());
            metadata.put("subject", notification.getSubject());
            metadata.put("hasHtml", notification.getHtmlContent() != null);

            return NotificationChannelResult.success(
                    "email_" + notification.getNotificationId(), 
                    metadata
            );

        } catch (MessagingException e) {
            log.error("Failed to send email to: {} for notification: {}", 
                    notification.getRecipientEmail(), notification.getNotificationId(), e);
            return NotificationChannelResult.failure("Email sending failed: " + e.getMessage(), "EMAIL_SEND_ERROR");
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {} for notification: {}", 
                    notification.getRecipientEmail(), notification.getNotificationId(), e);
            return NotificationChannelResult.failure("Unexpected error: " + e.getMessage(), "EMAIL_UNKNOWN_ERROR");
        }
    }

    @Override
    public double getCost(Notification notification) {
        // Email обычно бесплатный или очень дешевый
        return 0.001;
    }

    @Override
    public int getPriority() {
        return 2; // Средний приоритет
    }

    /**
     * Обработка шаблона с подстановкой переменных
     */
    private String processTemplate(String template, Notification notification) {
        try {
            Context context = new Context();
            
            // Добавляем базовые переменные
            context.setVariable("subject", notification.getSubject());
            context.setVariable("content", notification.getContent());
            context.setVariable("notificationId", notification.getNotificationId());
            context.setVariable("userId", notification.getUserId());
            
            // Добавляем переменные из templateData если они есть
            if (notification.getTemplateData() != null && !notification.getTemplateData().isEmpty()) {
                // Здесь можно добавить парсинг JSON и добавление переменных в контекст
                // Для простоты пока пропустим
            }

            // Если шаблон содержит Thymeleaf разметку, обрабатываем его
            if (template.contains("${") || template.contains("th:")) {
                return templateEngine.process(template, context);
            }

            // Простая замена переменных вида {{variable}}
            return replaceSimpleVariables(template, notification);

        } catch (Exception e) {
            log.warn("Failed to process email template, using original content: {}", e.getMessage());
            return template;
        }
    }

    /**
     * Простая замена переменных вида {{variable}}
     */
    private String replaceSimpleVariables(String template, Notification notification) {
        String result = template;
        
        result = result.replace("{{subject}}", notification.getSubject() != null ? notification.getSubject() : "");
        result = result.replace("{{content}}", notification.getContent() != null ? notification.getContent() : "");
        result = result.replace("{{notificationId}}", notification.getNotificationId() != null ? notification.getNotificationId() : "");
        result = result.replace("{{userId}}", notification.getUserId() != null ? notification.getUserId().toString() : "");
        
        return result;
    }

    /**
     * Простая валидация email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}