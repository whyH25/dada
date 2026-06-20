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
}
