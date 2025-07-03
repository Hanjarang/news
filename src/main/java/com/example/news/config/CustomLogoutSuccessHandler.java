package com.example.news.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    
    @Override
    public void onLogoutSuccess(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Authentication authentication) throws IOException, ServletException {
        
        log.info("로그아웃 성공 핸들러 호출");
        
        // JSON 응답으로 로그아웃 성공 메시지 반환
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"로그아웃이 성공적으로 완료되었습니다.\",\"status\":\"success\"}");
    }
} 