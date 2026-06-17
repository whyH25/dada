package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.InterviewReportDto;
import com.ssafy.mvc.dto.ReportApplicantDto;
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
}
