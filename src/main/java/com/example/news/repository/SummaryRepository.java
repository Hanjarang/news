package com.example.news.repository;

import com.example.news.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

}
