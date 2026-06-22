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
                .put("languageCode", extractLanguageCode(voiceType))
                .put("name", voiceType);
        root.putObject("audioConfig").put("audioEncoding", "MP3");
        return objectMapper.writeValueAsString(root);
    }

    // 구글 voice 이름은 "languageCode-종류-변형" 형태(예: ko-KR-Standard-A, en-US-Wavenet-D)라서
    // 앞 두 토큰을 합치면 그 목소리의 언어 코드가 됨. 영어 페르소나도 voice_type만 잘 채우면 별도 처리 없이 동작
    private String extractLanguageCode(String voiceType) {
        String[] parts = voiceType.split("-");
        return parts.length >= 2 ? parts[0] + "-" + parts[1] : "ko-KR";
    }
}
