package com.ssafy.mvc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class OpenAiSttService {

    private final RestClient openAiSttRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiSttService(@Qualifier("openAiSttRestClient") RestClient openAiSttRestClient) {
        this.openAiSttRestClient = openAiSttRestClient;
    }

    // 오디오 바이트(WebM/Opus) → 텍스트 변환 (whisper-1)
    // languageCode: ko-KR/en-US 형태로 들어오면 ISO-639-1(ko/en)만 추출해 힌트로 전달
    // phraseHints: 회사명/면접관·지원자 이름 등 - Whisper의 prompt 파라미터로 전달해 인식 보조
    public String transcribe(byte[] audioBytes, String languageCode, List<String> phraseHints) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return "answer.webm";
                }
            });
            body.add("model", "whisper-1");
            if (languageCode != null && languageCode.contains("-")) {
                body.add("language", languageCode.split("-")[0]);
            }
            if (phraseHints != null && !phraseHints.isEmpty()) {
                body.add("prompt", String.join(", ", phraseHints));
            }

            String responseBody = openAiSttRestClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseTranscript(responseBody);

        } catch (Exception e) {
            log.error("OpenAI STT 호출 실패: {}", e.getMessage());
            throw new RuntimeException("STT 변환에 실패했습니다.", e);
        }
    }

    private String parseTranscript(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        return root.path("text").asText("");
    }
}
