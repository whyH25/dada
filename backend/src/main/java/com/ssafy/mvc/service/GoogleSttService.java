package com.ssafy.mvc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;

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

    // 오디오 바이트(WebM/Opus) → 텍스트 변환 (languageCode 예: ko-KR, en-US)
    // phraseHints: 회사명/면접관·지원자 이름처럼 다른 언어 고유명사가 섞여도 인식이 끊기지 않도록 주는 힌트
    public String transcribe(byte[] audioBytes, String languageCode, List<String> phraseHints) {
        try {
            String requestBody = buildRequest(audioBytes, languageCode, phraseHints);

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

    private String buildRequest(byte[] audioBytes, String languageCode, List<String> phraseHints) throws Exception {
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);

        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode config = root.putObject("config");
        config.put("encoding", "WEBM_OPUS");
        // WEBM_OPUS는 컨테이너 헤더에서 샘플레이트 자동 감지 - 하드코딩하면 기기마다 불일치 발생
        config.put("languageCode", languageCode);
        config.put("enableAutomaticPunctuation", true);
        if (phraseHints != null && !phraseHints.isEmpty()) {
            ArrayNode contexts = config.putArray("speechContexts");
            ArrayNode phrases = contexts.addObject().putArray("phrases");
            phraseHints.forEach(phrases::add);
        }
        root.putObject("audio").put("content", base64Audio);

        return objectMapper.writeValueAsString(root);
    }

    // 발화 중 침묵 구간이 있으면 results가 여러 개로 나뉘는데, 첫 번째 것만 보면
    // 그 뒤에 이어 말한 내용이 전부 누락됨 - 모든 결과를 이어붙여야 함
    private String parseTranscript(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode results = root.path("results");
        if (results.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (JsonNode result : results) {
            String segment = result.path("alternatives").get(0).path("transcript").asText("");
            if (segment.isBlank()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(segment);
        }
        return sb.toString();
    }
}
