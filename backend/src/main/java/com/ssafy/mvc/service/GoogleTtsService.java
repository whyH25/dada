package com.ssafy.mvc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Slf4j
@Service
public class GoogleTtsService {

    private final RestClient googleTtsRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.api-key}")
    private String apiKey;

    public GoogleTtsService(@Qualifier("googleTtsRestClient") RestClient googleTtsRestClient) {
        this.googleTtsRestClient = googleTtsRestClient;
    }

    // 텍스트 + 목소리 타입 → MP3 바이트
    public byte[] synthesize(String text, String voiceType) {
        try {
            String requestBody = buildRequest(text, voiceType);

            String responseBody = googleTtsRestClient.post()
                    .uri("/v1/text:synthesize?key=" + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String base64Audio = root.path("audioContent").asText();
            return Base64.getDecoder().decode(base64Audio);

        } catch (Exception e) {
            log.error("Google TTS 호출 실패: {}", e.getMessage());
            throw new RuntimeException("TTS 변환에 실패했습니다.", e);
        }
    }

    private String buildRequest(String text, String voiceType) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.putObject("input").put("text", text);
        root.putObject("voice")
                .put("languageCode", "ko-KR")
                .put("name", voiceType);
        root.putObject("audioConfig").put("audioEncoding", "MP3");
        return objectMapper.writeValueAsString(root);
    }
}
