package com.example.news.service.impl;

import com.example.news.dto.GuardianApiResponse;
import com.example.news.entity.NewsDocument;
import com.example.news.service.NewsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuardianApiServiceImpl implements NewsApiService {

    private final RestTemplate restTemplate;
    
    @Value("${guardian-api.base-url}")
    private String baseUrl;
    
    @Value("${guardian-api.api-key}")
    private String apiKey;
    
    @Value("${guardian-api.default-section}")
    private String defaultSection;
    
    @Value("${guardian-api.page-size}")
    private int pageSize;
    
    private final Random random = new Random();

    @Override
    public Optional<NewsDocument> getRandomNewsByKeyword(String keyword) {
        log.info("The Guardian API에서 키워드로 랜덤 뉴스 조회: {}", keyword);
        
        try {
            List<NewsDocument> newsList = searchNewsByKeyword(keyword);
            
            if (newsList.isEmpty()) {
                log.warn("키워드 '{}'에 해당하는 뉴스를 찾을 수 없습니다.", keyword);
                return Optional.empty();
            }
            
            // 랜덤하게 하나 선택
            NewsDocument randomNews = newsList.get(random.nextInt(newsList.size()));
            log.info("랜덤 뉴스 선택: {}", randomNews.getTitle());
            
            return Optional.of(randomNews);
            
        } catch (Exception e) {
            log.error("The Guardian API 키워드 검색 중 오류 발생: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<NewsDocument> searchNewsByKeyword(String keyword) {
        log.info("The Guardian API에서 키워드로 뉴스 검색: {}", keyword);
        
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/search")
                    .queryParam("q", keyword)
                    .queryParam("api-key", apiKey)
                    .queryParam("page-size", pageSize)
                    .queryParam("show-fields", "headline,standfirst,byline,bodyText,wordcount")
                    .queryParam("show-tags", "contributor")
                    .queryParam("order-by", "newest")
                    .build()
                    .toUriString();
            
            log.info("The Guardian API 요청 URL: {}", url.replace(apiKey, "***"));
            
            GuardianApiResponse response = restTemplate.getForObject(url, GuardianApiResponse.class);
            
            if (response != null && response.getResponse() != null && response.getResponse().getResults() != null) {
                return response.getResponse().getResults().stream()
                        .map(this::convertToNewsDocument)
                        .collect(Collectors.toList());
            } else {
                log.warn("The Guardian API 응답이 올바르지 않습니다: {}", response);
                return List.of();
            }
            
        } catch (Exception e) {
            log.error("The Guardian API 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Optional<NewsDocument> getRandomLatestNews() {
        log.info("The Guardian API에서 최신 뉴스 랜덤 조회");
        
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/" + defaultSection)
                    .queryParam("api-key", apiKey)
                    .queryParam("page-size", pageSize)
                    .queryParam("show-fields", "headline,standfirst,byline,bodyText,wordcount")
                    .queryParam("show-tags", "contributor")
                    .queryParam("order-by", "newest")
                    .build()
                    .toUriString();
            
            log.info("The Guardian API 요청 URL: {}", url.replace(apiKey, "***"));
            
            GuardianApiResponse response = restTemplate.getForObject(url, GuardianApiResponse.class);
            
            if (response != null && response.getResponse() != null && response.getResponse().getResults() != null && !response.getResponse().getResults().isEmpty()) {
                GuardianApiResponse.GuardianArticle randomArticle = response.getResponse().getResults().get(random.nextInt(response.getResponse().getResults().size()));
                NewsDocument randomNews = convertToNewsDocument(randomArticle);
                log.info("랜덤 최신 뉴스 선택: {}", randomNews.getTitle());
                return Optional.of(randomNews);
            } else {
                log.warn("The Guardian API 최신 뉴스 응답이 올바르지 않습니다: {}", response);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("The Guardian API 최신 뉴스 조회 중 오류 발생: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<NewsDocument> getRandomNewsByCategory(String category) {
        log.info("The Guardian API에서 카테고리로 랜덤 뉴스 조회: {}", category);
        
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/" + category)
                    .queryParam("api-key", apiKey)
                    .queryParam("page-size", pageSize)
                    .queryParam("show-fields", "headline,standfirst,byline,bodyText,wordcount")
                    .queryParam("show-tags", "contributor")
                    .queryParam("order-by", "newest")
                    .build()
                    .toUriString();
            
            log.info("The Guardian API 요청 URL: {}", url.replace(apiKey, "***"));
            
            GuardianApiResponse response = restTemplate.getForObject(url, GuardianApiResponse.class);
            
            if (response != null && response.getResponse() != null && response.getResponse().getResults() != null && !response.getResponse().getResults().isEmpty()) {
                GuardianApiResponse.GuardianArticle randomArticle = response.getResponse().getResults().get(random.nextInt(response.getResponse().getResults().size()));
                NewsDocument randomNews = convertToNewsDocument(randomArticle);
                log.info("랜덤 카테고리 뉴스 선택: {}", randomNews.getTitle());
                return Optional.of(randomNews);
            } else {
                log.warn("The Guardian API 카테고리 뉴스 응답이 올바르지 않습니다: {}", response);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("The Guardian API 카테고리 뉴스 조회 중 오류 발생: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<NewsDocument> getRandomNewsByNaturalLanguage(String naturalLanguage) {
        log.info("The Guardian API에서 자연어로 랜덤 뉴스 조회: {}", naturalLanguage);
        
        try {
            List<NewsDocument> newsList = searchNewsByNaturalLanguage(naturalLanguage);
            
            if (newsList.isEmpty()) {
                log.warn("자연어 '{}'에 해당하는 뉴스를 찾을 수 없습니다.", naturalLanguage);
                return Optional.empty();
            }
            
            // 랜덤하게 하나 선택
            NewsDocument randomNews = newsList.get(random.nextInt(newsList.size()));
            log.info("자연어 검색 랜덤 뉴스 선택: {}", randomNews.getTitle());
            
            return Optional.of(randomNews);
            
        } catch (Exception e) {
            log.error("The Guardian API 자연어 검색 중 오류 발생: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<NewsDocument> searchNewsByNaturalLanguage(String naturalLanguage) {
        log.info("The Guardian API에서 자연어로 뉴스 검색: {}", naturalLanguage);
        
        try {
            // 자연어 검색을 위해 따옴표로 감싸서 정확한 구문 검색
            String searchQuery = "\"" + naturalLanguage + "\"";
            
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/search")
                    .queryParam("q", searchQuery)
                    .queryParam("api-key", apiKey)
                    .queryParam("page-size", pageSize)
                    .queryParam("show-fields", "headline,standfirst,byline,bodyText,wordcount")
                    .queryParam("show-tags", "contributor")
                    .queryParam("order-by", "newest")
                    .build()
                    .toUriString();
            
            log.info("The Guardian API 자연어 검색 요청 URL: {}", url.replace(apiKey, "***"));
            
            GuardianApiResponse response = restTemplate.getForObject(url, GuardianApiResponse.class);
            
            if (response != null && response.getResponse() != null && response.getResponse().getResults() != null) {
                return response.getResponse().getResults().stream()
                        .map(this::convertToNewsDocument)
                        .collect(Collectors.toList());
            } else {
                log.warn("The Guardian API 자연어 검색 응답이 올바르지 않습니다: {}", response);
                return List.of();
            }
            
        } catch (Exception e) {
            log.error("The Guardian API 자연어 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private NewsDocument convertToNewsDocument(GuardianApiResponse.GuardianArticle article) {
        LocalDateTime publishedAt = null;
        if (article.getWebPublicationDate() != null) {
            try {
                publishedAt = LocalDateTime.parse(article.getWebPublicationDate(), 
                    DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                log.warn("날짜 파싱 실패: {}", article.getWebPublicationDate());
                publishedAt = LocalDateTime.now();
            }
        } else {
            publishedAt = LocalDateTime.now();
        }
        
        // 필드에서 내용 추출
        String title = article.getWebTitle();
        String content = "";
        String author = "Unknown";
        
        if (article.getFields() != null) {
            GuardianApiResponse.Fields fields = article.getFields();
            if (fields.getHeadline() != null) {
                title = fields.getHeadline();
            }
            if (fields.getBodyText() != null) {
                content = fields.getBodyText();
            } else if (fields.getStandfirst() != null) {
                content = fields.getStandfirst();
            }
            if (fields.getByline() != null) {
                author = fields.getByline();
            }
        }
        
        // 태그에서 저자 정보 추출
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            GuardianApiResponse.Tag contributor = article.getTags().stream()
                    .filter(tag -> "contributor".equals(tag.getType()))
                    .findFirst()
                    .orElse(null);
            if (contributor != null && contributor.getWebTitle() != null) {
                author = contributor.getWebTitle();
            }
        }
        
        return NewsDocument.builder()
                .title(title)
                .content(content)
                .source("The Guardian")
                .url(article.getWebUrl())
                .publishedAt(publishedAt)
                .category(article.getSectionName() != null ? article.getSectionName() : "General")
                .author(author)
                .language("en")
                .build();
    }
} 