package com.ssafy.mvc.service;

import java.util.List;

import com.ssafy.mvc.dto.UserDto;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto userLogin(String email, String password);

    void signup(UserDto user);

    UserDto getUserByEmail(String email);

    boolean existsByEmail(String email);

    void updateUser(UserDto user);

    void deleteUser(Long userId);

    boolean verifyPassword(UserDto user, String rawPassword);

    // 소셜 로그인: 이미 연동된 계정이면 그대로, 이메일이 같은 기존 계정이 있으면 연동만, 둘 다 없으면 신규 가입
    UserDto findOrCreateSocialUser(String provider, String providerAccountId,
                                    String email, String name, String profileImageUrl);
}