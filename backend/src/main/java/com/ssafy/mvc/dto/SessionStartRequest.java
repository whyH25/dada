package com.ssafy.mvc.dto;

import lombok.Data;

public class SessionStartRequest {
    private Long roomId;
    private Long userId;
    
	public Long getRoomId() {
		return roomId;
	}
	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "SessionStartRequest [roomId=" + roomId + ", userId=" + userId + "]";
	}
    
}
