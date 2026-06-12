package com.ssafy.mvc.dto;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AdminUserDetailsDto implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final AdminDto adminDto;

    public AdminUserDetailsDto(AdminDto adminDto) {
        this.adminDto = adminDto;
    }

    public AdminDto getAdminDto() {
        return adminDto;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public String getPassword() { return adminDto.getAdminPwd(); }

    @Override
    public String getUsername() { return adminDto.getAdminEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
