package com.example.news.service.impl;

import com.example.news.entity.NewsDocument;
import com.example.news.repository.NewsElasticsearchRepository;
import com.example.news.service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchServiceImpl implements ElasticsearchService {

    private final NewsElasticsearchRepository newsRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final Random random = new Random();

    @Override
    public NewsDocument saveNews(NewsDocument newsDocument) {
        log.info("뉴스 문서 저장: {}", newsDocument.getTitle());
        return newsRepository.save(newsDocument);
    }

    @Override
    public Iterable<NewsDocument> saveAllNews(List<NewsDocument> newsDocuments) {
        log.info("뉴스 문서 일괄 저장: {}개", newsDocuments.size());
        return newsRepository.saveAll(newsDocuments);
    }

    @Override
    public Optional<NewsDocument> findNewsById(String id) {
        log.info("뉴스 문서 조회: ID = {}", id);
        return newsRepository.findById(id);
    }

    @Override
    public List<NewsDocument> searchNewsByKeyword(String keyword) {
        log.info("키워드로 뉴스 검색: {}", keyword);
        
        try {
            // Iterable을 List로 변환
            Iterable<NewsDocument> allNewsIterable = newsRepository.findAll();
            List<NewsDocument> allNews = new ArrayList<>();
            for (NewsDocument news : allNewsIterable) {
                allNews.add(news);
            }
            
            return allNews.stream()
                    .filter(news -> 
                        (news.getTitle() != null && news.getTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                        (news.getContent() != null && news.getContent().toLowerCase().contains(keyword.toLowerCase()))
                    )
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("키워드 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Optional<NewsDocument> findRandomNewsByKeyword(String keyword) {
        log.info("키워드로 랜덤 뉴스 조회: {}", keyword);
        List<NewsDocument> newsList = searchNewsByKeyword(keyword);
        
        if (newsList.isEmpty()) {
            log.warn("키워드 '{}'에 해당하는 뉴스를 찾을 수 없습니다.", keyword);
            return Optional.empty();
        }
        
        // 현재 시간을 시드로 사용하여 매번 다른 랜덤 값 생성
        Random timeBasedRandom = new Random(System.currentTimeMillis());
        // 랜덤하게 하나 선택
        NewsDocument randomNews = newsList.get(timeBasedRandom.nextInt(newsList.size()));
        log.info("랜덤 뉴스 선택: {}", randomNews.getTitle());
        
        return Optional.of(randomNews);
    }

    @Override
    public List<NewsDocument> findAllNews() {
        log.info("모든 뉴스 조회");
        Iterable<NewsDocument> allNewsIterable = newsRepository.findAll();
        List<NewsDocument> allNews = new ArrayList<>();
        for (NewsDocument news : allNewsIterable) {
            allNews.add(news);
        }
        return allNews;
    }

    @Override
    public void deleteNews(String id) {
        log.info("뉴스 문서 삭제: ID = {}", id);
        newsRepository.deleteById(id);
    }

    @Override
    public boolean indexExists(String indexName) {
        try {
            return elasticsearchOperations.indexOps(NewsDocument.class).exists();
        } catch (Exception e) {
            log.error("인덱스 존재 여부 확인 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean createIndex(String indexName) {
        try {
            return elasticsearchOperations.indexOps(NewsDocument.class).create();
        } catch (Exception e) {
            log.error("인덱스 생성 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
}