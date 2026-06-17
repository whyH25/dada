package com.ssafy.mvc.dto;

import java.time.LocalDateTime;

public class InterviewReportDto {
    private Long reportId;
    private Long roomId;
    private Integer overallScore;
    private String aiComment;
    private Integer compExpertise;
    private String compExpertiseDetail;
    private Integer compLogic;
    private String compLogicDetail;
    private Integer compCommu;
    private String compCommuDetail;
    private Integer compCulture;
    private String compCultureDetail;
    private Integer compPressure;
    private String compPressureDetail;
    private Integer speechWpm;
    private Integer speechFiller;
    private LocalDateTime createdAt;

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public String getAiComment() { return aiComment; }
    public void setAiComment(String aiComment) { this.aiComment = aiComment; }

    public Integer getCompExpertise() { return compExpertise; }
    public void setCompExpertise(Integer compExpertise) { this.compExpertise = compExpertise; }

    public String getCompExpertiseDetail() { return compExpertiseDetail; }
    public void setCompExpertiseDetail(String compExpertiseDetail) { this.compExpertiseDetail = compExpertiseDetail; }

    public Integer getCompLogic() { return compLogic; }
    public void setCompLogic(Integer compLogic) { this.compLogic = compLogic; }

    public String getCompLogicDetail() { return compLogicDetail; }
    public void setCompLogicDetail(String compLogicDetail) { this.compLogicDetail = compLogicDetail; }

    public Integer getCompCommu() { return compCommu; }
    public void setCompCommu(Integer compCommu) { this.compCommu = compCommu; }

    public String getCompCommuDetail() { return compCommuDetail; }
    public void setCompCommuDetail(String compCommuDetail) { this.compCommuDetail = compCommuDetail; }

    public Integer getCompCulture() { return compCulture; }
    public void setCompCulture(Integer compCulture) { this.compCulture = compCulture; }

    public String getCompCultureDetail() { return compCultureDetail; }
    public void setCompCultureDetail(String compCultureDetail) { this.compCultureDetail = compCultureDetail; }

    public Integer getCompPressure() { return compPressure; }
    public void setCompPressure(Integer compPressure) { this.compPressure = compPressure; }

    public String getCompPressureDetail() { return compPressureDetail; }
    public void setCompPressureDetail(String compPressureDetail) { this.compPressureDetail = compPressureDetail; }

    public Integer getSpeechWpm() { return speechWpm; }
    public void setSpeechWpm(Integer speechWpm) { this.speechWpm = speechWpm; }

    public Integer getSpeechFiller() { return speechFiller; }
    public void setSpeechFiller(Integer speechFiller) { this.speechFiller = speechFiller; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
