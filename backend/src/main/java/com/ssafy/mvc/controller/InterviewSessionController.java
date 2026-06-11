package com.ssafy.mvc.controller;

import com.ssafy.mvc.dto.ApiResponse;
import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.SessionStartRequest;
import com.ssafy.mvc.dto.SessionStartResponse;
import com.ssafy.mvc.service.InterviewSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

    @PostMapping("/start")
    public ApiResponse<SessionStartResponse> startSession(
            @RequestBody SessionStartRequest request,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails
    ) {
        Long userId = userDetails.getUserDto().getUserId();
        SessionStartResponse response = interviewSessionService.startSession(request.getRoomId(), userId);
        return new ApiResponse<>(true, "면접 세션이 시작되었습니다.", response);
    }
}
