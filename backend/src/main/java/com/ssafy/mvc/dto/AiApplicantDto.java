package com.ssafy.mvc.dto;

public class AiApplicantDto {

    private Long applicantId;
    private String applicantName;
    private String applicantGender;
    private String applicantType;
    private String profileImageUrl;
    private String moveVideoUrl;
    private String applicantPrompt;
    private String voiceType;
    private String language; // KO, EN

    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicantGender() { return applicantGender; }
    public void setApplicantGender(String applicantGender) { this.applicantGender = applicantGender; }

    public String getApplicantType() { return applicantType; }
    public void setApplicantType(String applicantType) { this.applicantType = applicantType; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getMoveVideoUrl() { return moveVideoUrl; }
    public void setMoveVideoUrl(String moveVideoUrl) { this.moveVideoUrl = moveVideoUrl; }

    public String getApplicantPrompt() { return applicantPrompt; }
    public void setApplicantPrompt(String applicantPrompt) { this.applicantPrompt = applicantPrompt; }

    public String getVoiceType() { return voiceType; }
    public void setVoiceType(String voiceType) { this.voiceType = voiceType; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    @Override
    public String toString() {
        return "AiApplicantDto [applicantId=" + applicantId + ", applicantName=" + applicantName
                + ", applicantType=" + applicantType + ", applicantPrompt=" + applicantPrompt + "]";
    }
}
