package com.ssafy.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
public class SessionStartResponse {
    private Long sessionId;
    private List<ScenarioDto> scenarios;
    
	public Long getSessionId() {
		return sessionId;
	}
	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}
	public List<ScenarioDto> getScenarios() {
		return scenarios;
	}
	public void setScenarios(List<ScenarioDto> scenarios) {
		this.scenarios = scenarios;
	}
	
	@Override
	public String toString() {
		return "SessionStartResponse [sessionId=" + sessionId + ", scenarios=" + scenarios + "]";
	}
	
}
