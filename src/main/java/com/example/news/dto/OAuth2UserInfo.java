package com.example.news.dto;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getName();
    String getEmail();
    String getProfileImage();
} 