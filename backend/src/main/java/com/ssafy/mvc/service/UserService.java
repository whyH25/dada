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

    // 구글 로그인 성공 핸들러에서 OAuth2AuthorizedClient의 refresh token을 받았을 때 저장 (회원탈퇴 시 토큰 해지용)
    void saveGoogleRefreshToken(Long userId, String refreshToken);

    // 회원탈퇴 시 로그인 세션 종류와 무관하게 토큰을 해지할 수 있도록, DB에 저장된 구글 refresh token 조회
    String getGoogleRefreshToken(Long userId);
}