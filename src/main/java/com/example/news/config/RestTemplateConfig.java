package com.example.news.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(10000); // 10초 연결 타임아웃
    factory.setReadTimeout(120000);   // 120초 읽기 타임아웃 (HuggingFace API 모델 로딩 시간 고려)
    
    return new RestTemplate(factory);
  }
}
