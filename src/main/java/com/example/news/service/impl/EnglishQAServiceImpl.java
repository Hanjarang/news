package com.example.news.service.impl;

import com.example.news.dto.EnglishQAResponse;
import com.example.news.entity.NewsDocument;
import com.example.news.service.ElasticsearchService;
import com.example.news.service.EnglishQAService;
import com.example.news.service.MCPService;
import com.example.news.service.NewsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnglishQAServiceImpl implements EnglishQAService {

    private final MCPService mcpService;
    private final NewsApiService newsApiService;
    private final ElasticsearchService elasticsearchService;
    
    @Override
    public EnglishQAResponse generateAnswer(String question) {
        log.info("영어 질문 처리 시작: {}", question);
        
        try {
            // 1. 질문에서 키워드 추출
            String keywords = extractKeywords(question);
            log.info("추출된 키워드: {}", keywords);
            
            // 2. 관련 뉴스 컨텍스트 수집
            String newsContext = collectNewsContext(keywords);
            log.info("수집된 뉴스 컨텍스트 길이: {} 문자", newsContext.length());
            
            // 3. AI 모델을 통한 답변 생성
            EnglishQAResponse response = mcpService.generateAnswer(question, newsContext);
            
            log.info("답변 생성 완료: {} 문장", response.getSentences().size());
            return response;
            
        } catch (Exception e) {
            log.error("답변 생성 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse(question);
        }
    }
    
    @Override
    public String extractKeywords(String question) {
        // 영어 질문에서 주요 키워드 추출
        // 간단한 키워드 추출 로직 (실제로는 더 정교한 NLP 처리가 필요)
        String[] stopWords = {"what", "when", "where", "who", "why", "how", "is", "are", "was", "were", 
                             "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", 
                             "with", "by", "from", "about", "into", "through", "during", "before", 
                             "after", "above", "below", "between", "among", "this", "that", "these", 
                             "those", "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", 
                             "us", "them", "my", "your", "his", "her", "its", "our", "their"};
        
        String[] words = question.toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", " ")
                .split("\\s+");
        
        StringBuilder keywords = new StringBuilder();
        for (String word : words) {
            if (word.length() > 2 && !contains(stopWords, word)) {
                keywords.append(word).append(" ");
            }
        }
        
        return keywords.toString().trim();
    }
    
    private boolean contains(String[] array, String target) {
        for (String item : array) {
            if (item.equals(target)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String collectNewsContext(String keywords) {
        StringBuilder context = new StringBuilder();
        
        try {
            // 1. Elasticsearch에서 캐시된 뉴스 검색
            List<NewsDocument> cachedNews = elasticsearchService.searchNewsByKeyword(keywords);
            
            // 2. Guardian API에서 실시간 뉴스 검색
            List<NewsDocument> guardianNews = newsApiService.searchNewsByKeyword(keywords);
            
            // 3. 컨텍스트 구성
            if (!cachedNews.isEmpty()) {
                context.append("Cached News:\n");
                for (int i = 0; i < Math.min(cachedNews.size(), 3); i++) {
                    NewsDocument news = cachedNews.get(i);
                    context.append("- ").append(news.getTitle()).append("\n");
                    if (news.getContent() != null) {
                        context.append("  ").append(news.getContent().substring(0, 
                            Math.min(news.getContent().length(), 200))).append("...\n");
                    }
                }
            }
            
            if (!guardianNews.isEmpty()) {
                context.append("\nRecent News:\n");
                for (int i = 0; i < Math.min(guardianNews.size(), 2); i++) {
                    NewsDocument news = guardianNews.get(i);
                    context.append("- ").append(news.getTitle()).append("\n");
                    if (news.getContent() != null) {
                        context.append("  ").append(news.getContent().substring(0, 
                            Math.min(news.getContent().length(), 200))).append("...\n");
                    }
                }
            }
            
            // 4. 컨텍스트가 비어있으면 기본 정보 제공
            if (context.length() == 0) {
                context.append("No specific news found for the keywords: ").append(keywords)
                       .append("\nProviding general information based on the question.");
            }
            
        } catch (Exception e) {
            log.error("뉴스 컨텍스트 수집 중 오류: {}", e.getMessage());
            context.append("Unable to retrieve specific news. Providing general information.");
        }
        
        return context.toString();
    }
    
    private EnglishQAResponse createErrorResponse(String question) {
        return EnglishQAResponse.builder()
                .question(question)
                .answer("I apologize, but I'm unable to provide a detailed answer at the moment.")
                .sentences(List.of(
                    "I apologize, but I'm unable to provide a detailed answer at the moment.",
                    "Please try rephrasing your question or check your internet connection.",
                    "The system is currently experiencing technical difficulties.",
                    "You may want to try again in a few moments.",
                    "For immediate assistance, please contact support."
                ))
                .modelUsed("error")
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
} 