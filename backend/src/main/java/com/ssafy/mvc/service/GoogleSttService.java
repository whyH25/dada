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
public class GoogleSttService {

    private final RestClient googleSttRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.api-key}")
    private String apiKey;

    public GoogleSttService(@Qualifier("googleSttRestClient") RestClient googleSttRestClient) {
        this.googleSttRestClient = googleSttRestClient;
    }

    // 오디오 바이트(WebM/Opus) → 텍스트 변환
    public String transcribe(byte[] audioBytes) {
        try {
            String requestBody = buildRequest(audioBytes);

            String responseBody = googleSttRestClient.post()
                    .uri("/v1/speech:recognize?key=" + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseTranscript(responseBody);

        } catch (Exception e) {
            log.error("Google STT 호출 실패: {}", e.getMessage());
            throw new RuntimeException("STT 변환에 실패했습니다.", e);
        }
    }

    private String buildRequest(byte[] audioBytes) throws Exception {
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);

        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode config = root.putObject("config");
        config.put("encoding", "WEBM_OPUS");
        // WEBM_OPUS는 컨테이너 헤더에서 샘플레이트 자동 감지 - 하드코딩하면 기기마다 불일치 발생
        config.put("languageCode", "ko-KR");
        config.put("enableAutomaticPunctuation", true);
        root.putObject("audio").put("content", base64Audio);

        return objectMapper.writeValueAsString(root);
    }

    private String parseTranscript(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode results = root.path("results");
        if (results.isEmpty()) return "";

        return results.get(0)
                .path("alternatives").get(0)
                .path("transcript").asText("");
    }
}
