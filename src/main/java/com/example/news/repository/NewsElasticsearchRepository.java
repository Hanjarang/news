package com.example.news.repository;

import com.example.news.entity.NewsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface NewsElasticsearchRepository extends ElasticsearchRepository<NewsDocument, String> {
    // 기본 CRUD 메서드들은 ElasticsearchRepository에서 제공됨
} 