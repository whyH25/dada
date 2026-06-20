package com.ssafy.mvc.controller;

import com.ssafy.mvc.dao.InterviewScenarioDao;
import com.ssafy.mvc.dto.InterviewScenarioDto;
import com.ssafy.mvc.service.GoogleSttService;
import com.ssafy.mvc.service.GoogleTtsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/speech")
@RequiredArgsConstructor
public class GoogleSpeechController {

    private final GoogleTtsService googleTtsService;
    private final GoogleSttService googleSttService;
    private final InterviewScenarioDao interviewScenarioDao;

    // scenarioId → DB에서 text + voiceType 조회 → MP3 반환
    @PostMapping("/tts")
    public ResponseEntity<byte[]> tts(@RequestBody Map<String, Long> body) {
        Long scenarioId = body.get("scenarioId");

        InterviewScenarioDto scenario = interviewScenarioDao.selectForTts(scenarioId);
        if (scenario == null || scenario.getSpeechText() == null) {
            return ResponseEntity.badRequest().build();
        }

        byte[] audioBytes = googleTtsService.synthesize(scenario.getSpeechText(), scenario.getVoiceType());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(audioBytes);
    }

    // 음성 → 텍스트 변환 + answer_text/answer_sec DB 저장
    // audioFile은 마이크 스트림이 없는 등의 사유로 없을 수 있어 필수로 받지 않음
    @PostMapping(value = "/stt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> stt(
            @RequestParam(required = false) MultipartFile audioFile,
            @RequestParam(required = false) Long scenarioId,
            @RequestParam(required = false) Integer answerSec
    ) throws IOException {
        String transcript = "";
        try {
            if (audioFile != null && !audioFile.isEmpty()) {
                transcript = googleSttService.transcribe(audioFile.getBytes());
            }
        } catch (Exception e) {
            log.error("Google STT 실패 (scenarioId={}): {}", scenarioId, e.getMessage());
        }

        if (scenarioId != null) {
            interviewScenarioDao.updateAnswer(scenarioId, transcript.isBlank() ? null : transcript, answerSec);
            log.info("answer 저장 완료 (scenarioId={}, answerSec={}, textLength={})", scenarioId, answerSec, transcript.length());
        } else {
            log.warn("answer 저장 스킵 (scenarioId={}, transcript='{}')", scenarioId, transcript);
        }

        return ResponseEntity.ok(Map.of("transcript", transcript));
    }
}
