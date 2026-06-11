package com.ssafy.mvc.dto;

public class InterviewRoomPersonaDto {

    private Long roomPersonaId;
    private Long roomId;
    private String personaRole;
    private Long personaId;

    public Long getRoomPersonaId() { return roomPersonaId; }
    public void setRoomPersonaId(Long roomPersonaId) { this.roomPersonaId = roomPersonaId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getPersonaRole() { return personaRole; }
    public void setPersonaRole(String personaRole) { this.personaRole = personaRole; }

    public Long getPersonaId() { return personaId; }
    public void setPersonaId(Long personaId) { this.personaId = personaId; }
}
