package com.ssafy.mvc.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

// 폼 로그인(UserDetails)과 소셜 로그인(OAuth2User/OidcUser) 양쪽 모두에서
// 동일한 타입으로 @AuthenticationPrincipal CustomUserDetailsDto를 쓸 수 있도록 두 인터페이스를 함께 구현
public class CustomUserDetailsDto implements UserDetails, OidcUser, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UserDto userDto;
    // 구글(OIDC) 로그인일 때만 채워짐. 폼 로그인/카카오 로그인이면 null
    private final transient OidcUser oidcDelegate;

    public CustomUserDetailsDto(UserDto userDto) {
        this(userDto, null);
    }

    public CustomUserDetailsDto(UserDto userDto, OidcUser oidcDelegate) {
        this.userDto = userDto;
        this.oidcDelegate = oidcDelegate;
    }

    public UserDto getUserDto() {
        return userDto;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return userDto.getUserPwd();
    }

    @Override
    public String getUsername() {
        return userDto.getUserEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // ── OAuth2User / OidcUser ──────────────────────────────
    @Override
    public Map<String, Object> getAttributes() {
        return oidcDelegate != null ? oidcDelegate.getAttributes() : Map.of();
    }

    @Override
    public String getName() {
        return String.valueOf(userDto.getUserId());
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcDelegate != null ? oidcDelegate.getClaims() : Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcDelegate != null ? oidcDelegate.getUserInfo() : null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcDelegate != null ? oidcDelegate.getIdToken() : null;
    }
}
