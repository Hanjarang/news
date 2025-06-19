package com.example.news.service;

import com.example.news.dto.SummaryRequest;
import com.example.news.dto.SummaryResponse;

public interface AIService {

  /**
   * 본문 텍스트 요약
   * @param request 요약 요청 DTO
   * @return 응답 DTO
   */
  SummaryResponse summarizeText(SummaryRequest request);

  /**
   * 자연어 검색
   * @param request 검색 요청 DTO
   * @return 검색 결과 DTO
   */
  SummaryResponse searchText(SummaryRequest request);


}
