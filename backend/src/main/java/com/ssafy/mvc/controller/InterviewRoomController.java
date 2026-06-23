package com.ssafy.mvc.controller;

import com.ssafy.mvc.dao.InterviewScenarioDao;
import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.InterviewRoomDto;
import com.ssafy.mvc.dto.InterviewScenarioDto;
import com.ssafy.mvc.dto.InterviewStartResultDto;
import com.ssafy.mvc.service.InterviewRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interview-rooms")
@RequiredArgsConstructor
public class InterviewRoomController {

    private final InterviewRoomService interviewRoomService;
    private final InterviewScenarioDao interviewScenarioDao;

    // 내 면접 기록 조회
    @GetMapping
    public ResponseEntity<List<InterviewRoomDto>> getMyRooms(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        Long userId = userDetails.getUserDto().getUserId();
        return ResponseEntity.ok(interviewRoomService.getRoomsByUserId(userId));
    }

    // 면접 시나리오(질문·답변) 조회
    @GetMapping("/{roomId}/scenarios")
    public ResponseEntity<List<InterviewScenarioDto>> getScenarios(@PathVariable Long roomId) {
        return ResponseEntity.ok(interviewScenarioDao.selectByRoomId(roomId));
    }

    // 면접방 생성: 파일 업로드 대신 마이페이지에 등록된 이력서/포트폴리오를 resumeId, portfolioId로 선택
    @PostMapping
    public ResponseEntity<InterviewRoomDto> createRoom(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails,
            @RequestParam String companyName,
            @RequestParam Integer jobId,
            @RequestParam String difficulty,
            @RequestParam String applicantType,
            @RequestParam(required = false) Integer aiInterviewerCnt,
            @RequestParam Integer aiApplicantCnt,
            @RequestParam(required = false) Long resumeId,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) String language
    ) {

        InterviewRoomDto dto = new InterviewRoomDto();
        dto.setUserId(userDetails.getUserDto().getUserId());
        dto.setCompanyName(companyName);
        dto.setJobId(jobId);
        dto.setDifficulty(difficulty);
        dto.setApplicantType(applicantType);
        dto.setAiInterviewerCnt(aiInterviewerCnt != null ? aiInterviewerCnt : 3);
        dto.setAiApplicantCnt(aiApplicantCnt);
        dto.setResumeId(resumeId);
        dto.setPortfolioId(portfolioId);
        dto.setLanguage(language != null ? language : "KO");

        InterviewRoomDto result = interviewRoomService.createRoom(dto);
        return ResponseEntity.ok(result);
    }

    // 페르소나 선정 → AI 대본 생성 → 시나리오 저장 → 상태 IN_PROGRESS 전환
    @PostMapping("/{roomId}/start")
    public ResponseEntity<?> startInterview(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        try {
            Long userId = userDetails.getUserDto().getUserId();
            InterviewStartResultDto result = interviewRoomService.startInterview(roomId, userId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 면접 상태 업데이트 (COMPLETED / CANCELLED)
    @PatchMapping("/{roomId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long roomId, @RequestBody Map<String, String> body) {
        interviewRoomService.updateStatus(roomId, body.get("status"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 마이페이지 면접 기록 삭제 (DB는 deleted_at으로 비활성화)
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        interviewRoomService.deleteRoom(roomId, userDetails.getUserDto().getUserId());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
