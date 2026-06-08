package com.ssafy.mvc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InterviewRoomCreateResponse {
    private Long roomId;
    private Long resumeId;
    private Long portfolioId;
    private String message;
}
