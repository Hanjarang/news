package com.example.news.dto.ai;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AISearchResponse {
  private List<SearchResult> results;
  private int totalResults;
  private double searchTime;

  @Getter
  @Setter
  public static class SearchResult {
    private String text;
    private String summary;
    private double relevance;
    private String source;
  }

}
