package com.example.news.service.impl;

import com.example.news.dto.EnglishQAResponse;
import com.example.news.service.MCPService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MCPServiceImpl implements MCPService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    @Value("${ollama.default-model:mistral:7b}")
    private String defaultModel;
    
    private String currentModel = "mistral:7b";
    private final String[] availableModels = {"mistral:7b", "llama3.1:8b"};
    
    @Override
    public EnglishQAResponse generateAnswer(String question, String newsContext) {
        log.info("영어 질문에 대한 답변 생성 시작: {}", question);
        
        try {
            // 프롬프트 생성
            String prompt = createPrompt(question, newsContext);
            
            // Ollama API 호출
            String response = callOllamaAPI(prompt);
            
            // 응답 파싱 및 구조화
            EnglishQAResponse qaResponse = parseResponse(question, response, newsContext);
            
            log.info("답변 생성 완료: {} 문장", qaResponse.getSentences().size());
            return qaResponse;
            
        } catch (Exception e) {
            log.error("답변 생성 중 오류 발생: {}", e.getMessage(), e);
            return createFallbackResponse(question);
        }
    }
    
    private String createPrompt(String question, String newsContext) {
        return String.format("""
            Question: %s
            
            News Context: %s
            
            Please provide exactly 5 sentences answering the question above. Format your response as:
            1. [First sentence - direct answer]
            2. [Second sentence - additional context]
            3. [Third sentence - background information]
            4. [Fourth sentence - current implications]
            5. [Fifth sentence - future considerations]
            
            Answer in clear, educational English suitable for English learners.
            """, question, newsContext);
    }
    
    private String callOllamaAPI(String prompt) {
        String url = ollamaBaseUrl + "/api/generate";
        
        // 프롬프트에서 개행 문자를 공백으로 변환하고 JSON 이스케이프 처리
        String escapedPrompt = prompt
                .replace("\n", " ")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .trim();
        
        // Ollama API 요청 본문
        String requestBody = String.format("""
            {
                "model": "%s",
                "prompt": "%s",
                "stream": false,
                "options": {
                    "temperature": 0.7,
                    "top_p": 0.9,
                    "max_tokens": 500
                }
            }
            """, currentModel, escapedPrompt);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        log.info("Ollama API 호출: {}", url);
        log.info("사용 모델: {}", currentModel);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("response").asText();
            } catch (Exception e) {
                log.error("Ollama 응답 파싱 오류: {}", e.getMessage());
                return response.getBody();
            }
        } else {
            throw new RuntimeException("Ollama API 호출 실패: " + response.getStatusCode());
        }
    }
    
    private EnglishQAResponse parseResponse(String question, String response, String newsContext) {
        List<String> sentences = new ArrayList<>();
        
        // 응답을 문장으로 분리
        String[] lines = response.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.matches("^\\d+\\..*")) {
                // 번호 제거하고 문장만 추출
                String sentence = line.replaceFirst("^\\d+\\.\\s*", "");
                if (!sentence.isEmpty()) {
                    sentences.add(sentence);
                }
            }
        }
        
        // 5문장이 되도록 조정
        while (sentences.size() < 5) {
            sentences.add("Additional information is being processed.");
        }
        
        if (sentences.size() > 5) {
            sentences = sentences.subList(0, 5);
        }
        
        return EnglishQAResponse.builder()
                .question(question)
                .answer(String.join(" ", sentences))
                .sentences(sentences)
                .modelUsed(currentModel)
                .newsContext(newsContext)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    private EnglishQAResponse createFallbackResponse(String question) {
        List<String> fallbackSentences = Arrays.asList(
            "I apologize, but I'm unable to provide a detailed answer at the moment.",
            "Please try rephrasing your question or check your internet connection.",
            "The system is currently experiencing technical difficulties.",
            "You may want to try again in a few moments.",
            "For immediate assistance, please contact support."
        );
        
        return EnglishQAResponse.builder()
                .question(question)
                .answer(String.join(" ", fallbackSentences))
                .sentences(fallbackSentences)
                .modelUsed("fallback")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public String[] getAvailableModels() {
        return availableModels;
    }
    
    @Override
    public void selectModel(String modelName) {
        if (Arrays.asList(availableModels).contains(modelName)) {
            this.currentModel = modelName;
            log.info("모델 변경: {}", modelName);
        } else {
            log.warn("지원하지 않는 모델: {}", modelName);
        }
    }
    
    @Override
    public boolean isConnected() {
        try {
            String url = ollamaBaseUrl + "/api/tags";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Ollama 연결 확인 실패: {}", e.getMessage());
            return false;
        }
    }
} 