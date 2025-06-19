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

      HttpEntity<String> entity = new HttpEntity<>(json, headers);

      // ✅ 요약 요청
      log.info("Calling HuggingFace API with token: {}", apiToken != null ? apiToken.substring(0, 10) + "..." : "null");

      ResponseEntity<String> response = restTemplate.postForEntity(
          "https://api-inference.huggingface.co/models/facebook/bart-large-cnn",
          entity,
          String.class
      );

      log.info("Response status: {}", response.getStatusCode());
      log.info("Response body: {}", response.getBody());

      JsonNode root = objectMapper.readTree(response.getBody());

      if (root.isArray() && root.size() > 0 && root.get(0).has("summary_text")) {
        String englishSummary = root.get(0).get("summary_text").asText();

        // ✅ 영어 요약을 한글로 번역
        String koreanSummary = translateToKorean(englishSummary);

        log.info("English summary: {}", englishSummary);
        log.info("Korean summary: {}", koreanSummary);

        return SummaryResponse.builder()
            .originalText(request.getOriginalText())
            .summaryText(koreanSummary)
            .createdAt(LocalDateTime.now())
            .build();
      } else {
        throw new AIServiceException("AI 응답에 summary_text가 없습니다.");
      }

    } catch (Exception e) {
      throw new AIServiceException("AI 요약 요청 중 오류 발생", e);
    }
  }

  /**
   * LibreTranslate API를 사용하여 영어를 한글로 번역
   */
  private String translateToKorean(String englishText) {
    try {
      // LibreTranslate API 요청 본문
      Map<String, String> translateRequest = new HashMap<>();
      translateRequest.put("q", englishText);
      translateRequest.put("source", "en");
      translateRequest.put("target", "ko");

      String translateJson = objectMapper.writeValueAsString(translateRequest);
      log.info("Translation request: {}", translateJson);

      // 번역 API 헤더
      HttpHeaders translateHeaders = new HttpHeaders();
      translateHeaders.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<String> translateEntity = new HttpEntity<>(translateJson, translateHeaders);

      // LibreTranslate API 호출
      ResponseEntity<String> translateResponse = restTemplate.postForEntity(
          "https://libretranslate.de/translate",
          translateEntity,
          String.class
      );

      log.info("Translation response status: {}", translateResponse.getStatusCode());
      log.info("Translation response body: {}", translateResponse.getBody());

      // 번역 결과 파싱
      JsonNode translateRoot = objectMapper.readTree(translateResponse.getBody());
      if (translateRoot.has("translatedText")) {
        return translateRoot.get("translatedText").asText();
      } else {
        log.warn("번역 응답에 translatedText가 없습니다. 원본 영어 텍스트를 반환합니다.");
        return englishText;
      }

    } catch (Exception e) {
      log.error("LibreTranslate 번역 중 오류 발생: {}", e.getMessage());
      
      // LibreTranslate 실패 시 MyMemory Translation API 시도
      try {
        log.info("MyMemory Translation API로 재시도...");
        return translateWithMyMemory(englishText);
      } catch (Exception e2) {
        log.error("MyMemory 번역도 실패: {}", e2.getMessage());
        // 번역 실패 시 원본 영어 텍스트 반환
        return englishText;
      }
    }
  }

  /**
   * MyMemory Translation API를 사용하여 영어를 한글로 번역 (백업용)
   */
  private String translateWithMyMemory(String englishText) {
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
        return englishText;
      }
      
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      log.error("MyMemory 번역 중 JSON 파싱 오류 발생: {}", e.getMessage());
      return englishText;
    } catch (Exception e) {
      log.error("MyMemory 번역 중 기타 오류 발생: {}", e.getMessage());
      return englishText;
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

