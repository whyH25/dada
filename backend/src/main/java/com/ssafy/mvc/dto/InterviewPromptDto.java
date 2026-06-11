package com.ssafy.mvc.dto;

public class InterviewPromptDto {

    private Long promptId;
    private Long roomId;
    private String promptType;
    private String promptText;
    private String responseText;

    public Long getPromptId() { return promptId; }
    public void setPromptId(Long promptId) { this.promptId = promptId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getPromptType() { return promptType; }
    public void setPromptType(String promptType) { this.promptType = promptType; }

    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { this.promptText = promptText; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }
}
