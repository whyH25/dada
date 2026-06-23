package com.ssafy.mvc.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.mvc.dto.InterviewRoomDto;

import java.util.List;

@Mapper
public interface InterviewRoomDao {

    void insertInterviewRoom(InterviewRoomDto dto);

    InterviewRoomDto selectByRoomId(Long roomId);

    List<InterviewRoomDto> selectByUserId(Long userId);

    void updateStatus(@Param("roomId") Long roomId, @Param("status") String status);

    // 회원탈퇴 시 자식 테이블 일괄 비활성화에 쓸 room_id 목록
    List<Long> selectRoomIdsByUserId(Long userId);

    // 회원탈퇴 시 해당 유저의 면접방을 비활성화
    void deactivateByUserId(Long userId);

    // 마이페이지에서 면접 기록 1건 삭제 시 비활성화
    void deactivateByRoomId(@Param("roomId") Long roomId);
}
