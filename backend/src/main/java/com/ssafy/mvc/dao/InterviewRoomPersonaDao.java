package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.InterviewRoomPersonaDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewRoomPersonaDao {

    void insertRoomPersonas(@Param("list") List<InterviewRoomPersonaDto> list);

    // 회원탈퇴 시 해당 유저의 면접방에 속한 페르소나 매핑을 일괄 비활성화
    void deactivateByRoomIds(@Param("roomIds") List<Long> roomIds);
}
