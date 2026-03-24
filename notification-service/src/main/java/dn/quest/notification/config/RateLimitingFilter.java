package dn.quest.notification.config;

import dn.quest.notification.exception.ValidationException;
import dn.quest.notification.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для Rate Limiting HTTP запросов
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Autowired
    private RateLimitingService rateLimitingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        try {
            // Проверяем глобальные лимиты
            if (!rateLimitingService.canSendGlobally()) {
                logger.warn("Global rate limit exceeded for IP: {}", clientIp);
                response.setStatus(429);
                response.getWriter().write("{\"error\":\"Global rate limit exceeded\"}");
                return;
            }

            // Проверяем лимиты для IP
            if (!rateLimitingService.canSendFromIp(clientIp, requestUri)) {
                logger.warn("IP rate limit exceeded: {} for {}", clientIp, requestUri);
                response.setStatus(429);
                response.getWriter().write("{\"error\":\"IP rate limit exceeded\"}");
                return;
            }

            // Регистрируем запрос
            rateLimitingService.recordIpRequest(clientIp, requestUri);

            // Продолжаем цепочку фильтров
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Error in rate limiting filter", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    /**
     * Получить IP адрес клиента
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Не применять rate limiting к health checks и статическим ресурсам
        return path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/favicon.ico");
    }
}