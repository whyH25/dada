package com.ssafy.mvc.dto;

public class AiApplicantDto {

    private Long applicantId;
    private String applicantName;
    private String applicantGender;
    private String applicantType;
    private String profileImageUrl; // 추후 사용
    private String applicantPrompt;
    private String voiceType;

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

    public String getApplicantPrompt() { return applicantPrompt; }
    public void setApplicantPrompt(String applicantPrompt) { this.applicantPrompt = applicantPrompt; }

    public String getVoiceType() { return voiceType; }
    public void setVoiceType(String voiceType) { this.voiceType = voiceType; }

    @Override
    public String toString() {
        return "AiApplicantDto [applicantId=" + applicantId + ", applicantName=" + applicantName
                + ", applicantType=" + applicantType + ", applicantPrompt=" + applicantPrompt + "]";
    }
}
