package com.ssafy.mvc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.mvc.dao.AiPersonaDao;
import com.ssafy.mvc.dao.InterviewRoomDao;
import com.ssafy.mvc.dao.InterviewScenarioDao;
import com.ssafy.mvc.dao.ReportDao;
import com.ssafy.mvc.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final RestClient openAiRestClient;
    private final InterviewRoomDao interviewRoomDao;
    private final InterviewScenarioDao interviewScenarioDao;
    private final AiPersonaDao aiPersonaDao;
    private final ReportDao reportDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.chat-path:/v1/chat/completions}")
    private String chatPath;

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

        String prompt = buildAnalysisPrompt(room, scenarios, applicantNames);
        String responseBody = callOpenAi(prompt);
        return parseAndSave(roomId, responseBody, applicantNames);
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

    private String buildAnalysisPrompt(InterviewRoomDto room,
                                       List<InterviewScenarioDto> scenarios,
                                       Map<Long, String> applicantNames) {
        // question_seq > 0 인 턴들을 seq 순서대로 그루핑
        Map<Integer, List<InterviewScenarioDto>> groups = new TreeMap<>();
        for (InterviewScenarioDto s : scenarios) {
            if (s.getQuestionSeq() != null && s.getQuestionSeq() > 0) {
                groups.computeIfAbsent(s.getQuestionSeq(), k -> new ArrayList<>()).add(s);
            }
        }

        // 질문별 Q&A 텍스트 구성
        StringBuilder qa = new StringBuilder();
        for (Map.Entry<Integer, List<InterviewScenarioDto>> entry : groups.entrySet()) {
            String question = "";
            String userAnswer = "(답변 없음)";
            Map<String, String> appAnswers = new LinkedHashMap<>();

            for (InterviewScenarioDto s : entry.getValue()) {
                switch (s.getTurnRole()) {
                    case "INTERVIEWER" -> question = nvl(s.getSpeechText(), "");
                    case "USER" -> userAnswer = nvl(s.getAnswerText(), "(답변 없음)");
                    case "APPLICANT" -> {
                        if (s.getTurnRefId() != null) {
                            String name = applicantNames.getOrDefault(s.getTurnRefId(), "AI지원자" + s.getTurnRefId());
                            appAnswers.put(name, nvl(s.getSpeechText(), ""));
                        }
                    }
                }
            }

            qa.append("Q").append(entry.getKey()).append(". ").append(question).append("\n");
            qa.append("  나: ").append(userAnswer).append("\n");
            for (Map.Entry<String, String> app : appAnswers.entrySet()) {
                qa.append("  ").append(app.getKey()).append(": ").append(app.getValue()).append("\n");
            }
            qa.append("\n");
        }

        // AI 경쟁 지원자 목록 (없으면 "없음")
        String applicantList = applicantNames.isEmpty() ? "없음" :
                applicantNames.entrySet().stream()
                        .map(e -> e.getValue() + " (persona_id: " + e.getKey() + ")")
                        .collect(Collectors.joining("\n"));

        return ANALYSIS_PROMPT_TEMPLATE
                .replace("{company}", nvl(room.getCompanyName(), "미정"))
                .replace("{job}", nvl(room.getJobName(), "미정"))
                .replace("{type}", nvl(room.getApplicantType(), "NEW"))
                .replace("{difficulty}", nvl(room.getDifficulty(), "NORMAL"))
                .replace("{applicantList}", applicantList)
                .replace("{qaSection}", qa.toString().trim());
    }

    private Long parseAndSave(Long roomId, String responseBody, Map<Long, String> applicantNames) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode json = objectMapper.readTree(content);

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

    private String callOpenAi(String userPrompt) {
        try {
            String escapedPrompt = objectMapper.writeValueAsString(userPrompt);
            String requestBody = """
                    {
                      "model": "gpt-4o",
                      "messages": [
                        {
                          "role": "system",
                          "content": "당신은 면접 평가 전문가입니다. 반드시 JSON 형식으로만 응답하세요."
                        },
                        {
                          "role": "user",
                          "content": %s
                        }
                      ],
                      "response_format": { "type": "json_object" },
                      "temperature": 0.3
                    }
                    """.formatted(escapedPrompt);

            return openAiRestClient.post()
                    .uri(chatPath)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 호출 실패", e);
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

    private String nvl(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static final String ANALYSIS_PROMPT_TEMPLATE = """
            당신은 면접 평가 전문가입니다.
            아래 면접 정보와 질문/답변 내용을 바탕으로 지원자를 종합적으로 평가하세요.
            반드시 JSON만 응답하세요. JSON 외의 문자(설명, 마크다운, 코드블록)는 절대 출력하지 마세요.

            [면접 정보]
            회사: {company}
            직무: {job}
            지원 유형: {type}
            난이도: {difficulty}

            [AI 경쟁 지원자 목록]
            {applicantList}

            [면접 Q&A]
            (나: 실제 사용자 답변 / 나머지: AI 경쟁 지원자 답변)
            {qaSection}

            [평가 항목 안내]
            - compExpertise: 직무 전문성 (기술적 지식, 직무 관련 경험의 깊이)
            - compLogic: 논리적 사고력 (구조적 설명, 인과관계, 결론 도출)
            - compCommu: 커뮤니케이션 (명확한 표현, 답변의 흐름과 전달력)
            - compCulture: 조직 적합성 (가치관, 협업 태도, 성장 마인드셋)
            - compPressure: 압박 대응력 (어려운 질문에서의 태도 유지 및 대처)
            - speechWpm: 예상 말하기 속도 (분당 어절 수, 총 답변 어절 수 ÷ 총 답변 시간(분)으로 추정)
            - speechFiller: 추임새 예상 횟수 ("음", "어", "그", "저", "뭐" 등 답변 내 등장 횟수 합산)

            [출력 JSON 형식]
            {
              "overallScore": number (0-100),
              "aiComment": string (종합 평가 코멘트),
              "compExpertise": number (0-100),
              "compExpertiseDetail": string,
              "compLogic": number (0-100),
              "compLogicDetail": string,
              "compCommu": number (0-100),
              "compCommuDetail": string,
              "compCulture": number (0-100),
              "compCultureDetail": string,
              "compPressure": number (0-100),
              "compPressureDetail": string,
              "speechWpm": number,
              "speechFiller": number,
              "applicants": [
                {
                  "isUser": true,
                  "personaId": null,
                  "name": "나",
                  "score": number (0-100),
                  "strength": string (핵심 강점 1~2문장),
                  "weakness": string (개선 포인트 1~2문장)
                }
              ],
              "questions": [
                {
                  "questionSeq": number,
                  "questionText": string,
                  "answerText": string (사용자 답변 원문),
                  "score": number (0-100),
                  "label": "우수" | "양호" | "보통" | "미흡",
                  "feedback": string (구체적 피드백),
                  "tags": string (쉼표로 구분된 키워드, 예: "논리성,구체성,직무연관")
                }
              ]
            }

            [주의사항]
            - applicants 배열에는 반드시 사용자("나") 항목을 포함하고, AI 경쟁 지원자가 있다면 각각 추가하세요.
            - AI 경쟁 지원자 항목의 personaId는 위 경쟁 지원자 목록에 나온 persona_id 값을 그대로 사용하세요.
            - questions 배열은 사용자가 답변한 질문만 포함하세요 (사용자 답변이 없으면 제외).
            """;
}
