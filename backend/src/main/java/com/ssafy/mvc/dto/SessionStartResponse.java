package com.ssafy.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SessionStartResponse {
    private Long             sessionId;
    private List<ScenarioDto> scenarios;
}
