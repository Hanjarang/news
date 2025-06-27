package com.example.news.service.impl;

import com.example.news.dto.SummaryRequest;
import com.example.news.dto.SummaryResponse;
import com.example.news.entity.Summary;
import com.example.news.exception.AIServiceException;
import com.example.news.exception.InvalidInputException;
import com.example.news.exception.SummaryNotFoundException;
import com.example.news.exception.UnauthorizedException;
import com.example.news.repository.SummaryRepository;
import com.example.news.service.AIService;
import com.example.news.service.SummaryService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.news.entity.User;
import com.example.news.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class SummaryServiceImpl implements SummaryService {

  private final SummaryRepository summaryRepository;
  private final AIService aiService;
  private final UserRepository userRepository;

  // 현재 로그인한 사용자 정보를 가져오는 메서드
  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated() ||
        "anonymousUser".equals(authentication.getPrincipal())) {
      return null; // 비회원
    }

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String email = userDetails.getUsername(); // 또는 providerId

    return userRepository.findByEmail(email)
        .orElse(null);
  }

  @Override
  public SummaryResponse createSummary(SummaryRequest request) {
    if (request.getOriginalText() == null || request.getOriginalText().trim().isEmpty()) {
      throw new InvalidInputException("Original text cannot be empty");
    }

    try {
      // AI 모델 사용해서 요약 생성 로직 구현
      SummaryResponse aiResponse = aiService.summarizeText(request);

      // 현재 사용자 정보 가져오기
      User currentUser = getCurrentUser();

      Summary summary = Summary.builder()
          .user(currentUser) // 로그인한 사용자면 User 객체, 비회원이면 null
          .originalText(request.getOriginalText())
          .summaryText(aiResponse.getSummaryText())
          .createdAt(LocalDateTime.now())
          .build();

      // 회원인 경우에만 DB에 저장
      Summary savedSummary = null;
      if (currentUser != null) {
        savedSummary = summaryRepository.save(summary);
      }

      // 응답 생성 (저장 여부와 관계없이 요약 결과 반환)
      return SummaryResponse.builder()
          .id(savedSummary != null ? savedSummary.getId() : null)
          .originalText(aiResponse.getOriginalText()) // 요약된 영어 텍스트
          .summaryText(aiResponse.getSummaryText())   // 번역된 한글 텍스트
          .createdAt(LocalDateTime.now())
          .userId(currentUser != null ? currentUser.getId() : null) // 사용자 ID 추가
          .isSaved(currentUser != null) // 저장 여부 표시
          .build();

    } catch (Exception e) {
      throw new AIServiceException("Failed to create summary", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public SummaryResponse getSummary(Long summaryId) {
    User currentUser = getCurrentUser();

    if (currentUser == null) {
      throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
    }

    Summary summary = summaryRepository.findById(summaryId)
        .orElseThrow(() -> new SummaryNotFoundException(summaryId));

    // 본인의 요약만 조회 가능
    if (!summary.getUser().getId().equals(currentUser.getId())) {
      throw new UnauthorizedException("본인의 요약만 조회할 수 있습니다.");
    }

    return convertToResponse(summary);
  }

  @Override
  public void deleteSummary(Long summaryId) {
    User currentUser = getCurrentUser();

    if (currentUser == null) {
      throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
    }

    Summary summary = summaryRepository.findById(summaryId)
        .orElseThrow(() -> new SummaryNotFoundException(summaryId));

    // 본인의 요약만 삭제 가능
    if (!summary.getUser().getId().equals(currentUser.getId())) {
      throw new UnauthorizedException("본인의 요약만 삭제할 수 있습니다.");
    }

    summaryRepository.deleteById(summaryId);
  }

  // 회원의 요약 내역 조회 메서드 추가
  public Page<SummaryResponse> getMySummaries(Pageable pageable) {
    User currentUser = getCurrentUser();

    if (currentUser == null) {
      throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
    }

    Page<Summary> summaries = summaryRepository.findByUserId(currentUser.getId(), pageable);
    return summaries.map(this::convertToResponse);
  }

  private SummaryResponse convertToResponse(Summary summary) {
    return SummaryResponse.builder()
        .id(summary.getId())
        .originalText(summary.getOriginalText())
        .summaryText(summary.getSummaryText())
        .createdAt(summary.getCreatedAt())
        .userId(summary.getUser() != null ? summary.getUser().getId() : null)
        .isSaved(summary.getUser() != null)
        .build();
  }
}
