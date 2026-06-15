package com.ssafy.mvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GoogleSpeechConfig {

    @Bean
    public RestClient googleTtsRestClient() {
        return RestClient.builder()
                .baseUrl("https://texttospeech.googleapis.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public RestClient googleSttRestClient() {
        return RestClient.builder()
                .baseUrl("https://speech.googleapis.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
