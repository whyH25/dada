package com.ssafy.mvc.dto;

import java.time.LocalDateTime;

public class UserPortfolioDto {
    private Long portfolioId;
    private Long userId;
    private String fileUrl;
    private String fileName;
    private String parsedText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getParsedText() { return parsedText; }
    public void setParsedText(String parsedText) { this.parsedText = parsedText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
