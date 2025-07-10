package com.example.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnglishQAResponse {
    
    private String question;
    private String answer;
    private List<String> sentences; // 5문장 구조화된 답변
    private String modelUsed;
    private String newsContext;
    private List<String> relatedNewsUrls;
    private LocalDateTime timestamp;
    private String grammarNotes;
    private String vocabularyNotes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sentence {
        private String content;
        private String type; // "answer", "context", "background", "implications", "future"
        private String explanation;
    }
} 