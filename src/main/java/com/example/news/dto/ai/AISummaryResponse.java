package com.example.news.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AISummaryResponse {
  private String summary_text;

  public String getSummary_text() {
    return summary_text;
  }

  public void setSummary_text(String summary_text) {
    this.summary_text = summary_text;
  }
}
