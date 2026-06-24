package com.ssafy.mvc.dto;

import java.util.List;
import java.util.Map;

public class InterviewStartResultDto {

    private Long roomId;
    private String interviewTitle;
    private String companyName;
    private String jobRole;
    private String applicantType;
    private String difficulty;
    private List<InterviewScenarioDto> scenario;
    private List<Long> interviewerPersonaIds;  // 선정된 면접관 ID 순서 (타일 순서와 일치)
    private List<Long> applicantPersonaIds;    // 선정된 지원자 ID 순서 (타일 순서와 일치)
    private Map<Long, String> personaNames;          // applicantId → 이름
    private Map<Long, String> interviewerStopVideos; // interviewerId → stop 영상 URL
    private Map<Long, String> interviewerMoveVideos; // interviewerId → move 영상 URL
    private Map<Long, String> applicantStopVideos;   // applicantId → stop 영상 URL
    private Map<Long, String> applicantMoveVideos;   // applicantId → move 영상 URL

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

    public List<Long> getInterviewerPersonaIds() { return interviewerPersonaIds; }
    public void setInterviewerPersonaIds(List<Long> interviewerPersonaIds) { this.interviewerPersonaIds = interviewerPersonaIds; }

    public List<Long> getApplicantPersonaIds() { return applicantPersonaIds; }
    public void setApplicantPersonaIds(List<Long> applicantPersonaIds) { this.applicantPersonaIds = applicantPersonaIds; }

    public Map<Long, String> getPersonaNames() { return personaNames; }
    public void setPersonaNames(Map<Long, String> personaNames) { this.personaNames = personaNames; }

    public Map<Long, String> getInterviewerStopVideos() { return interviewerStopVideos; }
    public void setInterviewerStopVideos(Map<Long, String> interviewerStopVideos) { this.interviewerStopVideos = interviewerStopVideos; }

    public Map<Long, String> getInterviewerMoveVideos() { return interviewerMoveVideos; }
    public void setInterviewerMoveVideos(Map<Long, String> interviewerMoveVideos) { this.interviewerMoveVideos = interviewerMoveVideos; }

    public Map<Long, String> getApplicantStopVideos() { return applicantStopVideos; }
    public void setApplicantStopVideos(Map<Long, String> applicantStopVideos) { this.applicantStopVideos = applicantStopVideos; }

    public Map<Long, String> getApplicantMoveVideos() { return applicantMoveVideos; }
    public void setApplicantMoveVideos(Map<Long, String> applicantMoveVideos) { this.applicantMoveVideos = applicantMoveVideos; }
}
