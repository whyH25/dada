package com.ssafy.mvc.dto;

public class AiInterviewerDto {

    private Long interviewerId;
    private String interviewerName;
    private String interviewerGender;
    private String profileImageUrl; // 추후 사용
    private String interviewerPrompt;

    public Long getInterviewerId() { return interviewerId; }
    public void setInterviewerId(Long interviewerId) { this.interviewerId = interviewerId; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public String getInterviewerGender() { return interviewerGender; }
    public void setInterviewerGender(String interviewerGender) { this.interviewerGender = interviewerGender; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getInterviewerPrompt() { return interviewerPrompt; }
    public void setInterviewerPrompt(String interviewerPrompt) { this.interviewerPrompt = interviewerPrompt; }

    @Override
    public String toString() {
        return "AiInterviewerDto [interviewerId=" + interviewerId + ", interviewerName=" + interviewerName
                + ", interviewerGender=" + interviewerGender + ", interviewerPrompt=" + interviewerPrompt + "]";
    }
}
