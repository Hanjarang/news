package com.example.news.service.impl;

import com.example.news.dto.SummaryRequest;
import com.example.news.dto.SummaryResponse;
import com.example.news.entity.Summary;
import com.example.news.exception.AIServiceException;
import com.example.news.exception.InvalidInputException;
import com.example.news.exception.SummaryNotFoundException;
import com.example.news.repository.SummaryRepository;
import com.example.news.service.AIService;
import com.example.news.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SummaryServiceImpl implements SummaryService {

  private final SummaryRepository summaryRepository;
  private final AIService aiService;

  @Override
  public SummaryResponse createSummary(SummaryRequest request) {
    if (request.getOriginalText() == null || request.getOriginalText().trim().isEmpty()) {
      throw new InvalidInputException("Original text cannot be empty");
    }

    try{
      // AI 모델 사용해서 요약 생성 로직 구현
      String summaryText = aiService.summarizeText(request.getOriginalText());

      Summary summary = Summary.builder()
          .originalText(request.getOriginalText())
          .summaryText(summaryText) // 임시 데이터
          .build();

      Summary savedSummary = summaryRepository.save(summary);
      return convertToResponse(savedSummary);
    } catch (Exception e){
      throw new AIServiceException("Failed to create summary", e);
    }
  }

  @Override
  public SummaryResponse searchSummary(SummaryRequest request) {
    if (request.getOriginalText() == null || request.getOriginalText().trim().isEmpty()) {
      throw new InvalidInputException("Search query cannot be empty");
    }

    try {
      // 검색 로직 구현
      String summaryText = aiService.searchText(request.getOriginalText());

      Summary summary = Summary.builder()
          .originalText(request.getOriginalText())
          .summaryText(summaryText) // 임시 데이터
          .build();

      Summary savedSummary = summaryRepository.save(summary);
      return convertToResponse(savedSummary);
    } catch (Exception e) {
      throw new AIServiceException("Failed to search summary", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public SummaryResponse getSummary(Long summaryId) {
    Summary summary = summaryRepository.findById(summaryId)
        .orElseThrow(() -> new SummaryNotFoundException(summaryId));
    return convertToResponse(summary);
  }

  @Override
  public void deleteSummary(Long summaryId) {
    //  삭제 로직 구현
    if (!summaryRepository.existsById(summaryId)) {
      throw new SummaryNotFoundException(summaryId);
    }
    summaryRepository.deleteById(summaryId);
  }

  private SummaryResponse convertToResponse(Summary summary) {
    return SummaryResponse.builder()
        .id(summary.getId())
        .originalText(summary.getOriginalText())
        .summaryText(summary.getSummaryText())
        .createAt(summary.getCreatedAt())
        .build();
  }
}
