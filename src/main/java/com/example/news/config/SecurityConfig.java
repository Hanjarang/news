package com.example.news.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // 비회원/회원 모두 접근 가능한 엔드포인트
                .requestMatchers(
                    "/api/v1/summaries", // 요약,번역 API 접근 허용
                    "/api/v1/summaries/**",
                    "/api/v1/elasticsearch/**", // Elasticsearch API 접근 허용
                    "/api/v1/integrated-news/**", // 통합 뉴스 API 접근 허용
                    "/api/v1/auth/**", // 인증 관련 엔드포인트
                    "/oauth2/**" // OAuth2 관련 엔드포인트
                ).permitAll()
                // 회원 전용 엔드포인트
                .requestMatchers(
                    "/api/v1/summaries/{summaryId}",
                    "/api/v1/users/me/summaries"
                ).authenticated()
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService())
                )
            )
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(logoutSuccessHandler)
            );
        
        return http.build();
    }
    
    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService();
    }
} 