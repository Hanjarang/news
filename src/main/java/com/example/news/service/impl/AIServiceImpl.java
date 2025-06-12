package com.example.news.service.impl;

import com.example.news.dto.SummaryRequest;
import com.example.news.dto.SummaryResponse;
import com.example.news.dto.ai.AISearchRequest;
import com.example.news.dto.ai.AISearchResponse;
import com.example.news.dto.ai.AISummaryRequest;
import com.example.news.dto.ai.AISummaryResponse;
import com.example.news.exception.AIServiceException;
import com.example.news.service.AIService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

  private final RestTemplate restTemplate;

  @Value("${ai.service.url}")
  private String aiServiceUrl;

  @Value("${ai.service.api-key}")
  private String apiKey;

  @Override
  public SummaryResponse summarizeText(SummaryRequest request) {
    try{
      //실제 AI 서비스 연동 구현 필요
      AISummaryRequest aiRequest = AISummaryRequest.builder()
          .text(request.getOriginalText())
          .language("ko")
          .maxLength(200)
          .build();

      HttpHeaders headers = createHeaders();
      HttpEntity<AISummaryRequest> entity = new HttpEntity<>(aiRequest, headers);

      AISummaryResponse response = restTemplate.postForObject(
          aiServiceUrl + "/api/v1/summarize",
          entity,
          AISummaryResponse.class
      );

      if (response == null || response.getSummary() == null) {
        throw new AIServiceException("Failed to get summary from AI service");
      }
      return SummaryResponse.builder()
          .originalText(request.getOriginalText())
          .summaryText(response.getSummary())
          .createdAt(LocalDateTime.now())
          .build();
    } catch (Exception e) {
      throw new AIServiceException("Failed to summarize text.", e);
    }
  }

  @Override
  public SummaryResponse searchText(SummaryRequest request){
    try {
      // 실제 AI 서비스 연동 구현 필요
      AISearchRequest aiRequest = AISearchRequest.builder()
          .query(request.getOriginalText())
          .language("ko")
          .maxResults(1)
          .build();

      HttpHeaders headers = createHeaders();
      HttpEntity<AISearchRequest> entity = new HttpEntity<>(aiRequest, headers);

      AISearchResponse response = restTemplate.postForObject(
          aiServiceUrl + "/api/v1/search",
          entity,
          AISearchResponse.class
      );

      if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
        throw new AIServiceException("No search results found");
      }

      return SummaryResponse.builder()
          .originalText(request.getOriginalText())
          .summaryText(response.getResults().get(0).getSummary())
          .createdAt(LocalDateTime.now())
          .build();
    } catch (Exception e) {
      throw new AIServiceException("Failed to search text.", e);
    }
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-API-Key", apiKey);
    return headers;
  }

}
