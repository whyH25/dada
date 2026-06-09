package com.ssafy.mvc.dto;

public class UserDto {
    private Long userId;
    private String userEmail;
    private String userPwd;
    private String userName;
    private String userPhone;
    private String userProfileImg;
    private String userStatus;

    public Long getUserIdx() {
        return userId;
    }

    public void setUserIdx(Long userIdx) {
        this.userId = userIdx;
    }

    // 프론트에서 userId로 접근하는 경우 호환
    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getUserPwd() {
		return userPwd;
	}

	public void setUserPwd(String userPwd) {
		this.userPwd = userPwd;
	}

	public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserProfileImg() {
        return userProfileImg;
    }

    public void setUserProfileImg(String userProfileImg) {
        this.userProfileImg = userProfileImg;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}
