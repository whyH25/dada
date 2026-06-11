package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.InterviewRoomPersonaDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewRoomPersonaDao {

    void insertRoomPersonas(@Param("list") List<InterviewRoomPersonaDto> list);
}
