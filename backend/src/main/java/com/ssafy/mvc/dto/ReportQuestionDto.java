package com.ssafy.mvc.dto;

public class ReportQuestionDto {
    private Long id;
    private Long roomId;         // report_question.room_id FK
    private Long scenarioId;     // USER 또는 APPLICANT 턴의 scenario_id
    private Integer questionSeq;
    // JOIN으로 채워지는 필드 (DB 컬럼 아님)
    private String questionText; // INTERVIEWER 턴 speech_text
    private String answerText;   // USER/APPLICANT 턴 answer_text 또는 speech_text
    private String turnRole;     // interview_scenario.turn_role
    private Long turnRefId;      // interview_scenario.turn_ref_id (APPLICANT의 persona id)
    private Integer score;
    private String label;
    private String feedback;
    private String tags;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }

    public Integer getQuestionSeq() { return questionSeq; }
    public void setQuestionSeq(Integer questionSeq) { this.questionSeq = questionSeq; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public String getTurnRole() { return turnRole; }
    public void setTurnRole(String turnRole) { this.turnRole = turnRole; }

    public Long getTurnRefId() { return turnRefId; }
    public void setTurnRefId(Long turnRefId) { this.turnRefId = turnRefId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
