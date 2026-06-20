package com.ssafy.mvc.dto;

// users_social 테이블 매핑 (소셜 로그인 계정 연동 정보)
public class UserSocialDto {
    private Long socialId;
    private Long userId;
    private String provider;          // GOOGLE, KAKAO, NAVER
    private String providerAccountId;
    private String createdAt;

    public Long getSocialId() { return socialId; }
    public void setSocialId(Long socialId) { this.socialId = socialId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderAccountId() { return providerAccountId; }
    public void setProviderAccountId(String providerAccountId) { this.providerAccountId = providerAccountId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
