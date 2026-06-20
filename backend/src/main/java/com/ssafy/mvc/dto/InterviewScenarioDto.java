package com.ssafy.mvc.dto;

public class InterviewScenarioDto {

    private Long scenarioId;
    private Long roomId;
    private String scenarioType;
    private Long parentScenarioId;

    private Integer questionSeq;
    private Integer turnOrder;
    private String turnRole;
    private Long turnRefId;

    private String speechText;
    private String answerText;
    private Integer answerSec;
    private Integer timeoutSec;
    private String voiceType; // TTS 조회 시 JOIN으로 채워짐 (DB 컬럼 없음)

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getScenarioType() { return scenarioType; }
    public void setScenarioType(String scenarioType) { this.scenarioType = scenarioType; }

    public Long getParentScenarioId() { return parentScenarioId; }
    public void setParentScenarioId(Long parentScenarioId) { this.parentScenarioId = parentScenarioId; }

    public Integer getQuestionSeq() { return questionSeq; }
    public void setQuestionSeq(Integer questionSeq) { this.questionSeq = questionSeq; }

    public Integer getTurnOrder() { return turnOrder; }
    public void setTurnOrder(Integer turnOrder) { this.turnOrder = turnOrder; }

    public String getTurnRole() { return turnRole; }
    public void setTurnRole(String turnRole) { this.turnRole = turnRole; }

    public Long getTurnRefId() { return turnRefId; }
    public void setTurnRefId(Long turnRefId) { this.turnRefId = turnRefId; }

    public String getSpeechText() { return speechText; }
    public void setSpeechText(String speechText) { this.speechText = speechText; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public Integer getAnswerSec() { return answerSec; }
    public void setAnswerSec(Integer answerSec) { this.answerSec = answerSec; }

    public Integer getTimeoutSec() { return timeoutSec; }
    public void setTimeoutSec(Integer timeoutSec) { this.timeoutSec = timeoutSec; }

    public String getVoiceType() { return voiceType; }
    public void setVoiceType(String voiceType) { this.voiceType = voiceType; }
}
