package com.example.news.service;

import com.example.news.entity.NewsDocument;

import java.util.List;
import java.util.Optional;

public interface ElasticsearchService {

    // 뉴스 문서 저장
    NewsDocument saveNews(NewsDocument newsDocument);

    // 뉴스 문서 일괄 저장
    Iterable<NewsDocument> saveAllNews(List<NewsDocument> newsDocuments);

    // ID로 뉴스 문서 조회
    Optional<NewsDocument> findNewsById(String id);

    // 키워드로 뉴스 검색 (제목과 내용에서)
    List<NewsDocument> searchNewsByKeyword(String keyword);

    // 키워드로 랜덤 뉴스 하나 조회
    Optional<NewsDocument> findRandomNewsByKeyword(String keyword);

    // 모든 뉴스 조회
    List<NewsDocument> findAllNews();

    // 뉴스 문서 삭제
    void deleteNews(String id);

    // 인덱스 존재 여부 확인
    boolean indexExists(String indexName);

    // 인덱스 생성
    boolean createIndex(String indexName);
} 