package com.example.news.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class GoogleUserInfo implements OAuth2UserInfo {
    
    @JsonProperty("sub")
    private String sub;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("given_name")
    private String givenName;
    
    @JsonProperty("family_name")
    private String familyName;
    
    @JsonProperty("picture")
    private String picture;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    
    @JsonProperty("locale")
    private String locale;
    
    @Override
    public String getProvider() {
        return "google";
    }
    
    @Override
    public String getProviderId() {
        return sub;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getEmail() {
        return email;
    }
    
    @Override
    public String getProfileImage() {
        return picture;
    }
} 