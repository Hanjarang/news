package com.example.news.service;

import com.example.news.dto.EnglishQAResponse;

public interface MCPService {
    
    /**
     * Ollama 모델을 사용하여 영어 질문에 대한 답변 생성
     */
    EnglishQAResponse generateAnswer(String question, String newsContext);
    
    /**
     * 사용 가능한 모델 목록 조회
     */
    String[] getAvailableModels();
    
    /**
     * 특정 모델 선택
     */
    void selectModel(String modelName);
    
    /**
     * 연결 상태 확인
     */
    boolean isConnected();
} 