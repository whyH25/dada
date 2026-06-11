package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.InterviewPromptDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewPromptDao {

    void insertPrompt(InterviewPromptDto dto);
}
