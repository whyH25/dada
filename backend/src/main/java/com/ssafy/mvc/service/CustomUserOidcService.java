package com.ssafy.mvc.service;

import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

// 구글(OIDC) 로그인 처리
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserOidcService extends OidcUserService {

    private final UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String providerAccountId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String profileImageUrl = oidcUser.getPicture();

        try {
            UserDto user = userService.findOrCreateSocialUser("GOOGLE", providerAccountId, email, name, profileImageUrl);
            return new CustomUserDetailsDto(user, oidcUser);
        } catch (Exception e) {
            log.error("구글 소셜 로그인 처리 실패: {}", e.getMessage());
            throw new OAuth2AuthenticationException(new OAuth2Error("social_signup_failed", e.getMessage(), null));
        }
    }
}
