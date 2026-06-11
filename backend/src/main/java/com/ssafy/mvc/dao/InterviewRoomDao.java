package com.ssafy.mvc.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.mvc.dto.InterviewRoomDto;

@Mapper
public interface InterviewRoomDao {

    void insertInterviewRoom(InterviewRoomDto dto);

    InterviewRoomDto selectByRoomId(Long roomId);

    void updateStatus(@Param("roomId") Long roomId, @Param("status") String status);
}
