package com.example.news.controller;

import com.example.news.dto.SummaryRequest;
import com.example.news.dto.SummaryResponse;
import com.example.news.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/summaries")
@RequiredArgsConstructor
public class SummaryController {

  private final SummaryService summaryService;

  @PostMapping
  public ResponseEntity<SummaryResponse> createSummary(@RequestBody SummaryRequest request) {
    return ResponseEntity.ok(summaryService.createSummary(request));
  }

  @GetMapping("/{summaryId}")
  public ResponseEntity<SummaryResponse> getSummary(@PathVariable Long summaryId) {
    return ResponseEntity.ok(summaryService.getSummary(summaryId));
  }

  @DeleteMapping("/{summaryId}")
  public ResponseEntity<Void> deleteSummary(@PathVariable Long summaryId) {
    summaryService.deleteSummary(summaryId);
    return  ResponseEntity.ok().build();
  }
}
