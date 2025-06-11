package com.example.news.service.impl;

import com.example.news.exception.AIServiceException;
import com.example.news.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

  private final RestTemplate restTemplate;

  @Value("${ai.service.url}")
  private String aiServiceUrl;

  @Override
  public String summarizeText(String originalText) {
    try{
      // TODO : 실제 AI 서비스 연동 구현 필요
      // NOW : 임시 간단 요약 로직
      if (originalText.length() > 100) {
        return originalText.substring(0, 100) + "...";
      }
      return originalText;
    } catch (Exception e) {
      throw new AIServiceException("Failed to summarize text.", e);
    }
  }

  @Override
  public String searchText(String query) {
    try {
      // TODO : 실제 AI 서비스 연동 구현 필요
      // NOW : 임시 검색 결과 반환
      return "검색 결과: " +query;
    } catch (Exception e) {
      throw new AIServiceException("Failed to search text.", e);
    }
  }

}
