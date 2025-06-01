package com.example.gagso.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 API
                .allowedOrigins("http://localhost:3000") // React 개발 서버 주소
                .allowedMethods("*"); // 모든 HTTP 메소드 허용
    }
}
