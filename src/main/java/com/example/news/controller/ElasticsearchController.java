package com.example.news.controller;

import com.example.news.entity.NewsDocument;
import com.example.news.entity.User;
import com.example.news.repository.UserRepository;
import com.example.news.service.ElasticsearchService;
import com.example.news.service.NewsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/elasticsearch")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchController {

    private final ElasticsearchService elasticsearchService;
    private final NewsApiService guardianApiService; // The Guardian API 서비스
    private final UserRepository userRepository;

    // 현재 로그인한 사용자 정보를 가져오는 메서드
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // 비회원
        }

        // OAuth2 사용자인 경우
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            // 각 OAuth2 제공자별로 이메일 추출
            String email = extractEmailFromOAuth2User(attributes);
            
            if (email != null) {
                return userRepository.findByEmail(email).orElse(null);
            }
        }

        return null; // OAuth2 사용자가 아니거나 이메일을 찾을 수 없는 경우
    }
    
    // OAuth2 사용자 정보에서 이메일 추출
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

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Elasticsearch 헬스 체크 요청");
        return ResponseEntity.ok("Elasticsearch 연결 정상");
    }

    @PostMapping("/news")
    public ResponseEntity<NewsDocument> saveNews(@RequestBody NewsDocument newsDocument) {
        log.info("뉴스 저장 요청: {}", newsDocument.getTitle());
        NewsDocument savedNews = elasticsearchService.saveNews(newsDocument);
        return ResponseEntity.ok(savedNews);
    }

    // 키워드 검색 (랜덤)
    @GetMapping("/news/search")
    public ResponseEntity<NewsDocument> searchNewsByKeyword(@RequestParam String keyword) {
        log.info("키워드 검색 요청: {}", keyword);
        
        // The Guardian API에서 실제 뉴스 가져오기
        Optional<NewsDocument> randomNews = guardianApiService.getRandomNewsByKeyword(keyword);
        
        if (randomNews.isPresent()) {
            NewsDocument news = randomNews.get();
            
            // 현재 사용자 정보 가져오기
            User currentUser = getCurrentUser();
            
            // 사용자 ID 설정
            news.setUserId(currentUser != null ? currentUser.getId() : null);
            
            return ResponseEntity.ok(news);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    // 최신 뉴스 (랜덤)
    @GetMapping("/news/latest")
    public ResponseEntity<NewsDocument> getRandomLatestNews() {
        log.info("최신 뉴스 랜덤 요청");
        Optional<NewsDocument> randomNews = guardianApiService.getRandomLatestNews();
        
        if (randomNews.isPresent()) {
            NewsDocument news = randomNews.get();
            
            // 현재 사용자 정보 가져오기
            User currentUser = getCurrentUser();
            
            // 사용자 ID 설정
            news.setUserId(currentUser != null ? currentUser.getId() : null);
            
            return ResponseEntity.ok(news);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 카테고리 검색 (랜덤)
    @GetMapping("/news/category/{category}")
    public ResponseEntity<NewsDocument> getRandomNewsByCategory(@PathVariable String category) {
        log.info("카테고리 뉴스 랜덤 요청: {}", category);
        Optional<NewsDocument> randomNews = guardianApiService.getRandomNewsByCategory(category);
        
        if (randomNews.isPresent()) {
            NewsDocument news = randomNews.get();
            
            // 현재 사용자 정보 가져오기
            User currentUser = getCurrentUser();
            
            // 사용자 ID 설정
            news.setUserId(currentUser != null ? currentUser.getId() : null);
            
            return ResponseEntity.ok(news);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 자연어 검색 (랜덤)
    @GetMapping("/news/natural-language")
    public ResponseEntity<NewsDocument> getRandomNewsByNaturalLanguage(@RequestParam String query) {
        log.info("자연어 뉴스 랜덤 요청: {}", query);
        Optional<NewsDocument> randomNews = guardianApiService.getRandomNewsByNaturalLanguage(query);
        
        if (randomNews.isPresent()) {
            NewsDocument news = randomNews.get();
            
            // 현재 사용자 정보 가져오기
            User currentUser = getCurrentUser();
            
            // 사용자 ID 설정
            news.setUserId(currentUser != null ? currentUser.getId() : null);
            
            return ResponseEntity.ok(news);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 자연어 검색 (모든 결과 리스트 반환)
    @GetMapping("/news/natural-language/search")
    public ResponseEntity<List<NewsDocument>> searchNewsByNaturalLanguage(@RequestParam String query) {
        log.info("자연어 뉴스 검색 요청: {}", query);
        List<NewsDocument> newsList = guardianApiService.searchNewsByNaturalLanguage(query);
        
        // 현재 사용자 정보 가져오기
        User currentUser = getCurrentUser();
        
        // 각 뉴스에 사용자 ID 설정
        for (NewsDocument news : newsList) {
            news.setUserId(currentUser != null ? currentUser.getId() : null);
        }
        
        return ResponseEntity.ok(newsList);
    }




} 