package com.example.news.exception;

public class SummaryNotFoundException extends RuntimeException {
  public SummaryNotFoundException(Long summaryId) {
    super("해당 뉴스를 찾을 수 없습니다. : " + summaryId);
  }

}
