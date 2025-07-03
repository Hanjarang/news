package com.example.news.config;

import com.example.news.dto.OAuth2UserInfo;
import com.example.news.entity.User;
import com.example.news.service.OAuth2ProviderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final List<OAuth2ProviderService> oAuth2ProviderServices;
    private final ObjectMapper objectMapper;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String provider = getProviderFromRequest(request);
        
        log.info("OAuth2 로그인 성공: provider={}", provider);
        
        OAuth2ProviderService providerService = findProviderService(provider);
        OAuth2UserInfo userInfo = providerService.convertToUserInfo(oAuth2User);
        User user = providerService.processOAuth2Login(userInfo);
        
        // JSESSIONID를 URL에 포함하여 리다이렉트
        String sessionId = request.getSession().getId();
        String redirectUrl = "/api/v1/auth/login-success?sessionId=" + sessionId;
        
        log.info("로그인 성공 후 리다이렉트: {}", redirectUrl);
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
    
    private String getProviderFromRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.info("요청 URI: {}", requestURI);
        
        // OAuth2 콜백 URL에서 제공자 감지
        if (requestURI.contains("/login/oauth2/code/naver")) {
            return "naver";
        }
        if (requestURI.contains("/login/oauth2/code/google")) {
            return "google";
        }
        if (requestURI.contains("/login/oauth2/code/kakao")) {
            return "kakao";
        }
        
        // OAuth2 인증 요청 URL에서 제공자 감지 (fallback)
        if (requestURI.contains("/oauth2/authorization/naver")) {
            return "naver";
        }
        if (requestURI.contains("/oauth2/authorization/google")) {
            return "google";
        }
        if (requestURI.contains("/oauth2/authorization/kakao")) {
            return "kakao";
        }
        
        log.warn("알 수 없는 OAuth2 제공자: {}", requestURI);
        return "unknown";
    }
    
    private OAuth2ProviderService findProviderService(String provider) {
        return oAuth2ProviderServices.stream()
                .filter(service -> service.getProviderName().equals(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다: " + provider));
    }
} 