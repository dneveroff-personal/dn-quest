package dn.quest.shared.config;

import dn.quest.gateway.client.AuthenticationServiceClient;
import dn.quest.shared.events.EventProducer;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Конфигурация для мокирования внешних сервисов в тестах
 */
@Slf4j
@TestConfiguration
@Profile("test")
public class TestMockConfig {

    /**
     * Мок для AuthenticationServiceClient
     */
    @Bean
    @Primary
    public AuthenticationServiceClient mockAuthenticationServiceClient() {
        AuthenticationServiceClient mockClient = Mockito.mock(AuthenticationServiceClient.class);
        
        // Настройка базовых ответов
        when(mockClient.validateToken(anyString()))
                .thenReturn(dn.quest.gateway.dto.TokenValidationResponse.builder()
                        .valid(true)
                        .userId(1L)
                        .username("testuser")
                        .role("PLAYER")
                        .build());
        
        log.debug("Created mock AuthenticationServiceClient");
        return mockClient;
    }

    /**
     * Мок для EventProducer
     */
    @Bean
    @Primary
    public EventProducer mockEventProducer() {
        EventProducer mockProducer = Mockito.mock(EventProducer.class);
        
        // Настройка базовых ответов
        doNothing().when(mockProducer).publishEvent(anyString(), any());
        doNothing().when(mockProducer).publishUserEvent(any());
        doNothing().when(mockProducer).publishQuestEvent(any());
        doNothing().when(mockProducer).publishGameEvent(any());
        doNothing().when(mockProducer).publishTeamEvent(any());
        doNothing().when(mockProducer).publishFileEvent(any());
        doNothing().when(mockProducer).publishNotificationEvent(any());
        doNothing().when(mockProducer).publishStatisticsEvent(any());
        
        log.debug("Created mock EventProducer");
        return mockProducer;
    }

    /**
     * Мок для RestTemplate
     */
    @Bean
    @Primary
    public RestTemplate mockRestTemplate() {
        RestTemplate mockTemplate = Mockito.mock(RestTemplate.class);
        
        // Настройка базовых ответов для внешних API
        when(mockTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn("mock response");
        
        log.debug("Created mock RestTemplate");
        return mockTemplate;
    }

    /**
     * Мок для JavaMailSender
     */
    @Bean
    @Primary
    public JavaMailSender mockJavaMailSender() {
        JavaMailSender mockMailSender = Mockito.mock(JavaMailSender.class);
        
        // Настройка базовых ответов
        doNothing().when(mockMailSender).send(any());
        
        log.debug("Created mock JavaMailSender");
        return mockMailSender;
    }

    /**
     * Фабрика для создания настроенных моков
     */
    @Bean
    @Primary
    public MockFactory mockFactory() {
        return new MockFactory();
    }

    /**
     * Фабрика для создания и настройки моков
     */
    public static class MockFactory {
        
        /**
         * Создание мока с настройками для успешной валидации токена
         */
        public AuthenticationServiceClient createSuccessfulAuthClient() {
            AuthenticationServiceClient mockClient = Mockito.mock(AuthenticationServiceClient.class);
            
            when(mockClient.validateToken(anyString()))
                    .thenReturn(dn.quest.gateway.dto.TokenValidationResponse.builder()
                            .valid(true)
                            .userId(1L)
                            .username("testuser")
                            .role("PLAYER")
                            .build());
            
            return mockClient;
        }

        /**
         * Создание мока с настройками для неуспешной валидации токена
         */
        public AuthenticationServiceClient createUnsuccessfulAuthClient() {
            AuthenticationServiceClient mockClient = Mockito.mock(AuthenticationServiceClient.class);
            
            when(mockClient.validateToken(anyString()))
                    .thenReturn(dn.quest.gateway.dto.TokenValidationResponse.builder()
                            .valid(false)
                            .build());
            
            return mockClient;
        }

        /**
         * Создание мока с настройками для администратора
         */
        public AuthenticationServiceClient createAdminAuthClient() {
            AuthenticationServiceClient mockClient = Mockito.mock(AuthenticationServiceClient.class);
            
            when(mockClient.validateToken(anyString()))
                    .thenReturn(dn.quest.gateway.dto.TokenValidationResponse.builder()
                            .valid(true)
                            .userId(1L)
                            .username("admin")
                            .role("ADMIN")
                            .build());
            
            return mockClient;
        }

        /**
         * Создание мока с настройками для выброса исключения
         */
        public AuthenticationServiceClient createFailingAuthClient() {
            AuthenticationServiceClient mockClient = Mockito.mock(AuthenticationServiceClient.class);
            
            when(mockClient.validateToken(anyString()))
                    .thenThrow(new RuntimeException("Service unavailable"));
            
            return mockClient;
        }

        /**
         * Создание мока EventProducer с отслеживанием вызовов
         */
        public EventProducer createTrackingEventProducer() {
            EventProducer mockProducer = Mockito.mock(EventProducer.class);
            
            doNothing().when(mockProducer).publishEvent(anyString(), any());
            doNothing().when(mockProducer).publishUserEvent(any());
            doNothing().when(mockProducer).publishQuestEvent(any());
            doNothing().when(mockProducer).publishGameEvent(any());
            doNothing().when(mockProducer).publishTeamEvent(any());
            doNothing().when(mockProducer).publishFileEvent(any());
            doNothing().when(mockProducer).publishNotificationEvent(any());
            doNothing().when(mockProducer).publishStatisticsEvent(any());
            
            return mockProducer;
        }

        /**
         * Создание мока RestTemplate с настройками для внешних API
         */
        public RestTemplate createExternalApiRestTemplate() {
            RestTemplate mockTemplate = Mockito.mock(RestTemplate.class);
            
            // Настройка ответов для различных типов запросов
            when(mockTemplate.getForObject(anyString(), any(Class.class)))
                    .thenReturn("mock response");
            
            when(mockTemplate.postForObject(anyString(), any(), any(Class.class)))
                    .thenReturn("mock post response");
            
            when(mockTemplate.exchange(anyString(), any(), any(), any(Class.class)))
                    .thenReturn(new org.springframework.http.ResponseEntity<>("mock exchange response", 
                            org.springframework.http.HttpStatus.OK));
            
            return mockTemplate;
        }

        /**
         * Создание мока RestTemplate с настройками для медленных ответов
         */
        public RestTemplate createSlowRestTemplate() {
            RestTemplate mockTemplate = Mockito.mock(RestTemplate.class);
            
            // Имитация медленного ответа
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            when(mockTemplate.getForObject(anyString(), any(Class.class)))
                    .thenReturn("slow mock response");
            
            return mockTemplate;
        }

        /**
         * Создание мока RestTemplate с настройками для выброса исключений
         */
        public RestTemplate createFailingRestTemplate() {
            RestTemplate mockTemplate = Mockito.mock(RestTemplate.class);
            
            when(mockTemplate.getForObject(anyString(), any(Class.class)))
                    .thenThrow(new RuntimeException("External service unavailable"));
            
            when(mockTemplate.postForObject(anyString(), any(), any(Class.class)))
                    .thenThrow(new RuntimeException("External service unavailable"));
            
            return mockTemplate;
        }
    }

    /**
     * Конфигурация для тестовых сценариев
     */
    @Bean
    @Primary
    public TestScenarioConfig testScenarioConfig() {
        return new TestScenarioConfig();
    }

    /**
     * Конфигурация тестовых сценариев
     */
    public static class TestScenarioConfig {
        
        /**
         * Сценарий успешной работы всех сервисов
         */
        public void configureAllServicesSuccess() {
            log.info("Configuring all services for success scenario");
        }

        /**
         * Сценарий недоступности внешних сервисов
         */
        public void configureExternalServicesFailure() {
            log.info("Configuring external services for failure scenario");
        }

        /**
         * Сценарий медленных ответов сервисов
         */
        public void configureSlowServices() {
            log.info("Configuring services for slow response scenario");
        }

        /**
         * Сценарий частичных сбоев
         */
        public void configurePartialFailures() {
            log.info("Configuring services for partial failure scenario");
        }
    }
}