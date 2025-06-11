package com.example.news.service;

public interface AIService {

  /**
   * 본문 텍스트 요약
   * @param originalText 원본 텍스트
   * @return 요약된 텍스트
   */
  String summarizeText(String originalText);

  /**
   * 자연어 검색
   * @param query 검색 쿼리
   * @return 검색 결과 텍스트
   */
  String searchText(String query);


}
