package com.example.news.service;

import com.example.news.dto.EnglishQAResponse;

public interface EnglishQAService {
    
    /**
     * 영어 질문에 대한 답변 생성
     */
    EnglishQAResponse generateAnswer(String question);
    
    /**
     * 질문을 키워드로 변환
     */
    String extractKeywords(String question);
    
    /**
     * 관련 뉴스 컨텍스트 수집
     */
    String collectNewsContext(String keywords);
} 