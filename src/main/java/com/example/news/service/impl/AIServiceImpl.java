package com.example.news.service.impl;

import com.example.news.dto.SummaryRequest;
import com.example.news.dto.SummaryResponse;
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

  @Value("${ai.service.api-key}")
  private String apiToken;


  @Override
  public SummaryResponse summarizeText(SummaryRequest request) {
    try {
      // ✅ 토큰 상태 확인
      log.info("API Token length: {}", apiToken != null ? apiToken.length() : 0);
      log.info("API Token starts with: {}", apiToken != null ? apiToken.substring(0, Math.min(10, apiToken.length())) : "null");

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
      headers.set("X-Wait-For-Model", "true"); // 모델 로딩 대기

      HttpEntity<String> entity = new HttpEntity<>(json, headers);

      // ✅ 요약 요청 - 더 빠른 모델 사용
      log.info("Calling HuggingFace API with token: {}", apiToken != null ? apiToken.substring(0, 10) + "..." : "null");

      // 더 빠른 요약 모델들 (순서대로 시도)
      String[] modelUrls = {
        "https://api-inference.huggingface.co/models/facebook/bart-large-cnn"// 원본 (백업)
      };

      ResponseEntity<String> response = null;
      Exception lastException = null;

      for (String modelUrl : modelUrls) {
        try {
          log.info("Trying model: {}", modelUrl);
          response = restTemplate.postForEntity(modelUrl, entity, String.class);
          
          if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Success with model: {}", modelUrl);
            break;
          }
        } catch (Exception e) {
          log.warn("Failed with model {}: {}", modelUrl, e.getMessage());
          lastException = e;
          continue;
        }
      }

      if (response == null || !response.getStatusCode().is2xxSuccessful()) {
        throw new AIServiceException("모든 모델 시도 실패", lastException);
      }

      log.info("Response status: {}", response.getStatusCode());
      log.info("Response body: {}", response.getBody());

      // 응답 구조 분석을 위한 로그 추가
      JsonNode root = objectMapper.readTree(response.getBody());
      log.info("Response is array: {}", root.isArray());
      if (root.isArray()) {
        log.info("Array size: {}", root.size());
        if (root.size() > 0) {
          log.info("First element keys: {}", root.get(0).fieldNames());
          log.info("First element content: {}", root.get(0).toString());
        }
      }

      if (root.isArray() && root.size() > 0 && root.get(0).has("summary_text")) {
        String englishSummary = root.get(0).get("summary_text").asText();

        // ✅ 영어 요약을 한글로 번역
        String koreanSummary = translateToKorean(englishSummary);

        log.info("English summary: {}", englishSummary);
        log.info("Korean summary: {}", koreanSummary);

        return SummaryResponse.builder()
            .originalText(englishSummary)  // 요약된 영어 텍스트
            .summaryText(koreanSummary)    // 번역된 한글 텍스트
            .createdAt(LocalDateTime.now())
            .build();
      } else {
        throw new AIServiceException("AI 응답에 summary_text가 없습니다.");
      }

    } catch (Exception e) {
      log.error("요약 요청 중 오류 발생: {}", e.getMessage(), e);
      throw new AIServiceException("AI 요약 요청 중 오류 발생", e);
    }
  }

  /**
   * MyMemory Translation API를 사용하여 영어를 한글로 번역
   */
  private String translateToKorean(String englishText) {
    try {
      // MyMemory API는 GET 요청 사용
      String encodedText = java.net.URLEncoder.encode(englishText, java.nio.charset.StandardCharsets.UTF_8);
      String url = String.format("https://api.mymemory.translated.net/get?q=%s&langpair=en|ko", encodedText);

      log.info("MyMemory Translation URL: {}", url);

      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

      log.info("MyMemory response status: {}", response.getStatusCode());
      log.info("MyMemory response body: {}", response.getBody());

      JsonNode root = objectMapper.readTree(response.getBody());
      if (root.has("responseData") && root.get("responseData").has("translatedText")) {
        return root.get("responseData").get("translatedText").asText();
      } else {
        log.warn("MyMemory 번역 응답에 translatedText가 없습니다.");
        return englishText; // 번역 실패 시 원본 영어 텍스트 반환
      }

    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      log.error("MyMemory 번역 중 JSON 파싱 오류 발생: {}", e.getMessage());
      return englishText;
    } catch (Exception e) {
      log.error("MyMemory 번역 중 기타 오류 발생: {}", e.getMessage());
      return englishText;
    }
  }

}

