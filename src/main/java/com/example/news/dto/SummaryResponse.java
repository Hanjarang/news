package com.example.news.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SummaryResponse {
  private Long id;
  private String originalText;
  private String summaryText;
  private LocalDateTime createdAt;
  private Long userId;        // 추가: 사용자 ID (비회원이면 null)
  private Boolean isSaved;    // 추가: 저장 여부 (회원이면 true, 비회원이면 false)
}
