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
}
