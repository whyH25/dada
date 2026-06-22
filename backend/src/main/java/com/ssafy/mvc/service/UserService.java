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

    // 아이디/비밀번호 찾기: 가입된 이메일이면 임시 비밀번호를 발급해 DB 갱신 + 메일 발송, 미가입이면 예외
    void resetPasswordByEmail(String email);

    // 소셜 로그인: 이미 연동된 계정이면 그대로, 이메일이 같은 기존 계정이 있으면 연동만, 둘 다 없으면 신규 가입
    UserDto findOrCreateSocialUser(String provider, String providerAccountId,
                                    String email, String name, String profileImageUrl);
}