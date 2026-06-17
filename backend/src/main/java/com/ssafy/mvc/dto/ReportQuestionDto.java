package com.ssafy.mvc.dto;

public class ReportQuestionDto {
    private Long id;
    private Long reportId;
    private Long roomId;
    private Integer questionSeq;
    private String questionText;
    private String answerText;
    private Integer score;
    private String label;
    private String feedback;
    private String tags;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Integer getQuestionSeq() { return questionSeq; }
    public void setQuestionSeq(Integer questionSeq) { this.questionSeq = questionSeq; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
