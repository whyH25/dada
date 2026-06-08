package com.ssafy.mvc.dto;

import lombok.Data;

@Data
public class SessionStartRequest {
    private Long roomId;
    private Long userId;
}
