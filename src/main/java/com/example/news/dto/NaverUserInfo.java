package com.example.news.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NaverUserInfo implements OAuth2UserInfo {

  @JsonProperty("response")
  private Map<String, Object> response;

  @Override
  public String getProvider() {
    return "naver";
  }

  @Override
  public String getProviderId() {
    return (String) response.get("id");
  }

  @Override
  public String getName() {
    return (String) response.get("name");
  }

  @Override
  public String getEmail() {
    return (String) response.get("email");
  }

  @Override
  public String getProfileImage() {
    return (String) response.get("profile_image");
  }
}