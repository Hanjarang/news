package com.example.news.dto.ai;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AISummaryRequest {
  private String text;
  private String language;
  private int maxLength;
}
