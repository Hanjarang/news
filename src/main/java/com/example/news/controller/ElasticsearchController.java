package com.example.news.controller;

import com.example.news.entity.NewsDocument;
import com.example.news.service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/elasticsearch")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchController {

    private final ElasticsearchService elasticsearchService;

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
    public ResponseEntity<List<NewsDocument>> searchNewsByKeyword(@RequestParam String keyword) {
        log.info("키워드 검색 요청: {}", keyword);
        List<NewsDocument> newsList = elasticsearchService.searchNewsByKeyword(keyword);
        return ResponseEntity.ok(newsList);
    }

    @GetMapping("/news/random")
    public ResponseEntity<NewsDocument> getRandomNewsByKeyword(@RequestParam String keyword) {
        log.info("랜덤 뉴스 요청: {}", keyword);
        Optional<NewsDocument> randomNews = elasticsearchService.findRandomNewsByKeyword(keyword);
        
        if (randomNews.isPresent()) {
            return ResponseEntity.ok(randomNews.get());
        } else {
            return ResponseEntity.notFound().build();
        }
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

    @PostMapping("/test-data")
    public ResponseEntity<String> createTestData() {
        log.info("테스트 데이터 생성 요청");
        
        // 샘플 뉴스 데이터 생성
        NewsDocument testNews1 = NewsDocument.builder()
                .title("Breaking News: Technology Advances in AI")
                .content("Artificial Intelligence has made significant progress in recent years. Researchers are developing new algorithms that can process natural language more effectively.")
                .source("Tech News")
                .url("https://example.com/tech-news-1")
                .publishedAt(LocalDateTime.now())
                .category("Technology")
                .author("John Doe")
                .language("en")
                .build();

        NewsDocument testNews2 = NewsDocument.builder()
                .title("Climate Change: Global Impact on Environment")
                .content("Climate change continues to affect our planet. Scientists warn about rising temperatures and their effects on ecosystems worldwide.")
                .source("Environmental News")
                .url("https://example.com/env-news-1")
                .publishedAt(LocalDateTime.now())
                .category("Environment")
                .author("Jane Smith")
                .language("en")
                .build();

        NewsDocument testNews3 = NewsDocument.builder()
                .title("Economic Growth: Market Trends Analysis")
                .content("The global economy shows signs of recovery. Market analysts predict positive trends for the upcoming quarter.")
                .source("Business News")
                .url("https://example.com/business-news-1")
                .publishedAt(LocalDateTime.now())
                .category("Business")
                .author("Mike Johnson")
                .language("en")
                .build();

        elasticsearchService.saveAllNews(List.of(testNews1, testNews2, testNews3));
        
        return ResponseEntity.ok("테스트 데이터가 성공적으로 생성되었습니다.");
    }
} 