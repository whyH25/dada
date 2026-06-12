package com.ssafy.mvc.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AdminDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long adminId;
    private String adminEmail;
    private String adminPwd;
    private String adminName;
    private LocalDateTime createdAt;

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminPwd() { return adminPwd; }
    public void setAdminPwd(String adminPwd) { this.adminPwd = adminPwd; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
