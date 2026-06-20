package com.ssafy.mvc.service;

import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

// 카카오 등 OIDC가 아닌 일반 OAuth2 제공자 로그인 처리
// 카카오 보류 중이라 @Service를 빼둠 — 빈으로 등록돼 있으면 Spring Security가
// 다른 비-OIDC 제공자의 기본 userService로 자동 채택해버릴 수 있음
// 재추가 시 @Service 복원 + SecurityConfig에서 명시적으로 .userService(...) 연결
@RequiredArgsConstructor
public class CustomUserOAuth2Service extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerAccountId = String.valueOf(attributes.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;

        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String name = profile != null ? (String) profile.get("nickname") : null;
        String profileImageUrl = profile != null ? (String) profile.get("profile_image_url") : null;

        try {
            UserDto user = userService.findOrCreateSocialUser(provider, providerAccountId, email, name, profileImageUrl);
            return new CustomUserDetailsDto(user);
        } catch (IllegalArgumentException e) {
            // 이메일 미동의 등 가입 불가 사유를 OAuth2 표준 예외로 변환 → failureUrl로 메시지 전달
            throw new OAuth2AuthenticationException(new OAuth2Error("social_signup_failed", e.getMessage(), null));
        }
    }
}
