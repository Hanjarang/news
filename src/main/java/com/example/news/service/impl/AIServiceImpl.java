package com.example.news.service.impl;

import com.example.news.dto.SummaryRequest;
import com.example.news.dto.SummaryResponse;
import com.example.news.dto.ai.AISearchRequest;
import com.example.news.dto.ai.AISearchResponse;
import com.example.news.dto.ai.AISummaryRequest;
import com.example.news.dto.ai.AISummaryResponse;
import com.example.news.exception.AIServiceException;
import com.example.news.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final Logger log = LoggerFactory.getLogger(AIServiceImpl.class);

  @Value("${ai.service.url}")
  private String aiServiceUrl;

  @Value("${ai.service.api-key}")
  private String apiKey;

  @Value("${huggingface.api.token}")
  private String apiToken;



//  @Override
//  public SummaryResponse summarizeText(SummaryRequest request) {
//    try{
//      //실제 AI 서비스 연동 구현 필요
//      AISummaryRequest aiRequest = AISummaryRequest.builder()
//          .text(request.getOriginalText())
//          .language("ko")
//          .maxLength(200)
//          .build();
//
//      HttpHeaders headers = createHeaders();
//      HttpEntity<AISummaryRequest> entity = new HttpEntity<>(aiRequest, headers);
//
//      AISummaryResponse response = restTemplate.postForObject(
//          aiServiceUrl + "/api/v1/summarize",
//          entity,
//          AISummaryResponse.class
//      );
//
//      if (response == null || response.getSummary() == null) {
//        throw new AIServiceException("Failed to get summary from AI service");
//      }
//      return SummaryResponse.builder()
//          .originalText(request.getOriginalText())
//          .summaryText(response.getSummary())
//          .createdAt(LocalDateTime.now())
//          .build();
//    } catch (Exception e) {
//      throw new AIServiceException("Failed to summarize text.", e);
//    }
//  }


  @Override
  public SummaryResponse summarizeText(SummaryRequest request) {
    try {
      String originalText = request.getOriginalText().trim();

      // ✅ 길이 제한 (1000자 이하)
      if (originalText.length() > 1000) {
        originalText = originalText.substring(0, 1000);
      }

      // ✅ 본문 구성
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("inputs", originalText);

      String json = objectMapper.writeValueAsString(requestBody);
      log.info("Request body: {}", json);

      // ✅ 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(apiToken);
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

      HttpEntity<String> entity = new HttpEntity<>(json, headers);

      // ✅ 요청
      ResponseEntity<String> response = restTemplate.postForEntity(
          "https://api-inference.huggingface.co/models/facebook/bart-large-cnn",
          entity,
          String.class
      );

      log.info("Response status: {}", response.getStatusCode());
      log.info("Response body: {}", response.getBody());

      JsonNode root = objectMapper.readTree(response.getBody());

      if (root.isArray() && root.size() > 0 && root.get(0).has("summary_text")) {
        String summary = root.get(0).get("summary_text").asText();

        return SummaryResponse.builder()
            .originalText(request.getOriginalText())
            .summaryText(summary)
            .createdAt(LocalDateTime.now())
            .build();
      } else {
        throw new AIServiceException("AI 응답에 summary_text가 없습니다.");
      }

    } catch (Exception e) {
      throw new AIServiceException("AI 요약 요청 중 오류 발생", e);
    }
  }



  @Override
  public SummaryResponse searchText(SummaryRequest request) {
    throw new UnsupportedOperationException("이 기능은 아직 구현되지 않았습니다.");
  }
}


//  @Override
//  public SummaryResponse searchText(SummaryRequest request){
//    try {
//      // 실제 AI 서비스 연동 구현 필요
//      AISearchRequest aiRequest = AISearchRequest.builder()
//          .query(request.getOriginalText())
//          .language("ko")
//          .maxResults(1)
//          .build();
//
//      HttpHeaders headers = createHeaders();
//      HttpEntity<AISearchRequest> entity = new HttpEntity<>(aiRequest, headers);
//
//      AISearchResponse response = restTemplate.postForObject(
//          aiServiceUrl + "/api/v1/search",
//          entity,
//          AISearchResponse.class
//      );
//
//      if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
//        throw new AIServiceException("No search results found");
//      }
//
//      return SummaryResponse.builder()
//          .originalText(request.getOriginalText())
//          .summaryText(response.getResults().get(0).getSummary())
//          .createdAt(LocalDateTime.now())
//          .build();
//    } catch (Exception e) {
//      throw new AIServiceException("Failed to search text.", e);
//    }
//  }

