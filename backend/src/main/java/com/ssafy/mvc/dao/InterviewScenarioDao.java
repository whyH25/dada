package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.InterviewScenarioDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewScenarioDao {

    void insertScenarios(@Param("list") List<InterviewScenarioDto> list);

    List<InterviewScenarioDto> selectByRoomId(Long roomId);

    InterviewScenarioDto selectForTts(Long scenarioId);

    void updateAnswerText(@Param("scenarioId") Long scenarioId, @Param("answerText") String answerText);
}
