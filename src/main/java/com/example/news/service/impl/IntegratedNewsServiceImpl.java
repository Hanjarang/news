package com.example.news.service.impl;

import com.example.news.entity.NewsDocument;
import com.example.news.service.ElasticsearchService;
import com.example.news.service.IntegratedNewsService;
import com.example.news.service.NewsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegratedNewsServiceImpl implements IntegratedNewsService {

    private final NewsApiService guardianApiService;
    private final ElasticsearchService elasticsearchService;
    private final Random random = new Random();

    @Override
    public Optional<NewsDocument> getAndCacheNewsByKeyword(String keyword) {
        log.info("키워드로 뉴스 가져오기 및 캐싱: {}", keyword);
        
        try {
            // 1. Guardian API에서 뉴스 가져오기
            Optional<NewsDocument> guardianNews = guardianApiService.getRandomNewsByKeyword(keyword);
            
            if (guardianNews.isPresent()) {
                NewsDocument news = guardianNews.get();
                
                // 2. Elasticsearch에 저장
                NewsDocument savedNews = elasticsearchService.saveNews(news);
                log.info("뉴스가 Elasticsearch에 저장되었습니다: {}", savedNews.getTitle());
                
                return Optional.of(savedNews);
            } else {
                log.warn("Guardian API에서 키워드 '{}'에 해당하는 뉴스를 찾을 수 없습니다.", keyword);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("뉴스 가져오기 및 캐싱 중 오류 발생: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void cacheNewsFromGuardian(String keyword) {
        log.info("Guardian API에서 뉴스 캐싱: {}", keyword);
        
        try {
            // Guardian API에서 여러 뉴스 가져오기
            List<NewsDocument> newsList = guardianApiService.searchNewsByKeyword(keyword);
            
            if (!newsList.isEmpty()) {
                // Elasticsearch에 일괄 저장
                elasticsearchService.saveAllNews(newsList);
                log.info("{}개의 뉴스가 Elasticsearch에 캐싱되었습니다.", newsList.size());
            } else {
                log.warn("키워드 '{}'에 해당하는 뉴스를 찾을 수 없습니다.", keyword);
            }
            
        } catch (Exception e) {
            log.error("뉴스 캐싱 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<NewsDocument> searchCachedNews(String keyword) {
        log.info("Elasticsearch에서 캐시된 뉴스 검색: {}", keyword);
        return elasticsearchService.searchNewsByKeyword(keyword);
    }

    @Override
    public List<NewsDocument> searchAllSources(String keyword) {
        log.info("모든 소스에서 뉴스 검색: {}", keyword);
        
        // 1. Elasticsearch에서 검색
        List<NewsDocument> cachedNews = elasticsearchService.searchNewsByKeyword(keyword);
        
        // 2. Guardian API에서 실시간 검색
        List<NewsDocument> guardianNews = guardianApiService.searchNewsByKeyword(keyword);
        
        // 3. 결과 합치기 (중복 제거)
        List<NewsDocument> allNews = new java.util.ArrayList<>(cachedNews);
        
        for (NewsDocument guardian : guardianNews) {
            boolean isDuplicate = cachedNews.stream()
                .anyMatch(cached -> cached.getUrl().equals(guardian.getUrl()));
            
            if (!isDuplicate) {
                allNews.add(guardian);
            }
        }
        
        log.info("총 {}개의 뉴스를 찾았습니다 (캐시: {}, 실시간: {})", 
                allNews.size(), cachedNews.size(), guardianNews.size());
        
        return allNews;
    }

    @Override
    public List<NewsDocument> searchByCategory(String category) {
        log.info("카테고리별 뉴스 검색: {}", category);
        
        // Elasticsearch에서 카테고리별 검색
        List<NewsDocument> allNews = elasticsearchService.findAllNews();
        
        return allNews.stream()
                .filter(news -> news.getCategory() != null && 
                               news.getCategory().toLowerCase().contains(category.toLowerCase()))
                .toList();
    }

    @Override
    public void cacheLatestNews() {
        log.info("최신 뉴스 캐싱 시작");
        
        try {
            // Guardian API에서 최신 뉴스 가져오기
            Optional<NewsDocument> latestNews = guardianApiService.getRandomLatestNews();
            
            if (latestNews.isPresent()) {
                NewsDocument news = latestNews.get();
                
                // Elasticsearch에 저장
                elasticsearchService.saveNews(news);
                log.info("최신 뉴스가 캐싱되었습니다: {}", news.getTitle());
            } else {
                log.warn("최신 뉴스를 가져올 수 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("최신 뉴스 캐싱 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public Optional<NewsDocument> getAndCacheNewsByNaturalLanguage(String naturalLanguage) {
        log.info("자연어로 뉴스 가져오기 및 캐싱: {}", naturalLanguage);
        
        try {
            // 1. Guardian API에서 자연어로 뉴스 가져오기
            Optional<NewsDocument> guardianNews = guardianApiService.getRandomNewsByNaturalLanguage(naturalLanguage);
            
            if (guardianNews.isPresent()) {
                NewsDocument news = guardianNews.get();
                
                // 2. Elasticsearch에 저장
                NewsDocument savedNews = elasticsearchService.saveNews(news);
                log.info("자연어 검색 뉴스가 Elasticsearch에 저장되었습니다: {}", savedNews.getTitle());
                
                return Optional.of(savedNews);
            } else {
                log.warn("Guardian API에서 자연어 '{}'에 해당하는 뉴스를 찾을 수 없습니다.", naturalLanguage);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("자연어 뉴스 가져오기 및 캐싱 중 오류 발생: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void cacheNewsByNaturalLanguage(String naturalLanguage) {
        log.info("자연어로 뉴스 캐싱: {}", naturalLanguage);
        
        try {
            // Guardian API에서 자연어로 여러 뉴스 가져오기
            List<NewsDocument> newsList = guardianApiService.searchNewsByNaturalLanguage(naturalLanguage);
            
            if (!newsList.isEmpty()) {
                // Elasticsearch에 일괄 저장
                elasticsearchService.saveAllNews(newsList);
                log.info("자연어 검색으로 {}개의 뉴스가 Elasticsearch에 캐싱되었습니다.", newsList.size());
            } else {
                log.warn("자연어 '{}'에 해당하는 뉴스를 찾을 수 없습니다.", naturalLanguage);
            }
            
        } catch (Exception e) {
            log.error("자연어 뉴스 캐싱 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<NewsDocument> searchCachedNewsByNaturalLanguage(String naturalLanguage) {
        log.info("Elasticsearch에서 자연어로 캐시된 뉴스 검색: {}", naturalLanguage);
        
        // Elasticsearch에서 모든 뉴스를 가져와서 자연어 필터링
        List<NewsDocument> allNews = elasticsearchService.findAllNews();
        
        return allNews.stream()
                .filter(news -> 
                    (news.getTitle() != null && news.getTitle().toLowerCase().contains(naturalLanguage.toLowerCase())) ||
                    (news.getContent() != null && news.getContent().toLowerCase().contains(naturalLanguage.toLowerCase()))
                )
                .toList();
    }
} 