package kr.zipimoney.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaWebConfig implements WebMvcConfigurer {
    // 정적 파일 서빙 비활성화 — 프론트엔드는 Vite(3000) 또는 별도 CDN에서 서빙
}
