package com.ssafy.mvc.controller;

import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.InterviewRoomDto;
import com.ssafy.mvc.dto.InterviewStartResultDto;
import com.ssafy.mvc.service.InterviewRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/interview-rooms")
@RequiredArgsConstructor
public class InterviewRoomController {

    private final InterviewRoomService interviewRoomService;

    // 면접방 생성 및 서류 텍스트 추출 후 DB 저장
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InterviewRoomDto> createRoom(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails,
            @RequestParam String companyName,
            @RequestParam Integer jobId,
            @RequestParam String difficulty,
            @RequestParam String applicantType,
            @RequestParam(required = false) Integer aiInterviewerCnt,
            @RequestParam Integer aiApplicantCnt,
            @RequestParam MultipartFile resumeFile,
            @RequestParam(required = false) MultipartFile portfolioFile
    ) throws IOException {

        InterviewRoomDto dto = new InterviewRoomDto();
        dto.setUserId(userDetails.getUserDto().getUserId());
        dto.setCompanyName(companyName);
        dto.setJobId(jobId);
        dto.setDifficulty(difficulty);
        dto.setApplicantType(applicantType);
        dto.setAiInterviewerCnt(aiInterviewerCnt != null ? aiInterviewerCnt : 3);
        dto.setAiApplicantCnt(aiApplicantCnt);

        InterviewRoomDto result = interviewRoomService.createRoom(dto, resumeFile, portfolioFile);
        return ResponseEntity.ok(result);
    }

    // 페르소나 선정 → AI 대본 생성 → 시나리오 저장 → 상태 IN_PROGRESS 전환
    @PostMapping("/{roomId}/start")
    public ResponseEntity<InterviewStartResultDto> startInterview(@PathVariable Long roomId) {
        InterviewStartResultDto result = interviewRoomService.startInterview(roomId);
        return ResponseEntity.ok(result);
    }
}
