package com.ssafy.mvc.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewRoomDto {
    private Long roomId;
    private Long userId;

    private String jobName;
    private String applicantType;
    private String companyName;
    private Integer jobId;
    private String difficulty;
    private String language; // KO, EN - 면접 진행 언어

    private Integer aiInterviewerCnt;
    private Integer aiApplicantCnt;

    private Long resumeId;
    private String resumeFileName;
    private String resumeText;
    private Long portfolioId;
    private String portfolioFileName;
    private String portfolioText;

    private String status;
    private String userName;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer overallScore; // interview_report 조인값 (목록 조회 시)
    
    
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
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getApplicantType() {
		return applicantType;
	}
	public void setApplicantType(String applicantType) {
		this.applicantType = applicantType;
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
	public String getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
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
	public String getResumeFileName() {
		return resumeFileName;
	}
	public void setResumeFileName(String resumeFileName) {
		this.resumeFileName = resumeFileName;
	}
	public String getResumeText() {
		return resumeText;
	}
	public void setResumeText(String resumeText) {
		this.resumeText = resumeText;
	}
	public Long getPortfolioId() {
		return portfolioId;
	}
	public void setPortfolioId(Long portfolioId) {
		this.portfolioId = portfolioId;
	}
	public String getPortfolioFileName() {
		return portfolioFileName;
	}
	public void setPortfolioFileName(String portfolioFileName) {
		this.portfolioFileName = portfolioFileName;
	}
	public String getPortfolioText() {
		return portfolioText;
	}
	public void setPortfolioText(String portfolioText) {
		this.portfolioText = portfolioText;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public LocalDateTime getStartedAt() {
		return startedAt;
	}
	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}
	public LocalDateTime getEndedAt() {
		return endedAt;
	}
	public void setEndedAt(LocalDateTime endedAt) {
		this.endedAt = endedAt;
	}
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Integer getOverallScore() {
		return overallScore;
	}
	public void setOverallScore(Integer overallScore) {
		this.overallScore = overallScore;
	}
	
	@Override
	public String toString() {
		return "InterviewRoomDto [roomId=" + roomId + ", userId=" + userId + ", applicantType=" + applicantType
				+ ", companyName=" + companyName + ", jobId=" + jobId + ", difficulty=" + difficulty
				+ ", aiInterviewerCnt=" + aiInterviewerCnt + ", aiApplicantCnt=" + aiApplicantCnt + ", resumeId="
				+ resumeId + ", resumeFileName=" + resumeFileName + ", resumeText=" + resumeText + ", portfolioId="
				+ portfolioId + ", portfolioFileName=" + portfolioFileName + ", portfolioText=" + portfolioText
				+ ", status=" + status + ", startedAt=" + startedAt + ", endedAt=" + endedAt + "]";
	}
	
    
}
