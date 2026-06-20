package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.InterviewReportDto;
import com.ssafy.mvc.dto.ReportApplicantDto;
import com.ssafy.mvc.dto.ReportNotificationDto;
import com.ssafy.mvc.dto.ReportQuestionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportDao {
    void insertReport(InterviewReportDto dto);
    void insertApplicants(@Param("list") List<ReportApplicantDto> list);
    void insertQuestions(@Param("list") List<ReportQuestionDto> list);
    InterviewReportDto selectByRoomId(Long roomId);
    List<ReportApplicantDto> selectApplicantsByReportId(Long reportId);
    List<ReportQuestionDto> selectQuestionsByReportId(Long reportId);

    // 면접 종료 1일이 지나 열람 가능해졌지만 아직 알림 메일을 안 보낸 리포트 목록
    List<ReportNotificationDto> selectPendingNotifications();

    // 알림 메일 발송 시각 기록 (중복 발송 방지)
    void markNotified(@Param("reportId") Long reportId);
}
