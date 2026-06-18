package com.ssafy.mvc.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GcsConfig {

    @Value("${gcs.credentials-path}")
    private String credentialsPath;

    // 서비스 계정 키 파일로 인증된 Storage 클라이언트
    // signed URL 발급에 서비스 계정의 개인키가 필요하므로 ADC 대신 키 파일을 직접 로드
    @Bean
    public Storage storage() throws IOException {
        try (FileInputStream keyStream = new FileInputStream(credentialsPath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(keyStream);
            return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        }
    }
}
