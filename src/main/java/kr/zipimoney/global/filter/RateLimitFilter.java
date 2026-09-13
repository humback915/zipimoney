package kr.zipimoney.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.zipimoney.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int AUTH_LIMIT = 10;       // 인증 API: 분당 10회
    private static final int PUBLIC_API_LIMIT = 60; // 공개 API: 분당 60회
    private static final int GENERAL_LIMIT = 120;   // 일반 API: 분당 120회

    private final Cache<String, AtomicInteger> authBucket = buildCache();
    private final Cache<String, AtomicInteger> publicBucket = buildCache();
    private final Cache<String, AtomicInteger> generalBucket = buildCache();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Cache<String, AtomicInteger> buildCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(10_000)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 정적 리소스, 헬스체크는 제외
        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        int limit;
        Cache<String, AtomicInteger> bucket;

        if (path.startsWith("/api/auth/")) {
            limit = AUTH_LIMIT;
            bucket = authBucket;
        } else if (isPublicApi(path)) {
            limit = PUBLIC_API_LIMIT;
            bucket = publicBucket;
        } else {
            limit = GENERAL_LIMIT;
            bucket = generalBucket;
        }

        AtomicInteger counter = bucket.get(clientIp, k -> new AtomicInteger(0));
        int current = counter.incrementAndGet();

        if (current > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ApiResponse<?> apiResponse = ApiResponse.builder()
                    .error(new ApiResponse.Error("TOO_MANY_REQUESTS", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."))
                    .build();
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcluded(String path) {
        return path.equals("/") || path.equals("/index.html")
                || path.startsWith("/assets/") || path.equals("/favicon.ico")
                || path.equals("/api/health")
                || path.equals("/privacy") || path.equals("/terms");
    }

    private boolean isPublicApi(String path) {
        return path.equals("/api/region") || path.equals("/api/geocode")
                || path.equals("/api/search") || path.equals("/api/deals")
                || path.equals("/api/complex");
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
