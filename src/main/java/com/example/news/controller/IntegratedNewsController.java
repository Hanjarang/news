package com.example.news.controller;

import com.example.news.entity.NewsDocument;
import com.example.news.service.IntegratedNewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/integrated-news")
@RequiredArgsConstructor
public class IntegratedNewsController {

    private final IntegratedNewsService integratedNewsService;

    /**
     * Guardian API에서 뉴스를 가져와서 Elasticsearch에 저장하고 반환
     */
    @GetMapping("/search-and-cache")
    public ResponseEntity<NewsDocument> searchAndCacheNews(@RequestParam String keyword) {
        log.info("뉴스 검색 및 캐싱 요청: {}", keyword);
        
        Optional<NewsDocument> news = integratedNewsService.getAndCacheNewsByKeyword(keyword);
        
        if (news.isPresent()) {
            return ResponseEntity.ok(news.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Guardian API에서 뉴스를 가져와서 Elasticsearch에 캐싱
     */
    @PostMapping("/cache")
    public ResponseEntity<String> cacheNewsFromGuardian(@RequestParam String keyword) {
        log.info("Guardian API에서 뉴스 캐싱 요청: {}", keyword);
        
        integratedNewsService.cacheNewsFromGuardian(keyword);
        
        return ResponseEntity.ok("뉴스 캐싱이 완료되었습니다.");
    }

    /**
     * Elasticsearch에서 캐시된 뉴스 검색
     */
    @GetMapping("/cached")
    public ResponseEntity<List<NewsDocument>> searchCachedNews(@RequestParam String keyword) {
        log.info("캐시된 뉴스 검색 요청: {}", keyword);
        
        List<NewsDocument> newsList = integratedNewsService.searchCachedNews(keyword);
        
        return ResponseEntity.ok(newsList);
    }

    /**
     * Guardian API와 Elasticsearch 모두에서 검색
     */
    @GetMapping("/all-sources")
    public ResponseEntity<List<NewsDocument>> searchAllSources(@RequestParam String keyword) {
        log.info("모든 소스에서 뉴스 검색 요청: {}", keyword);
        
        List<NewsDocument> newsList = integratedNewsService.searchAllSources(keyword);
        
        return ResponseEntity.ok(newsList);
    }

    /**
     * 카테고리별 저장된 뉴스 검색
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<NewsDocument>> searchByCategory(@PathVariable String category) {
        log.info("카테고리별 뉴스 검색 요청: {}", category);
        
        List<NewsDocument> newsList = integratedNewsService.searchByCategory(category);
        
        return ResponseEntity.ok(newsList);
    }

    /**
     * 최신 뉴스 캐싱
     */
    @PostMapping("/cache-latest")
    public ResponseEntity<String> cacheLatestNews() {
        log.info("최신 뉴스 캐싱 요청");
        
        integratedNewsService.cacheLatestNews();
        
        return ResponseEntity.ok("최신 뉴스 캐싱이 완료되었습니다.");
    }

    /**
     * 통합 뉴스 서비스 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("통합 뉴스 서비스 헬스 체크");
        
        return ResponseEntity.ok("통합 뉴스 서비스 정상 작동 중");
    }

    /**
     * 자연어로 뉴스를 가져와서 Elasticsearch에 저장하고 반환
     */
    @GetMapping("/natural-language/search-and-cache")
    public ResponseEntity<NewsDocument> searchAndCacheNewsByNaturalLanguage(@RequestParam String query) {
        log.info("자연어 뉴스 검색 및 캐싱 요청: {}", query);
        
        Optional<NewsDocument> news = integratedNewsService.getAndCacheNewsByNaturalLanguage(query);
        
        if (news.isPresent()) {
            return ResponseEntity.ok(news.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 자연어로 뉴스를 가져와서 Elasticsearch에 캐싱
     */
    @PostMapping("/natural-language/cache")
    public ResponseEntity<String> cacheNewsByNaturalLanguage(@RequestParam String query) {
        log.info("자연어로 뉴스 캐싱 요청: {}", query);
        
        integratedNewsService.cacheNewsByNaturalLanguage(query);
        
        return ResponseEntity.ok("자연어 뉴스 캐싱이 완료되었습니다.");
    }

    /**
     * Elasticsearch에서 자연어로 캐시된 뉴스 검색
     */
    @GetMapping("/natural-language/cached")
    public ResponseEntity<List<NewsDocument>> searchCachedNewsByNaturalLanguage(@RequestParam String query) {
        log.info("자연어로 캐시된 뉴스 검색 요청: {}", query);
        
        List<NewsDocument> newsList = integratedNewsService.searchCachedNewsByNaturalLanguage(query);
        
        return ResponseEntity.ok(newsList);
    }
} 