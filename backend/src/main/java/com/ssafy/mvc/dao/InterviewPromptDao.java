package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.InterviewPromptDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewPromptDao {

    void insertPrompt(InterviewPromptDto dto);

    // 회원탈퇴 시 해당 유저의 면접방에 속한 프롬프트 기록을 일괄 비활성화
    void deactivateByRoomIds(@Param("roomIds") List<Long> roomIds);
}
