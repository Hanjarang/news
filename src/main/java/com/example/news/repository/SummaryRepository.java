package com.example.news.repository;

import com.example.news.entity.Summary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

  Page<Summary> findByUserId(Long userId, Pageable pageable);
}
