package com.example.news.service;

import com.example.news.entity.NewsDocument;

import java.util.List;
import java.util.Optional;

public interface NewsApiService {
    
    /**
     * 키워드로 뉴스를 검색하여 랜덤으로 하나 반환
     */
    Optional<NewsDocument> getRandomNewsByKeyword(String keyword);
    
    /**
     * 키워드로 뉴스를 검색하여 리스트 반환
     */
    List<NewsDocument> searchNewsByKeyword(String keyword);
    
    /**
     * 최신 뉴스를 가져와서 랜덤으로 하나 반환
     */
    Optional<NewsDocument> getRandomLatestNews();
    
    /**
     * 특정 카테고리의 뉴스를 가져와서 랜덤으로 하나 반환
     */
    Optional<NewsDocument> getRandomNewsByCategory(String category);
    
    /**
     * 자연어로 뉴스를 검색하여 랜덤으로 하나 반환 (분야 구분 없음)
     */
    Optional<NewsDocument> getRandomNewsByNaturalLanguage(String naturalLanguage);
    
    /**
     * 자연어로 뉴스를 검색하여 리스트 반환 (분야 구분 없음)
     */
    List<NewsDocument> searchNewsByNaturalLanguage(String naturalLanguage);
} 