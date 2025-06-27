package com.example.news.service;

import com.example.news.entity.NewsDocument;

import java.util.List;
import java.util.Optional;

public interface IntegratedNewsService {
    
    /**
     * Guardian API에서 뉴스를 가져와서 Elasticsearch에 저장하고 반환
     */
    Optional<NewsDocument> getAndCacheNewsByKeyword(String keyword);
    
    /**
     * Guardian API에서 뉴스를 가져와서 Elasticsearch에 저장
     */
    void cacheNewsFromGuardian(String keyword);
    
    /**
     * Elasticsearch에서 저장된 뉴스 검색
     */
    List<NewsDocument> searchCachedNews(String keyword);
    
    /**
     * Guardian API와 Elasticsearch 모두에서 검색
     */
    List<NewsDocument> searchAllSources(String keyword);
    
    /**
     * 카테고리별 저장된 뉴스 검색
     */
    List<NewsDocument> searchByCategory(String category);
    
    /**
     * 최신 뉴스 가져와서 캐싱
     */
    void cacheLatestNews();
    
    /**
     * 자연어로 뉴스를 가져와서 Elasticsearch에 저장하고 반환
     */
    Optional<NewsDocument> getAndCacheNewsByNaturalLanguage(String naturalLanguage);
    
    /**
     * 자연어로 뉴스를 가져와서 Elasticsearch에 캐싱
     */
    void cacheNewsByNaturalLanguage(String naturalLanguage);
    
    /**
     * 자연어로 캐시된 뉴스 검색
     */
    List<NewsDocument> searchCachedNewsByNaturalLanguage(String naturalLanguage);
} 