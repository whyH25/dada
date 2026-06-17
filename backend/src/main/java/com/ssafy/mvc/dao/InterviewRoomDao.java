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
}
