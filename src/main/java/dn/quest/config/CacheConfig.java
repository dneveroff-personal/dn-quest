package dn.quest.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация кэширования для оптимизации производительности
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * Настройка менеджера кэша с различными TTL для разных типов данных
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Кэш для пользователей - 30 минут
        cacheManager.registerCustomCache("users", Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats()
                .build());
        
        // Кэш для квестов - 15 минут
        cacheManager.registerCustomCache("quests", Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats()
                .build());
        
        // Кэш для команд - 20 минут
        cacheManager.registerCustomCache("teams", Caffeine.newBuilder()
                .expireAfterWrite(20, TimeUnit.MINUTES)
                .maximumSize(200)
                .recordStats()
                .build());
        
        // Кэш для игровых сессий - 10 минут
        cacheManager.registerCustomCache("gameSessions", Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats()
                .build());
        
        // Кэш для уровней - 30 минут
        cacheManager.registerCustomCache("levels", Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(2000)
                .recordStats()
                .build());
        
        // Кэш для кодов - 60 минут
        cacheManager.registerCustomCache("codes", Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .maximumSize(5000)
                .recordStats()
                .build());
        
        // Кэш для попыток кода - 5 минут
        cacheManager.registerCustomCache("codeAttempts", Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10000)
                .recordStats()
                .build());
        
        // Кэш для прогресса уровней - 15 минут
        cacheManager.registerCustomCache("levelProgress", Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(2000)
                .recordStats()
                .build());
        
        // Кэш для приглашений - 10 минут
        cacheManager.registerCustomCache("invitations", Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats()
                .build());
        
        // Кэш для статистики - 5 минут
        cacheManager.registerCustomCache("statistics", Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .recordStats()
                .build());
        
        log.info("Cache manager initialized with custom caches");
        return cacheManager;
    }

    /**
     * Кэш для часто используемых запросов
     */
    @Bean("userCache")
    public Cache<String, Object> userCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats()
                .build();
    }

    /**
     * Кэш для запросов квестов
     */
    @Bean("questCache")
    public Cache<String, Object> questCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats()
                .build();
    }

    /**
     * Кэш для сессий игр
     */
    @Bean("gameSessionCache")
    public Cache<String, Object> gameSessionCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats()
                .build();
    }

    /**
     * Кэш для кодов уровня
     */
    @Bean("codeCache")
    public Cache<String, Object> codeCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .maximumSize(5000)
                .recordStats()
                .build();
    }
}