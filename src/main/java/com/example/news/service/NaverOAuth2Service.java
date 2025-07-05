package com.example.news.service;

import com.example.news.dto.NaverUserInfo;
import com.example.news.dto.OAuth2UserInfo;
import com.example.news.entity.User;
import com.example.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverOAuth2Service implements OAuth2ProviderService {

  private final UserRepository userRepository;

  @Override
  public String getProviderName() {
    return "naver";
  }

  @Override
  public OAuth2UserInfo convertToUserInfo(Object oAuth2User) {
    OAuth2User user = (OAuth2User) oAuth2User;
    Map<String, Object> attributes = user.getAttributes();

    NaverUserInfo naverUserInfo = new NaverUserInfo();
    @SuppressWarnings("unchecked")
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");
    naverUserInfo.setResponse(response);

    return naverUserInfo;
  }

  @Override
  @Transactional
  public User processOAuth2Login(OAuth2UserInfo userInfo) {
    log.info("네이버 OAuth2 로그인 처리 시작: providerId={}", userInfo.getProviderId());

    User user = findOrCreateUser(userInfo);
    log.info("네이버 OAuth2 로그인 처리 완료: userId={}", user.getId());

    return user;
  }

  private User findOrCreateUser(OAuth2UserInfo userInfo) {
    return userRepository.findByProviderAndProviderId(
        userInfo.getProvider(),
        userInfo.getProviderId()
    ).orElseGet(() -> createNewUser(userInfo));
  }

  private User createNewUser(OAuth2UserInfo userInfo) {
    User newUser = User.builder()
        .provider(userInfo.getProvider())
        .providerId(userInfo.getProviderId())
        .name(userInfo.getName())
        .email(userInfo.getEmail())
        .createdAt(LocalDateTime.now())
        .build();

    User savedUser = userRepository.save(newUser);
    log.info("새 네이버 사용자 생성: userId={}, providerId={}",
        savedUser.getId(), userInfo.getProviderId());

    return savedUser;
  }
}
