package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.InterviewRoomDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewRoomDao {

    void insertInterviewRoom(InterviewRoomDto dto);
}
