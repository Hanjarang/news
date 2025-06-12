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
}
