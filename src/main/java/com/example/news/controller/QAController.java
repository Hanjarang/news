package com.example.news.controller;

import com.example.news.dto.EnglishQAResponse;
import com.example.news.service.EnglishQAService;
import com.example.news.service.MCPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/qa")
@RequiredArgsConstructor
public class QAController {

    private final EnglishQAService englishQAService;
    private final MCPService mcpService;

    /**
     * 영어 질문에 대한 답변 생성
     * GET http://localhost:8080/api/v1/qa/ask?question=What is happening with climate change?
     */
    @GetMapping("/ask")
    public ResponseEntity<EnglishQAResponse> askQuestion(@RequestParam String question) {
        log.info("영어 질문 요청: {}", question);
        
        try {
            EnglishQAResponse response = englishQAService.generateAnswer(question);
            log.info("답변 생성 완료: {} 문장", response.getSentences().size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("질문 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용 가능한 AI 모델 목록 조회
     * GET http://localhost:8080/api/v1/qa/models
     */
    @GetMapping("/models")
    public ResponseEntity<String[]> getAvailableModels() {
        log.info("사용 가능한 모델 목록 조회");
        return ResponseEntity.ok(mcpService.getAvailableModels());
    }

    /**
     * AI 모델 선택
     * POST http://localhost:8080/api/v1/qa/models/select?model=mistral
     */
    @PostMapping("/models/select")
    public ResponseEntity<String> selectModel(@RequestParam String model) {
        log.info("모델 선택 요청: {}", model);
        
        try {
            mcpService.selectModel(model);
            return ResponseEntity.ok("모델이 성공적으로 변경되었습니다: " + model);
        } catch (Exception e) {
            log.error("모델 선택 중 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body("지원하지 않는 모델입니다: " + model);
        }
    }

    /**
     * Ollama 연결 상태 확인
     * GET http://localhost:8080/api/v1/qa/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("QA 서비스 헬스 체크");
        
        boolean isConnected = mcpService.isConnected();
        if (isConnected) {
            return ResponseEntity.ok("QA 서비스 정상 작동 중 (Ollama 연결됨)");
        } else {
            return ResponseEntity.status(503).body("QA 서비스 오류 (Ollama 연결 안됨)");
        }
    }
} 