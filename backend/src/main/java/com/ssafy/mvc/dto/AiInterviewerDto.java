package com.ssafy.mvc.dto;

public class AiInterviewerDto {

    private Long interviewerId;
    private String interviewerName;
    private String interviewerGender;
    private String profileImageUrl;
    private String moveVideoUrl;
    private String interviewerPrompt;
    private String voiceType;
    private String language; // KO, EN

    public Long getInterviewerId() { return interviewerId; }
    public void setInterviewerId(Long interviewerId) { this.interviewerId = interviewerId; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public String getInterviewerGender() { return interviewerGender; }
    public void setInterviewerGender(String interviewerGender) { this.interviewerGender = interviewerGender; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getMoveVideoUrl() { return moveVideoUrl; }
    public void setMoveVideoUrl(String moveVideoUrl) { this.moveVideoUrl = moveVideoUrl; }

    public String getInterviewerPrompt() { return interviewerPrompt; }
    public void setInterviewerPrompt(String interviewerPrompt) { this.interviewerPrompt = interviewerPrompt; }

    public String getVoiceType() { return voiceType; }
    public void setVoiceType(String voiceType) { this.voiceType = voiceType; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    @Override
    public String toString() {
        return "AiInterviewerDto [interviewerId=" + interviewerId + ", interviewerName=" + interviewerName
                + ", interviewerGender=" + interviewerGender + ", interviewerPrompt=" + interviewerPrompt + "]";
    }
}
