package com.example.news.service;

import com.example.news.dto.SummaryRequest;
import com.example.news.dto.SummaryResponse;
import com.example.news.dto.SearchRequest;

public interface SummaryService {

  SummaryResponse createSummary(SummaryRequest request);
  SummaryResponse searchSummary(SummaryRequest request);
  SummaryResponse getSummary(Long summaryId);
  void deleteSummary(Long summaryId);
}
