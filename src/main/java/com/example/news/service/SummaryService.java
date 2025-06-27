package com.example.news.service;

import com.example.news.dto.SummaryRequest;
import com.example.news.dto.SummaryResponse;

public interface SummaryService {

  SummaryResponse createSummary(SummaryRequest request);
  SummaryResponse getSummary(Long summaryId);
  void deleteSummary(Long summaryId);
}
