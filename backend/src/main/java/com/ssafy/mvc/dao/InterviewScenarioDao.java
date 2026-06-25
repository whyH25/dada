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

    // 회원탈퇴 시 해당 유저의 면접방에 속한 시나리오를 일괄 비활성화
    void deactivateByRoomIds(@Param("roomIds") List<Long> roomIds);

    // 리포트 생성 시 특정 턴의 scenario_id 조회 (USER: turnRefId=null, APPLICANT: turnRefId=personaId)
    Long selectScenarioId(@Param("roomId") Long roomId,
                          @Param("questionSeq") int questionSeq,
                          @Param("turnRole") String turnRole,
                          @Param("turnRefId") Long turnRefId);
}
