package com.ssafy.mvc.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetailsDto implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UserDto userDto;

    public CustomUserDetailsDto(UserDto userDto) {
        this.userDto = userDto;
    }

    public UserDto getUserDto() {
        return userDto;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = userDto.getUserRole() != null ? userDto.getUserRole() : "USER";
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
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
}
