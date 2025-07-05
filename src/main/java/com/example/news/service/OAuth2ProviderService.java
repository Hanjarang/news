package com.example.news.service;

import com.example.news.dto.OAuth2UserInfo;
import com.example.news.entity.User;

public interface OAuth2ProviderService {
  String getProviderName();
  OAuth2UserInfo convertToUserInfo(Object oAuth2User);
  User processOAuth2Login(OAuth2UserInfo userInfo);
}
