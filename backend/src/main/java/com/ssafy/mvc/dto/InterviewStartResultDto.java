package com.ssafy.mvc.dto;

import java.util.List;

public class InterviewStartResultDto {

    private Long roomId;
    private String interviewTitle;
    private String companyName;
    private String jobRole;
    private String applicantType;
    private String difficulty;
    private List<InterviewScenarioDto> scenario;

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getInterviewTitle() { return interviewTitle; }
    public void setInterviewTitle(String interviewTitle) { this.interviewTitle = interviewTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }

    public String getApplicantType() { return applicantType; }
    public void setApplicantType(String applicantType) { this.applicantType = applicantType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public List<InterviewScenarioDto> getScenario() { return scenario; }
    public void setScenario(List<InterviewScenarioDto> scenario) { this.scenario = scenario; }
}
