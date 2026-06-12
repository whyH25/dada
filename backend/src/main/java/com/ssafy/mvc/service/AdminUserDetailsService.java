package com.ssafy.mvc.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ssafy.mvc.dao.AdminDao;
import com.ssafy.mvc.dto.AdminDto;
import com.ssafy.mvc.dto.AdminUserDetailsDto;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminDao adminDao;

    public AdminUserDetailsService(AdminDao adminDao) {
        this.adminDao = adminDao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AdminDto admin = adminDao.selectByEmail(email);
        if (admin == null) {
            throw new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + email);
        }
        return new AdminUserDetailsDto(admin);
    }
}
