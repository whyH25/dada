package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.AiApplicantDto;
import com.ssafy.mvc.dto.AiInterviewerDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiPersonaDao {

    List<AiInterviewerDto> selectRandomInterviewers(@Param("count") int count);

    List<AiApplicantDto> selectRandomApplicants(@Param("count") int count, @Param("applicantType") String applicantType);
}
