package com.ssafy.mvc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssafy.mvc.dao.AiPersonaDao;
import com.ssafy.mvc.dao.InterviewRoomDao;
import com.ssafy.mvc.dao.InterviewScenarioDao;
import com.ssafy.mvc.dao.ReportDao;
import com.ssafy.mvc.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AiPromptService aiPromptService;
    private final InterviewRoomDao interviewRoomDao;
    private final InterviewScenarioDao interviewScenarioDao;
    private final AiPersonaDao aiPersonaDao;
    private final ReportDao reportDao;

    // 면접 시나리오 분석 → AI 평가 → 3개 리포트 테이블 저장
    @Transactional
    public Long generateReport(Long roomId) {
        if (reportDao.selectByRoomId(roomId) != null) {
            throw new IllegalStateException("이미 리포트가 생성되어 있습니다.");
        }

        InterviewRoomDto room = interviewRoomDao.selectByRoomId(roomId);
        List<InterviewScenarioDto> scenarios = interviewScenarioDao.selectByRoomId(roomId);

        // APPLICANT 페르소나 이름 조회 (turnRefId = applicant_id)
        List<Long> applicantIds = scenarios.stream()
                .filter(s -> "APPLICANT".equals(s.getTurnRole()) && s.getTurnRefId() != null)
                .map(InterviewScenarioDto::getTurnRefId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> applicantNames = new LinkedHashMap<>();
        if (!applicantIds.isEmpty()) {
            List<AiApplicantDto> applicants = aiPersonaDao.selectApplicantsByIds(applicantIds);
            for (AiApplicantDto a : applicants) {
                applicantNames.put(a.getApplicantId(), a.getApplicantName());
            }
        }

        // 프롬프트 생성/호출/기록은 AiPromptService가 담당 (면접방 생성 시 대본 생성과 동일한 구조)
        JsonNode json = aiPromptService.generateReportAnalysis(room, scenarios, applicantNames);
        return parseAndSave(roomId, json, applicantNames);
    }

    // 전체 리포트 조회 (마이페이지 탭 데이터)
    // applicant_id로 AI 지원자 이름 조회 후 응답에 name 필드 추가
    public Map<String, Object> getFullReport(Long roomId) {
        InterviewReportDto report = reportDao.selectByRoomId(roomId);
        if (report == null) return null;

        List<ReportApplicantDto> rawApplicants = reportDao.selectApplicantsByReportId(report.getReportId());

        // AI 지원자 이름 일괄 조회
        List<Long> aiIds = rawApplicants.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsUser()) && a.getApplicantId() != null)
                .map(ReportApplicantDto::getApplicantId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> nameMap = new LinkedHashMap<>();
        if (!aiIds.isEmpty()) {
            aiPersonaDao.selectApplicantsByIds(aiIds)
                        .forEach(a -> nameMap.put(a.getApplicantId(), a.getApplicantName()));
        }

        // name 필드를 포함한 응답 객체로 변환
        List<Map<String, Object>> applicants = rawApplicants.stream().map(a -> {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", a.getId());
            view.put("reportId", a.getReportId());
            view.put("isUser", a.getIsUser());
            view.put("applicantId", a.getApplicantId());
            view.put("name", Boolean.TRUE.equals(a.getIsUser())
                    ? "나"
                    : nameMap.getOrDefault(a.getApplicantId(), "AI 지원자"));
            view.put("score", a.getScore());
            view.put("strength", a.getStrength());
            view.put("weakness", a.getWeakness());
            return view;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("report", report);
        result.put("applicants", applicants);
        result.put("questions", reportDao.selectQuestionsByReportId(report.getReportId()));
        return result;
    }

    private Long parseAndSave(Long roomId, JsonNode json, Map<Long, String> applicantNames) {
        try {
            // 1. interview_report 저장
            InterviewReportDto report = new InterviewReportDto();
            report.setRoomId(roomId);
            report.setOverallScore(asIntOrNull(json, "overallScore"));
            report.setAiComment(textOrNull(json, "aiComment"));
            report.setCompExpertise(asIntOrNull(json, "compExpertise"));
            report.setCompExpertiseDetail(textOrNull(json, "compExpertiseDetail"));
            report.setCompLogic(asIntOrNull(json, "compLogic"));
            report.setCompLogicDetail(textOrNull(json, "compLogicDetail"));
            report.setCompCommu(asIntOrNull(json, "compCommu"));
            report.setCompCommuDetail(textOrNull(json, "compCommuDetail"));
            report.setCompCulture(asIntOrNull(json, "compCulture"));
            report.setCompCultureDetail(textOrNull(json, "compCultureDetail"));
            report.setCompPressure(asIntOrNull(json, "compPressure"));
            report.setCompPressureDetail(textOrNull(json, "compPressureDetail"));
            report.setSpeechWpm(asIntOrNull(json, "speechWpm"));
            report.setSpeechFiller(asIntOrNull(json, "speechFiller"));
            reportDao.insertReport(report);
            Long reportId = report.getReportId();

            // 2. report_applicant 저장 (name 제외 — 조회 시 JOIN으로 파생)
            List<ReportApplicantDto> applicants = new ArrayList<>();
            for (JsonNode a : json.path("applicants")) {
                ReportApplicantDto dto = new ReportApplicantDto();
                dto.setReportId(reportId);
                dto.setRoomId(roomId);
                dto.setIsUser(a.path("isUser").asBoolean(false));
                dto.setApplicantId(a.path("personaId").isNull() ? null : a.path("personaId").asLong());
                dto.setScore(asIntOrNull(a, "score"));
                dto.setStrength(textOrNull(a, "strength"));
                dto.setWeakness(textOrNull(a, "weakness"));
                applicants.add(dto);
            }
            if (!applicants.isEmpty()) {
                reportDao.insertApplicants(applicants);
            }

            // 3. report_question 저장
            List<ReportQuestionDto> questions = new ArrayList<>();
            for (JsonNode q : json.path("questions")) {
                ReportQuestionDto dto = new ReportQuestionDto();
                dto.setReportId(reportId);
                dto.setRoomId(roomId);
                dto.setQuestionSeq(q.path("questionSeq").asInt());
                dto.setQuestionText(textOrNull(q, "questionText"));
                dto.setAnswerText(textOrNull(q, "answerText"));
                dto.setScore(asIntOrNull(q, "score"));
                dto.setLabel(textOrNull(q, "label"));
                dto.setFeedback(textOrNull(q, "feedback"));
                dto.setTags(textOrNull(q, "tags"));
                questions.add(dto);
            }
            if (!questions.isEmpty()) {
                reportDao.insertQuestions(questions);
            }

            return reportId;

        } catch (Exception e) {
            log.error("리포트 파싱/저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("리포트 생성 중 오류가 발생했습니다.", e);
        }
    }

    private Integer asIntOrNull(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return (n.isNull() || n.isMissingNode()) ? null : n.asInt();
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return (n.isNull() || n.isMissingNode()) ? null : n.asText();
    }
}
