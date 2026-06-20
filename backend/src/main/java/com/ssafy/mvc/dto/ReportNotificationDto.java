package com.ssafy.mvc.dto;

// 리포트 열람 가능 알림 메일 발송 대상 조회용 (저장 없이 조회 결과만 담는 DTO)
public class ReportNotificationDto {
    private Long reportId;
    private String userEmail;
    private String userName;
    private String companyName;

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}
