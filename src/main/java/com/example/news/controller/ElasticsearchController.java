package com.example.news.controller;

import com.example.news.entity.NewsDocument;
import com.example.news.service.ElasticsearchService;
import com.example.news.service.NewsApiService;
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

    @PostMapping("/test-data-diverse")
    public ResponseEntity<String> createDiverseTestData() {
        log.info("다양한 테스트 데이터 생성 요청");
        
        List<NewsDocument> diverseNews = List.of(
            NewsDocument.builder()
                .title("Space Exploration: Mars Mission Success")
                .content("NASA's latest Mars rover has successfully landed and begun transmitting data. Scientists are excited about the potential discoveries about the red planet's geology and atmosphere.")
                .source("Space News")
                .url("https://example.com/space-news-1")
                .publishedAt(LocalDateTime.now())
                .category("Science")
                .author("Dr. Sarah Wilson")
                .language("en")
                .build(),
                
            NewsDocument.builder()
                .title("Healthcare Breakthrough: New Cancer Treatment")
                .content("Medical researchers have developed a revolutionary immunotherapy treatment that shows promising results in early clinical trials. This breakthrough could change how we approach cancer treatment.")
                .source("Medical News")
                .url("https://example.com/medical-news-1")
                .publishedAt(LocalDateTime.now())
                .category("Health")
                .author("Dr. Michael Chen")
                .language("en")
                .build(),
                
            NewsDocument.builder()
                .title("Sports: World Cup Final Results")
                .content("In an exciting match that went to penalties, the underdog team emerged victorious. Fans around the world celebrated this unexpected outcome in the most watched sporting event.")
                .source("Sports News")
                .url("https://example.com/sports-news-1")
                .publishedAt(LocalDateTime.now())
                .category("Sports")
                .author("Alex Rodriguez")
                .language("en")
                .build(),
                
            NewsDocument.builder()
                .title("Entertainment: New Blockbuster Movie Release")
                .content("The highly anticipated sci-fi movie has broken box office records in its opening weekend. Critics praise the innovative special effects and compelling storyline.")
                .source("Entertainment News")
                .url("https://example.com/entertainment-news-1")
                .publishedAt(LocalDateTime.now())
                .category("Entertainment")
                .author("Lisa Thompson")
                .language("en")
                .build(),
                
            NewsDocument.builder()
                .title("Education: Online Learning Revolution")
                .content("Universities worldwide are adopting hybrid learning models. Students and educators are discovering new ways to engage in virtual classrooms.")
                .source("Education News")
                .url("https://example.com/education-news-1")
                .publishedAt(LocalDateTime.now())
                .category("Education")
                .author("Prof. David Brown")
                .language("en")
                .build(),
                
            NewsDocument.builder()
                .title("Technology: Quantum Computing Milestone")
                .content("Scientists have achieved quantum supremacy in a new experiment. This breakthrough could revolutionize cryptography and computational power.")
                .source("Tech News")
                .url("https://example.com/tech-news-2")
                .publishedAt(LocalDateTime.now())
                .category("Technology")
                .author("Dr. Emily Zhang")
                .language("en")
                .build(),
                
            NewsDocument.builder()
                .title("Environment: Renewable Energy Adoption")
                .content("Countries are rapidly transitioning to renewable energy sources. Solar and wind power installations have reached record levels globally.")
                .source("Environmental News")
                .url("https://example.com/env-news-2")
                .publishedAt(LocalDateTime.now())
                .category("Environment")
                .author("Maria Garcia")
                .language("en")
                .build(),
                
            NewsDocument.builder()
                .title("Business: Startup Success Story")
                .content("A small startup has become a unicorn company in just three years. Their innovative approach to solving everyday problems has attracted major investors.")
                .source("Business News")
                .url("https://example.com/business-news-2")
                .publishedAt(LocalDateTime.now())
                .category("Business")
                .author("Robert Kim")
                .language("en")
                .build()
        );

        elasticsearchService.saveAllNews(diverseNews);
        
        return ResponseEntity.ok("다양한 테스트 데이터가 성공적으로 생성되었습니다. 총 " + diverseNews.size() + "개의 뉴스가 추가되었습니다.");
    }
} 