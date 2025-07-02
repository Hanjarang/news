package com.example.news.service;

import com.example.news.dto.GoogleUserInfo;
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
public class GoogleOAuth2Service implements OAuth2ProviderService {
    
    private final UserRepository userRepository;
    
    @Override
    public String getProviderName() {
        return "google";
    }
    
    @Override
    public OAuth2UserInfo convertToUserInfo(Object oAuth2User) {
        OAuth2User user = (OAuth2User) oAuth2User;
        Map<String, Object> attributes = user.getAttributes();
        
        GoogleUserInfo googleUserInfo = new GoogleUserInfo();
        googleUserInfo.setSub((String) attributes.get("sub"));
        googleUserInfo.setName((String) attributes.get("name"));
        googleUserInfo.setGivenName((String) attributes.get("given_name"));
        googleUserInfo.setFamilyName((String) attributes.get("family_name"));
        googleUserInfo.setPicture((String) attributes.get("picture"));
        googleUserInfo.setEmail((String) attributes.get("email"));
        googleUserInfo.setEmailVerified((Boolean) attributes.get("email_verified"));
        googleUserInfo.setLocale((String) attributes.get("locale"));
        
        return googleUserInfo;
    }
    
    @Override
    @Transactional
    public User processOAuth2Login(OAuth2UserInfo userInfo) {
        log.info("구글 OAuth2 로그인 처리 시작: providerId={}", userInfo.getProviderId());
        
        User user = findOrCreateUser(userInfo);
        log.info("구글 OAuth2 로그인 처리 완료: userId={}", user.getId());
        
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
        log.info("새 구글 사용자 생성: userId={}, providerId={}", 
                savedUser.getId(), userInfo.getProviderId());
        
        return savedUser;
    }
} 