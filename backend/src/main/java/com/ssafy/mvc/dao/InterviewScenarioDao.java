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

    // STT로 받은 답변 텍스트(없으면 null)와 답변 완료까지 걸린 시간(초)을 함께 저장
    void updateAnswer(@Param("scenarioId") Long scenarioId,
                       @Param("answerText") String answerText,
                       @Param("answerSec") Integer answerSec);
}
