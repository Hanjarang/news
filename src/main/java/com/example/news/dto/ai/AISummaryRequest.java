package com.example.news.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AISummaryRequest {
  @JsonProperty("inputs")
  private String inputs;

  public AISummaryRequest(String inputs) {
    this.inputs = inputs;
  }

  public String getInputs() {
    return inputs;
  }

  public void setInputs(String inputs) {
    this.inputs = inputs;
  }
}