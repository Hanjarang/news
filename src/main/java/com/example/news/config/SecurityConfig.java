package com.example.news.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // 비회원/회원 모두 접근 가능한 엔드포인트
                .requestMatchers(
                    "/api/v1/summaries", // 요약,번역 API 접근 허용
                    "/api/v1/summaries/**",
                    "/api/v1/elasticsearch/**"// Elasticsearch API 접근 허용
                ).permitAll()
                // 회원 전용 엔드포인트
                .requestMatchers(
                    "/api/v1/summaries/{summaryId}",
                    "/api/v1/users/me/summaries"
                ).authenticated()
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
} 