package com.ssafy.mvc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.mvc.dao.InterviewPromptDao;
import com.ssafy.mvc.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiPromptService {

    private final RestClient openAiRestClient;
    private final InterviewPromptDao interviewPromptDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.chat-path:/v1/chat/completions}")
    private String chatPath;

    private static final String PROMPT_TEMPLATE = """
            당신은 모의면접 대본을 생성하는 AI입니다.
            아래 입력 데이터를 바탕으로 실제 면접처럼 진행되는 대본 스크립트를 JSON 형식으로 생성하세요.

            [출력 형식 규칙]
            1. 반드시 JSON만 응답하세요. JSON 외의 어떠한 문자(설명, 마크다운, 코드블록)도 출력하지 마세요.
            2. 응답은 반드시 유효한 JSON이어야 합니다.
            3. 마지막 쉼표(trailing comma)를 사용하지 마세요.

            [턴 작성 규칙]
            4. 모든 대사는 scenario 배열 안에 작성하세요.
            5. turnOrder는 1부터 시작하여 1씩 증가해야 합니다.
            6. 면접관의 발화는 turnRole을 "INTERVIEWER"로 작성하세요.
            7. 경쟁 지원자의 발화는 turnRole을 "APPLICANT"로 작성하세요.
            8. 실제 사용자가 답변해야 하는 차례는 turnRole을 "USER"로 작성하세요.
            9. questionSeq는 다음 기준으로 반드시 정확하게 부여하세요.
               - USER 또는 APPLICANT의 답변이 뒤따르는 모든 INTERVIEWER 질문 턴: 1부터 순서대로 부여 (자기소개 포함)
               - 그 질문에 응답하는 USER/APPLICANT 턴: INTERVIEWER와 동일한 번호
               - 답변자가 없는 순수 인사/마무리 발언(예: "안녕하세요", "수고하셨습니다")만 0으로 설정
               - 자기소개, 직무 관련 질문 등 답변이 뒤따르는 모든 질문은 반드시 1 이상의 번호를 부여하세요.

            [ID 및 환각 방지 규칙]
            9. INTERVIEWER의 turnRefId는 반드시 제공된 면접관 페르소나의 interviewer_id 중 하나만 사용하세요.
            10. APPLICANT의 turnRefId는 반드시 제공된 지원자 페르소나의 applicant_id 중 하나만 사용하세요.
            11. USER의 turnRefId는 반드시 null로 작성하세요.
            12. 제공되지 않은 ID, 이름, 인물을 절대 만들지 마세요.
            13. 제공된 페르소나만 사용하세요. 페르소나 목록에 없는 면접관이나 지원자를 추가하지 마세요.
            14. 서류 내용에 없는 경력이나 기술을 임의로 추가하지 마세요.

            [speechText / timeoutSec 규칙]
            15. INTERVIEWER, APPLICANT의 speechText는 반드시 null이 아닌 문자열이어야 합니다.
            16. USER의 speechText는 반드시 null이어야 합니다. (USER의 답변 내용은 절대 생성하지 마세요. USER 턴은 사용자가 답변해야 하는 시점만 표현합니다.)
            17. USER 턴의 timeoutSec는 질문에 맞게 설정하세요. (최대값: 60초)
            18. INTERVIEWER, APPLICANT 턴의 timeoutSec는 null로 작성하세요.

            [내용 생성 규칙]
            19. 면접관의 질문은 직무, 지원 유형, 난이도, [사용자 서류 텍스트]를 반영하세요.
                 EASY: 기본 경험 확인, 동기, 역할 설명 중심으로 질문하세요.
                 NORMAL: 경험의 과정, 문제 해결 방식, 직무 적합성을 함께 확인하세요.
                 HARD: 기술적 의사결정, 트레이드오프, 실패 경험, 구체적 수치와 근거를 요구하는 압박 질문을 포함하세요.
                 회사명과 직무 특성을 고려하되, 확실하지 않은 회사 내부 정보는 지어내지 말고 일반적인 수준에서만 활용하세요.
            20. 질문은 자기소개, 직무역량, 프로젝트 경험, 협업 경험, 문제해결 경험을 포함하고, 질문은 자기소개, 직무역량, 프로젝트 경험, 협업 경험, 문제해결 경험 순서가 자연스럽게 이어지도록 구성하세요.
            21. INTERVIEWER 질문은 최소 8개 이상 생성하되, 그중 USER가 답해야 하는 질문(공통 질문 + USER 개별 질문)이 최소 5개 이상이 되도록 하세요.
            22. APPLICANT는 면접관의 질문에 대해 자신의 페르소나를 반영한 답변을 생성하세요.
            23. 제공된 모든 면접관은 최소 1회 이상 발화해야 합니다.
            24. 제공된 모든 경쟁 지원자는 최소 1회 이상 발화해야 합니다.
            25. 전체 시나리오는 15~30개의 턴으로 생성하세요.
            26. 면접 흐름은 다음 세 가지 유형을 적절히 섞어 구성하세요.
                - 공통 질문: INTERVIEWER가 질문을 던지면 APPLICANT와 USER가 차례로 답합니다. 순서는 랜덤입니다 (예: 자기소개, 직무역량)
                - USER 개별 질문: INTERVIEWER가 USER의 서류 내용을 근거로 USER에게만 질문하고, USER만 답합니다.
                - APPLICANT 개별 질문: INTERVIEWER가 특정 APPLICANT에게만 질문하고, 해당 APPLICANT만 답합니다.
                  (APPLICANT 개별 질문에 대한 답변은 해당 지원자의 페르소나와 모순되지 않게, 그리고 앞서 한 자신의 답변과 일관되게 작성하세요.)

            [지원 유형 설명]
            - NEW: 신입 채용. 정규직 입사를 목표로 하는 경력 없는 지원자.
            - INTERN: 인턴 채용. 정규직 전 단계의 인턴십 지원자.
            - EXPERIENCED: 경력 채용. 관련 직무 경력을 보유한 지원자.

            [입력 데이터]
            방 ID: {roomId}
            회사명: {companyName}
            직무: {jobRole}
            지원 유형: {applicantType}
            난이도: {difficulty}
            AI 면접관 수: {interviewerCount}
            AI 경쟁 지원자 수: {applicantCount}

            [사용자 서류 텍스트]
            이력서:
            {resumeText}
            포트폴리오:
            {portfolioText}

            [AI 면접관 페르소나]
            {interviewerPersonas}

            [AI 경쟁 지원자 페르소나]
            {applicantPersonas}

            [출력 JSON 형식]
            {
              "roomId": number,
              "interviewTitle": string,
              "companyName": string,
              "jobRole": string,
              "applicantType": string,
              "difficulty": string,
              "scenario": [
                {
                  "questionSeq": number,
                  "turnOrder": number,
                  "turnRole": "INTERVIEWER" | "APPLICANT" | "USER",
                  "turnRefId": number | null,
                  "speechText": string | null,
                  "timeoutSec": number | null
                }
              ]
            }
            """;

    // 프롬프트 생성 → OpenAI 호출 → 프롬프트 기록 저장 → 응답 파싱
    public InterviewStartResultDto generateScript(InterviewRoomDto room,
                                             List<AiInterviewerDto> interviewers,
                                             List<AiApplicantDto> applicants) {
        String prompt = buildPrompt(room, interviewers, applicants);
        String responseBody = callOpenAi(prompt);

        savePromptRecord(room.getRoomId(), prompt, responseBody);

        InterviewStartResultDto script = parseScript(responseBody);
        // AI가 반환한 roomId 대신 실제 roomId로 덮어씀 (환각 방지)
        script.setRoomId(room.getRoomId());
        return script;
    }

    private String buildPrompt(InterviewRoomDto room,
                               List<AiInterviewerDto> interviewers,
                               List<AiApplicantDto> applicants) {
        return PROMPT_TEMPLATE
                .replace("{roomId}",             String.valueOf(room.getRoomId()))
                .replace("{companyName}",         nvl(room.getCompanyName(), "미정"))
                .replace("{jobRole}",             nvl(room.getJobName(), "미정"))
                .replace("{applicantType}",       nvl(room.getApplicantType(), "NEW"))
                .replace("{difficulty}",          nvl(room.getDifficulty(), "MEDIUM"))
                .replace("{interviewerCount}",    String.valueOf(room.getAiInterviewerCnt()))
                .replace("{applicantCount}",      String.valueOf(room.getAiApplicantCnt()))
                .replace("{resumeText}",          nvl(room.getResumeText(), "미제출"))
                .replace("{portfolioText}",       nvl(room.getPortfolioText(), "미제출"))
                .replace("{interviewerPersonas}", formatInterviewers(interviewers))
                .replace("{applicantPersonas}",   formatApplicants(applicants));
    }

    private String formatInterviewers(List<AiInterviewerDto> interviewers) {
        if (interviewers == null || interviewers.isEmpty()) return "없음";
        StringBuilder sb = new StringBuilder();
        for (AiInterviewerDto i : interviewers) {
            sb.append("interviewer_id: ").append(i.getInterviewerId())
              .append(", 이름: ").append(i.getInterviewerName())
              .append(", 성별: ").append(nvl(i.getInterviewerGender(), "미상"))
              .append(", 성격: ").append(nvl(i.getInterviewerPrompt(), ""))
              .append("\n");
        }
        return sb.toString().trim();
    }

    private String formatApplicants(List<AiApplicantDto> applicants) {
        if (applicants == null || applicants.isEmpty()) return "없음";
        StringBuilder sb = new StringBuilder();
        for (AiApplicantDto a : applicants) {
            sb.append("applicant_id: ").append(a.getApplicantId())
              .append(", 이름: ").append(a.getApplicantName())
              .append(", 성별: ").append(nvl(a.getApplicantGender(), "미상"))
              .append(", 지원유형: ").append(a.getApplicantType())
              .append(", 페르소나: ").append(nvl(a.getApplicantPrompt(), ""))
              .append("\n");
        }
        return sb.toString().trim();
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
                          "content": "당신은 모의면접 대본을 생성하는 AI입니다. 반드시 JSON 형식으로만 응답하세요."
                        },
                        {
                          "role": "user",
                          "content": %s
                        }
                      ],
                      "response_format": { "type": "json_object" },
                      "temperature": 0.7
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
    
    // 프롬프트 저장
    private void savePromptRecord(Long roomId, String promptText, String responseText) {
        InterviewPromptDto dto = new InterviewPromptDto();
        dto.setRoomId(roomId);
        dto.setPromptType("SCRIPT");
        dto.setPromptText(promptText);
        dto.setResponseText(responseText);
        interviewPromptDao.insertPrompt(dto);
    }

    private InterviewStartResultDto parseScript(String responseBody) {
        try {
            JsonNode root    = objectMapper.readTree(responseBody);
            String content   = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode jsonNode = objectMapper.readTree(content);

            InterviewStartResultDto script = new InterviewStartResultDto();
            script.setInterviewTitle(jsonNode.path("interviewTitle").asText());
            script.setCompanyName(jsonNode.path("companyName").asText());
            script.setJobRole(jsonNode.path("jobRole").asText());
            script.setApplicantType(jsonNode.path("applicantType").asText());
            script.setDifficulty(jsonNode.path("difficulty").asText());

            List<InterviewScenarioDto> scenarios = new ArrayList<>();
            for (JsonNode turn : jsonNode.path("scenario")) {
                InterviewScenarioDto s = new InterviewScenarioDto();
                s.setQuestionSeq(turn.path("questionSeq").asInt(0));
                s.setTurnOrder(turn.path("turnOrder").asInt());
                s.setTurnRole(turn.path("turnRole").asText());
                s.setTurnRefId(turn.path("turnRefId").isNull() ? null : turn.path("turnRefId").asLong());
                s.setSpeechText(turn.path("speechText").isNull() ? null : turn.path("speechText").asText());
                s.setTimeoutSec(turn.path("timeoutSec").isNull() ? null : turn.path("timeoutSec").asInt());
                scenarios.add(s);
            }
            script.setScenario(scenarios);
            return script;

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("AI 대본 생성 중 오류가 발생했습니다.", e);
        }
    }

    private String nvl(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
