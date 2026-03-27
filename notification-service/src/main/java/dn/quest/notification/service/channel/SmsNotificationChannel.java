package dn.quest.notification.service.channel;

import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация SMS канала доставки уведомлений
 * Поддерживает различных SMS провайдеров через конфигурацию
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationChannel implements NotificationChannel {

    private final RestTemplate restTemplate;

    @Value("${app.notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.notification.sms.provider:twilio}")
    private String smsProvider;

    @Value("${app.notification.sms.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${app.notification.sms.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${app.notification.sms.twilio.from-number:}")
    private String twilioFromNumber;

    @Value("${app.notification.sms.max-length:160}")
    private int maxSmsLength;

    @Value("${app.notification.sms.api-url:}")
    private String smsApiUrl;

    @Value("${app.notification.sms.api-key:}")
    private String smsApiKey;

    @Override
    public String getChannelType() {
        return "sms";
    }

    @Override
    public boolean isAvailable() {
        if (!smsEnabled) {
            return false;
        }

        switch (smsProvider.toLowerCase()) {
            case "twilio":
                return twilioAccountSid != null && !twilioAccountSid.isEmpty() &&
                       twilioAuthToken != null && !twilioAuthToken.isEmpty() &&
                       twilioFromNumber != null && !twilioFromNumber.isEmpty();
            case "custom":
                return smsApiUrl != null && !smsApiUrl.isEmpty() &&
                       smsApiKey != null && !smsApiKey.isEmpty();
            default:
                return false;
        }
    }

    @Override
    public boolean canSend(Notification notification) {
        if (notification.getType() != NotificationType.SMS) {
            return false;
        }

        String recipientPhone = notification.getRecipientPhone();
        return recipientPhone != null && !recipientPhone.isEmpty() && isValidPhoneNumber(recipientPhone);
    }

    @Override
    public boolean validate(Notification notification) {
        if (notification.getContent() == null || notification.getContent().isEmpty()) {
            log.warn("SMS notification validation failed: missing content");
            return false;
        }

        // Проверка длины SMS
        if (notification.getContent().length() > maxSmsLength) {
            log.warn("SMS notification validation failed: content too long (max {} chars)", maxSmsLength);
            return false;
        }

        return canSend(notification);
    }

    @Override
    public NotificationChannelResult send(Notification notification) {
        if (!validate(notification)) {
            return NotificationChannelResult.failure("SMS notification validation failed");
        }

        try {
            String messageId;
            
            switch (smsProvider.toLowerCase()) {
                case "twilio":
                    messageId = sendViaTwilio(notification);
                    break;
                case "custom":
                    messageId = sendViaCustomApi(notification);
                    break;
                default:
                    return NotificationChannelResult.failure("Unsupported SMS provider: " + smsProvider);
            }

            log.info("SMS sent successfully to: {} for notification: {}", 
                    notification.getRecipientPhone(), notification.getNotificationId());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("recipient", notification.getRecipientPhone());
            metadata.put("provider", smsProvider);
            metadata.put("length", notification.getContent().length());

            return NotificationChannelResult.success(messageId, metadata);

        } catch (Exception e) {
            log.error("Failed to send SMS to: {} for notification: {}", 
                    notification.getRecipientPhone(), notification.getNotificationId(), e);
            return NotificationChannelResult.failure(
                    "SMS sending failed: " + e.getMessage(), 
                    "SMS_SEND_ERROR"
            );
        }
    }

    @Override
    public double getCost(Notification notification) {
        // SMS обычно платные, стоимость зависит от провайдера и страны
        return 0.05; // Примерная стоимость
    }

    @Override
    public int getPriority() {
        return 1; // Низкий приоритет из-за стоимости
    }

    /**
     * Отправка SMS через Twilio
     */
    private String sendViaTwilio(Notification notification) {
        String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", twilioAccountSid);
        
        // Формирование запроса
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("To", notification.getRecipientPhone());
        requestBody.put("From", twilioFromNumber);
        requestBody.put("Body", notification.getContent());

        // Аутентификация Basic Auth
        String credentials = twilioAccountSid + ":" + twilioAuthToken;
        String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

        // Заголовки
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encodedCredentials);
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        // Отправка запроса
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

        if (response != null && response.containsKey("sid")) {
            return (String) response.get("sid");
        } else {
            throw new RuntimeException("Twilio API response missing SID");
        }
    }

    /**
     * Отправка SMS через кастомный API
     */
    private String sendViaCustomApi(Notification notification) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("to", notification.getRecipientPhone());
        requestBody.put("message", notification.getContent());
        requestBody.put("notificationId", notification.getNotificationId());

        // Заголовки
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + smsApiKey);
        headers.put("Content-Type", "application/json");

        // Отправка запроса
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(smsApiUrl, requestBody, Map.class);

        if (response != null && response.containsKey("messageId")) {
            return (String) response.get("messageId");
        } else if (response != null && response.containsKey("id")) {
            return (String) response.get("id");
        } else {
            throw new RuntimeException("Custom SMS API response missing message ID");
        }
    }

    /**
     * Простая валидация номера телефона
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }

        // Удаляем все нецифровые символы кроме +
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        
        // Базовая проверка формата
        return cleanPhone.matches("^\\+?[1-9]\\d{6,14}$");
    }

    /**
     * Форматирование номера телефона
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }

        // Удаляем все нецифровые символы кроме +
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        
        // Если номер начинается с 8 и это российский номер, заменяем на +7
        if (cleanPhone.startsWith("8") && cleanPhone.length() == 11) {
            return "+7" + cleanPhone.substring(1);
        }
        
        // Если номер не начинается с +, добавляем его
        if (!cleanPhone.startsWith("+")) {
            return "+" + cleanPhone;
        }
        
        return cleanPhone;
    }

    /**
     * Проверка баланса SMS провайдера
     */
    public boolean checkBalance() {
        try {
            switch (smsProvider.toLowerCase()) {
                case "twilio":
                    return checkTwilioBalance();
                case "custom":
                    return checkCustomApiBalance();
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("Failed to check SMS balance", e);
            return false;
        }
    }

    /**
     * Проверка баланса Twilio
     */
    private boolean checkTwilioBalance() {
        String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Balance.json", twilioAccountSid);
        
        String credentials = twilioAccountSid + ":" + twilioAuthToken;
        String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("balance")) {
                String balance = (String) response.get("balance");
                double balanceValue = Double.parseDouble(balance);
                return balanceValue > 1.0; // Минимальный баланс $1
            }
        } catch (Exception e) {
            log.warn("Failed to check Twilio balance", e);
        }
        
        return false;
    }

    /**
     * Проверка баланса кастомного API
     */
    private boolean checkCustomApiBalance() {
        // Реализация зависит от конкретного API
        return true; // По умолчанию считаем, что баланс есть
    }
}