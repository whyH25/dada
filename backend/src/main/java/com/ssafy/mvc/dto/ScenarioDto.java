package com.ssafy.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public class ScenarioDto {
    private Long   scenarioId;
    private String questionType;
    private String questionText;
    private int    questionOrder;
    
	public Long getScenarioId() {
		return scenarioId;
	}
	public void setScenarioId(Long scenarioId) {
		this.scenarioId = scenarioId;
	}
	public String getQuestionType() {
		return questionType;
	}
	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}
	public String getQuestionText() {
		return questionText;
	}
	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}
	public int getQuestionOrder() {
		return questionOrder;
	}
	public void setQuestionOrder(int questionOrder) {
		this.questionOrder = questionOrder;
	}
	
	@Override
	public String toString() {
		return "ScenarioDto [scenarioId=" + scenarioId + ", questionType=" + questionType + ", questionText="
				+ questionText + ", questionOrder=" + questionOrder + "]";
	}
    
}
