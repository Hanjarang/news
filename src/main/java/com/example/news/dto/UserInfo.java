package com.example.news.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserInfo {
    private Long id;
    private String provider;
    private String providerId;
    private String name;
    private String email;
    private LocalDateTime createdAt;
} 