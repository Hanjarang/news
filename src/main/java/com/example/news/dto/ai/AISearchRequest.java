package com.example.news.dto.ai;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AISearchRequest {
  private String query;
  private String language;
  private int maxResults;
}
