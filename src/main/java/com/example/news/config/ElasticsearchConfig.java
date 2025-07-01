package com.example.news.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "com.example.news.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.username:}")
    private String username;

    @Value("${elasticsearch.password:}")
    private String password;

    @Value("${elasticsearch.ssl.enabled:false}")
    private boolean sslEnabled;

    @Override
    @Bean
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(host + ":" + port);

        // 인증 정보가 있는 경우에만 추가
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }

        // SSL이 활성화된 경우 HTTPS 사용 (인증서 검증 비활성화)
        if (sslEnabled) {
            builder.usingSsl(createTrustAllSSLContext());
        }

        return builder.build();
    }

    private SSLContext createTrustAllSSLContext() {
        try {
            // 모든 인증서를 신뢰하는 TrustManager 생성
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("SSL Context 생성 실패", e);
        }
    }
}