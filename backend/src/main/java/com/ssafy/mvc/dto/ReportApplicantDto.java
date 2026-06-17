package com.ssafy.mvc.dto;

public class ReportApplicantDto {
    private Long id;
    private Long reportId;
    private Long roomId;
    private Boolean isUser;
    private Long applicantId; // AI 지원자 ID (is_user=0일 때)
    private Integer score;
    private String strength;
    private String weakness;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Boolean getIsUser() { return isUser; }
    public void setIsUser(Boolean isUser) { this.isUser = isUser; }

    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public String getWeakness() { return weakness; }
    public void setWeakness(String weakness) { this.weakness = weakness; }
}
