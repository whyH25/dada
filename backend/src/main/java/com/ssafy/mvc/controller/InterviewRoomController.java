package com.ssafy.mvc.controller;

import com.ssafy.mvc.dto.InterviewRoomDto;
import com.ssafy.mvc.service.InterviewRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/interview-rooms")
@RequiredArgsConstructor
public class InterviewRoomController {

    private final InterviewRoomService interviewRoomService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InterviewRoomDto> createRoom(
            @RequestParam Long userId,
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
        dto.setUserId(userId);
        dto.setCompanyName(companyName);
        dto.setJobId(jobId);
        dto.setDifficulty(difficulty);
        dto.setApplicantType(applicantType);
        dto.setAiInterviewerCnt(aiInterviewerCnt != null ? aiInterviewerCnt : 3);
        dto.setAiApplicantCnt(aiApplicantCnt);

        InterviewRoomDto result = interviewRoomService.createRoom(dto, resumeFile, portfolioFile);
        return ResponseEntity.ok(result);
    }
}
