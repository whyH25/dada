package com.ssafy.mvc.dto;

import lombok.Builder;
import lombok.Data;

@Builder
public class InterviewRoomCreateResponse {
    private Long roomId;
    private Long resumeId;
    private Long portfolioId;
    private String message;
    
	public Long getRoomId() {
		return roomId;
	}
	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}
	public Long getResumeId() {
		return resumeId;
	}
	public void setResumeId(Long resumeId) {
		this.resumeId = resumeId;
	}
	public Long getPortfolioId() {
		return portfolioId;
	}
	public void setPortfolioId(Long portfolioId) {
		this.portfolioId = portfolioId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "InterviewRoomCreateResponse [roomId=" + roomId + ", resumeId=" + resumeId + ", portfolioId="
				+ portfolioId + ", message=" + message + "]";
	}
    
}
