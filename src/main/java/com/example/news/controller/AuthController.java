package com.example.news.controller;

import com.example.news.dto.UserInfo;
import com.example.news.entity.User;
import com.example.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserRepository userRepository;
    
    @GetMapping("/login-success")
    public ResponseEntity<Map<String, Object>> loginSuccess(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        log.info("로그인 성공 엔드포인트 호출, sessionId: {}", sessionId);
        
        // sessionId가 제공된 경우 쿠키로 설정
        if (sessionId != null && !sessionId.isEmpty()) {
            Cookie sessionCookie = new Cookie("JSESSIONID", sessionId);
            sessionCookie.setPath("/");
            sessionCookie.setHttpOnly(true);
            sessionCookie.setSecure(false); // 개발환경에서는 false, 프로덕션에서는 true
            response.addCookie(sessionCookie);
            log.info("JSESSIONID 쿠키 설정: {}", sessionId);
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "로그인이 성공했습니다.");
        responseData.put("user", oAuth2User.getAttributes());
        responseData.put("sessionId", sessionId);
        
        return ResponseEntity.ok(responseData);
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        log.info("인증 테스트 엔드포인트 호출");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "인증 테스트 성공");
        response.put("status", "OK");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test-with-session")
    public ResponseEntity<Map<String, Object>> testWithSession(
            @RequestParam String sessionId,
            HttpServletRequest request) {
        
        log.info("세션 ID로 인증 테스트: {}", sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "세션 ID 테스트");
        response.put("sessionId", sessionId);
        response.put("requestSessionId", request.getSession().getId());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getAvailableProviders() {
        log.info("사용 가능한 OAuth2 제공자 조회");
        
        Map<String, Object> response = new HashMap<>();
        response.put("providers", Map.of(
            "naver", "/oauth2/authorization/naver",
            "google", "/oauth2/authorization/google",
            "kakao", "/oauth2/authorization/kakao"
        ));
        response.put("message", "사용 가능한 OAuth2 제공자 목록");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증되지 않은 사용자입니다."));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", oAuth2User.getAttributes());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user-info")
    public ResponseEntity<UserInfo> getCurrentUserInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseEntity.status(401).build();
        }
        
        // OAuth2 사용자 정보에서 이메일 추출
        String email = extractEmailFromOAuth2User(oAuth2User.getAttributes());
        
        if (email == null) {
            return ResponseEntity.status(400).build();
        }
        
        // 데이터베이스에서 사용자 정보 조회
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(404).build();
        }
        
        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
        
        return ResponseEntity.ok(userInfo);
    }
    
    private String extractEmailFromOAuth2User(Map<String, Object> attributes) {
        // 네이버
        if (attributes.containsKey("response")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response.containsKey("email")) {
                return (String) response.get("email");
            }
        }
        
        // 구글
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }
        
        // 카카오
        if (attributes.containsKey("kakao_account")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount.containsKey("email")) {
                return (String) kakaoAccount.get("email");
            }
        }
        
        return null;
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("로그아웃 요청");
        
        // Spring Security 컨텍스트 클리어
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        
        // 세션 무효화
        request.getSession().invalidate();
        
        // JSESSIONID 쿠키 삭제
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        response.addCookie(sessionCookie);
        
        Map<String, String> responseData = new HashMap<>();
        responseData.put("message", "로그아웃되었습니다.");
        responseData.put("status", "success");
        
        log.info("로그아웃 완료: Security 컨텍스트 클리어, 세션 무효화 및 쿠키 삭제");
        
        return ResponseEntity.ok(responseData);
    }
} 