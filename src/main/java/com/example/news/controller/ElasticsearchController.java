package com.example.news.controller;

import com.example.news.entity.NewsDocument;
import com.example.news.service.ElasticsearchService;
import com.example.news.service.NewsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    @GetMapping("/news/search")
    public ResponseEntity<NewsDocument> searchNewsByKeyword(@RequestParam String keyword) {
        log.info("키워드 검색 요청: {}", keyword);
        
        // The Guardian API에서 실제 뉴스 가져오기
        Optional<NewsDocument> randomNews = guardianApiService.getRandomNewsByKeyword(keyword);
        
        if (randomNews.isPresent()) {
            return ResponseEntity.ok(randomNews.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/news/random")
    public ResponseEntity<NewsDocument> getRandomNewsByKeyword(@RequestParam String keyword) {
        log.info("랜덤 뉴스 요청: {}", keyword);
        Optional<NewsDocument> randomNews = guardianApiService.getRandomNewsByKeyword(keyword);
        
        if (randomNews.isPresent()) {
            return ResponseEntity.ok(randomNews.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/news/latest")
    public ResponseEntity<NewsDocument> getRandomLatestNews() {
        log.info("최신 뉴스 랜덤 요청");
        Optional<NewsDocument> randomNews = guardianApiService.getRandomLatestNews();
        
        if (randomNews.isPresent()) {
            return ResponseEntity.ok(randomNews.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/news/category/{category}")
    public ResponseEntity<NewsDocument> getRandomNewsByCategory(@PathVariable String category) {
        log.info("카테고리 뉴스 랜덤 요청: {}", category);
        Optional<NewsDocument> randomNews = guardianApiService.getRandomNewsByCategory(category);
        
        if (randomNews.isPresent()) {
            return ResponseEntity.ok(randomNews.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/news/natural-language")
    public ResponseEntity<NewsDocument> getRandomNewsByNaturalLanguage(@RequestParam String query) {
        log.info("자연어 뉴스 랜덤 요청: {}", query);
        Optional<NewsDocument> randomNews = guardianApiService.getRandomNewsByNaturalLanguage(query);
        
        if (randomNews.isPresent()) {
            return ResponseEntity.ok(randomNews.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/news/natural-language/search")
    public ResponseEntity<List<NewsDocument>> searchNewsByNaturalLanguage(@RequestParam String query) {
        log.info("자연어 뉴스 검색 요청: {}", query);
        List<NewsDocument> newsList = guardianApiService.searchNewsByNaturalLanguage(query);
        
        return ResponseEntity.ok(newsList);
    }

    @GetMapping("/news")
    public ResponseEntity<List<NewsDocument>> getAllNews() {
        log.info("모든 뉴스 조회 요청");
        List<NewsDocument> newsList = elasticsearchService.findAllNews();
        return ResponseEntity.ok(newsList);
    }

    @GetMapping("/news/{id}")
    public ResponseEntity<NewsDocument> getNewsById(@PathVariable String id) {
        log.info("뉴스 조회 요청: ID = {}", id);
        Optional<NewsDocument> news = elasticsearchService.findNewsById(id);
        
        if (news.isPresent()) {
            return ResponseEntity.ok(news.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable String id) {
        log.info("뉴스 삭제 요청: ID = {}", id);
        elasticsearchService.deleteNews(id);
        return ResponseEntity.ok().build();
    }




} 