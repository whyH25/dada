package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.UserSocialDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserSocialDao {

    // provider + provider_account_id로 연동된 계정 조회 (이미 가입한 소셜 로그인인지 확인)
    UserSocialDto selectByProviderAndAccountId(@Param("provider") String provider,
                                                @Param("providerAccountId") String providerAccountId);

    void insertSocial(UserSocialDto dto);

    // 구글 로그인 성공 시점에만 refresh_token이 내려오므로, 받았을 때만 갱신
    void updateRefreshToken(@Param("userId") Long userId,
                             @Param("provider") String provider,
                             @Param("refreshToken") String refreshToken);

    // 회원탈퇴 시 토큰 해지용 - 로그인 세션 종류와 무관하게 사용 가능하도록 DB에 저장된 값을 조회
    String selectRefreshTokenByUserId(@Param("userId") Long userId, @Param("provider") String provider);

    // 회원탈퇴 시 해당 유저의 소셜 연동 기록을 전부 삭제
    void deleteByUserId(@Param("userId") Long userId);
}
