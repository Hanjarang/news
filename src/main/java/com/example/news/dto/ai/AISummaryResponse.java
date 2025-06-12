package com.example.news.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AISummaryResponse {
  private String summary;
  private String originalText;
  private int originalLength;
  private int summaryLength;
  private double compressionRatio;

}
