package com.ssafy.mvc.controller;

import com.ssafy.mvc.dto.InterviewRoomCreateResponse;
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
    public ResponseEntity<InterviewRoomCreateResponse> createRoom(
            @RequestParam Long userId,
            @RequestParam String companyName,
            @RequestParam(required = false) Integer jobId,
            @RequestParam String difficulty,
            @RequestParam(defaultValue = "1") Integer interviewerCount,
            @RequestParam(defaultValue = "0") Integer aiApplicantCount,
            @RequestPart(required = false) MultipartFile resumeFile,
            @RequestPart(required = false) MultipartFile portfolioFile
    ) throws IOException {

        InterviewRoomCreateResponse response = interviewRoomService.createRoom(
                userId, companyName, jobId, difficulty,
                interviewerCount, aiApplicantCount,
                resumeFile, portfolioFile
        );
        return ResponseEntity.ok(response);
    }
}
