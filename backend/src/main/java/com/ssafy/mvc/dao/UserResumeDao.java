package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.UserResumeDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserResumeDao {

    void insertResume(UserResumeDto dto);

    UserResumeDto selectById(Long resumeId);

    List<UserResumeDto> selectByUserId(Long userId);

    void deleteByIdAndUserId(@Param("resumeId") Long resumeId, @Param("userId") Long userId);

    // 회원탈퇴 시 해당 유저의 이력서를 전부 삭제 (GCS 원본 정리는 서비스 레이어에서 먼저 처리)
    void deleteByUserId(@Param("userId") Long userId);
}
