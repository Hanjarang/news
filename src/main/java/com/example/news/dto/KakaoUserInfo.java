package com.example.news.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class KakaoUserInfo implements OAuth2UserInfo {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("connected_at")
    private String connectedAt;
    
    @JsonProperty("properties")
    private Map<String, Object> properties;
    
    @JsonProperty("kakao_account")
    private Map<String, Object> kakaoAccount;
    
    @Override
    public String getProvider() {
        return "kakao";
    }
    
    @Override
    public String getProviderId() {
        return String.valueOf(id);
    }
    
    @Override
    public String getName() {
        if (properties != null && properties.containsKey("nickname")) {
            return (String) properties.get("nickname");
        }
        return null;
    }
    
    @Override
    public String getEmail() {
        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
            return (String) kakaoAccount.get("email");
        }
        return null;
    }
    
    @Override
    public String getProfileImage() {
        if (properties != null && properties.containsKey("profile_image")) {
            return (String) properties.get("profile_image");
        }
        return null;
    }
} 