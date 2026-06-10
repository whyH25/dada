package com.ssafy.mvc.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public class InterviewRoomDto {
    private Long roomId;
    private Long userId;
    private String companyName;
    private Integer jobId;
    private Integer industryId;
    private String difficulty;
    private Integer aiInterviewerCnt;
    private Integer aiApplicantCnt;
    private Long resumeId;
    private Long portfolioId;
    private String status;
    private String prompt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
	public Long getRoomId() {
		return roomId;
	}
	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public Integer getJobId() {
		return jobId;
	}
	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}
	public Integer getIndustryId() {
		return industryId;
	}
	public void setIndustryId(Integer industryId) {
		this.industryId = industryId;
	}
	public String getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}
	public Integer getAiInterviewerCnt() {
		return aiInterviewerCnt;
	}
	public void setAiInterviewerCnt(Integer aiInterviewerCnt) {
		this.aiInterviewerCnt = aiInterviewerCnt;
	}
	public Integer getAiApplicantCnt() {
		return aiApplicantCnt;
	}
	public void setAiApplicantCnt(Integer aiApplicantCnt) {
		this.aiApplicantCnt = aiApplicantCnt;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPrompt() {
		return prompt;
	}
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	@Override
	public String toString() {
		return "InterviewRoomDto [roomId=" + roomId + ", userId=" + userId + ", companyName=" + companyName + ", jobId="
				+ jobId + ", industryId=" + industryId + ", difficulty=" + difficulty + ", aiInterviewerCnt="
				+ aiInterviewerCnt + ", aiApplicantCnt=" + aiApplicantCnt + ", resumeId=" + resumeId + ", portfolioId="
				+ portfolioId + ", status=" + status + ", prompt=" + prompt + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}
    
}
