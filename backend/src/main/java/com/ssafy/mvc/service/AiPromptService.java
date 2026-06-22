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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiPromptService {

    private final RestClient openAiRestClient;
    private final InterviewPromptDao interviewPromptDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.chat-path:/v1/chat/completions}")
    private String chatPath;

    private static final String PROMPT_TEMPLATE_KO = """
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

	        [난이도별 면접 스타일 규칙]
	        ※ 아래 질문 깊이 기준은 깊이와 방향성을 보여주는 참고용입니다.
	           [...]로 표시된 부분은 반드시 [사용자 서류 텍스트]에 실제로 기재된
	           프로젝트명·기술 스택·경험으로 채워 넣어 고유한 질문을 생성하세요.
	           기준 문장을 그대로 사용하지 마세요.
	
	        EASY (하):
	        - 면접관 톤: 친절하고 편안하게 유도하는 질문 위주
	        - 질문 유형: 동기·목표·역할 설명 중심. 지원자가 경험을 자유롭게 풀어낼 수 있는 열린 질문
	        - 기술 깊이: 직무 지식 불필요. 경험 유무와 태도 확인 수준
	        - 금지: 수치·근거 요구, 실패 경험 추궁, 트레이드오프, 기술적 의사결정 질문
	        - 질문 깊이 기준 (서류 내용으로 반드시 재구성):
	          "[서류에 언급된 직무/활동]에 지원하게 된 계기가 무엇인가요?"
	          "[서류에 언급된 프로젝트/경험]에서 주로 어떤 역할을 담당하셨나요?"
	          "입사 후 가장 도전해보고 싶은 업무 영역이 있다면 무엇인가요?"
	          "본인의 강점이 이 직무에 어떻게 기여할 수 있을 것 같으신가요?"
	
	        NORMAL (중):
	        - 면접관 톤: 중립적. 경험의 맥락과 이유를 묻는 질문 포함
	        - 질문 유형: 문제 해결 과정, 선택의 배경, 직무 적합성 확인
	        - 기술 깊이: 해당 직무의 기본 개념 이해 수준 요구. 구체적 사례와 본인의 기여를 설명할 수 있어야 함
	        - 질문 깊이 기준 (서류 내용으로 반드시 재구성):
	          "[서류에 언급된 프로젝트]에서 어떤 문제가 있었고 어떻게 해결하셨나요?"
	          "협업 과정에서 의견 충돌이 생겼을 때 어떻게 조율하셨나요?"
	          "[서류에 언급된 기술]을 선택한 이유는 무엇이었나요?"
	          "[서류에 언급된 경험] 중 가장 어려웠던 기술적 도전과 그 결과를 설명해 주세요."
	
	        HARD (상):
	        - 면접관 톤: 압박적·도전적. 전문성과 논리를 검증하는 질문
	        - 질문 유형: 기술적 의사결정, 트레이드오프 분석, 설계 근거, 장애·실패 경험, 특정 기술의 내부 동작 원리
	        - 기술 깊이: 해당 직무의 심화 지식 요구.
	          단순 경험 나열이 아닌 "왜", "어떻게", "무엇을 포기했는가"까지 답할 수 있어야 함.
	          수치·데이터 기반 설명을 전제로 질문을 구성하세요.
	        - 금지: 동기·목표 중심의 단순 질문, 모호한 답변으로 넘길 수 있는 열린 질문
	        - 질문 깊이 기준 (서류 내용으로 반드시 재구성):
	          "[서류에 언급된 아키텍처/기술 선택]을 하셨는데, 검토했던 대안과 각각의 트레이드오프를 비교해서 설명해 주세요."
	          "[서류에 언급된 기술] 도입 후 성능·비용·유지보수 측면에서 어떤 변화가 있었나요? 수치로 말씀해 주세요."
	          "[서류에 언급된 프로젝트/시스템]에서 장애나 심각한 버그가 발생했을 때 원인 분석부터 복구까지 단계별로 설명해 주세요."
	          "[서류에 언급된 기술적 판단] 중 나중에 잘못됐다고 느낀 사례와 그 이유를 말씀해 주세요."
	          "[서류에 언급된 시스템]의 병목 구간을 어떻게 찾아냈고, 어떤 방법으로 개선했나요?"
	
	        [난이도별 APPLICANT 답변 수준]
	        - EASY: 경험을 자유롭게 서술. 수치 없어도 됨. 긍정적 어조
	        - NORMAL: 구체적 사례 포함. 본인의 기여와 결과를 간략히 언급
	        - HARD: 수치·근거 포함 필수. 기술적 선택의 이유와 포기한 대안까지 언급.
	          전문 용어를 자연스럽게 사용하며 깊이 있는 답변 생성

            [내용 생성 규칙]
            19. 면접관의 질문은 위 [난이도별 면접 스타일 규칙]을 반드시 따르세요.
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

    private static final String PROMPT_TEMPLATE_EN = """
            You are an AI that generates a mock interview script.
            Based on the input data below, generate an interview script as if it were a real interview, in JSON format.

            [Output Format Rules]
            1. Respond with JSON only. Do not output any character outside JSON (no explanations, no markdown, no code blocks).
            2. The response must be valid JSON.
            3. Do not use trailing commas.

            [Turn Writing Rules]
            4. Write every line of dialogue inside the scenario array.
            5. turnOrder must start at 1 and increase by 1 each turn.
            6. For an interviewer's line, set turnRole to "INTERVIEWER".
            7. For a competing applicant's line, set turnRole to "APPLICANT".
            8. For a turn where the real user must answer, set turnRole to "USER".
            9. Assign questionSeq exactly according to the following rule.
               - Every INTERVIEWER question turn that is followed by a USER or APPLICANT answer: assign sequentially starting from 1 (including self-introduction)
               - The USER/APPLICANT turn that answers that question: same number as the INTERVIEWER turn
               - Pure greeting/closing remarks with no answer (e.g. "Hello", "Thank you for your time") only: set to 0
               - Every question that is followed by an answer (self-introduction, job-related questions, etc.) must be assigned a number of 1 or higher.

            [ID and Hallucination Prevention Rules]
            9. For INTERVIEWER, turnRefId must be one of the provided interviewer personas' interviewer_id values only.
            10. For APPLICANT, turnRefId must be one of the provided applicant personas' applicant_id values only.
            11. For USER, turnRefId must always be null.
            12. Never invent IDs, names, or people that were not provided.
            13. Use only the provided personas. Do not add interviewers or applicants not in the persona list.
            14. Do not arbitrarily add experience or skills not present in the submitted documents.

            [speechText / timeoutSec Rules]
            15. For INTERVIEWER and APPLICANT, speechText must always be a non-null string.
            16. For USER, speechText must always be null. (Never generate the content of the USER's answer. A USER turn only marks the point where the user must answer.)
            17. For a USER turn, set timeoutSec appropriately for the question (maximum 60 seconds).
            18. For INTERVIEWER and APPLICANT turns, set timeoutSec to null.

            [Difficulty-Level Interview Style Rules]
            ※ The question depth references below illustrate the intended tone and depth only.
               Placeholders in [...] must be filled with actual content from [User Document Text]
               (project names, tech stacks, specific experiences). Never copy the reference sentences verbatim.

            EASY (Low):
            - Interviewer tone: Warm and encouraging — guide the applicant with open-ended questions
            - Question type: Motivation, goals, role description. Let applicants freely narrate their experiences
            - Technical depth: No domain knowledge required. Assess attitude and presence of experience
            - Prohibited: Asking for numbers/evidence, probing failures, trade-offs, or technical decision-making
            - Question depth reference (must be rewritten using document content):
              "What motivated you to apply for [role/activity mentioned in documents]?"
              "What was your primary responsibility in [project/experience mentioned in documents]?"
              "Is there a specific area you'd most like to work on after joining the team?"
              "How do you think your strengths would contribute to this role?"

            NORMAL (Mid):
            - Interviewer tone: Neutral. Probe the context and reasoning behind experiences
            - Question type: Problem-solving process, reasoning behind decisions, job fit assessment
            - Technical depth: Basic domain knowledge expected. Applicant should articulate their specific contribution and outcome
            - Question depth reference (must be rewritten using document content):
              "What challenges came up in [project mentioned in documents], and how did you resolve them?"
              "How did you handle disagreements or conflicts during team collaboration?"
              "What was your reasoning for choosing [technology mentioned in documents]?"
              "Walk me through the hardest technical challenge in [experience mentioned in documents] and the result."

            HARD (High):
            - Interviewer tone: Challenging and pressured. Verify deep expertise and logical rigor
            - Question type: Technical decision-making, trade-off analysis, design rationale, incidents/failures, internals of specific technologies
            - Technical depth: Advanced domain knowledge required.
              Applicant must go beyond listing experiences — they must answer "why," "how," and "what was sacrificed."
              Frame questions that presuppose data-driven, quantitative justification.
            - Prohibited: Simple motivation/goal questions, open-ended questions that allow vague non-answers
            - Question depth reference (must be rewritten using document content):
              "You chose [architecture/technology mentioned in documents] — compare the alternatives you considered and the trade-offs of each."
              "What measurable changes in performance, cost, or maintainability followed the adoption of [technology mentioned in documents]? Please be specific."
              "Describe a critical failure in [project/system mentioned in documents]: walk me through detection, root cause analysis, and recovery step by step."
              "Share a technical decision from [experience mentioned in documents] that you later regretted, and explain why."
              "How did you identify the bottleneck in [system mentioned in documents], and what steps did you take to improve it?"

            [Difficulty-Level APPLICANT Answer Standards]
            - EASY: Freely narrate experiences. No numbers required. Positive, enthusiastic tone.
            - NORMAL: Include specific examples. Briefly mention personal contribution and outcome.
            - HARD: Numbers and evidence are mandatory. Reference the reasoning behind technical choices and ruled-out alternatives.
              Use domain terminology naturally; provide deep, substantiated answers.

            [Content Generation Rules]
            19. Base the interviewer's questions strictly on the [Difficulty-Level Interview Style Rules] above.
                 Consider the company name and job characteristics, but do not fabricate uncertain internal company information; use only general-level knowledge.
            20. Questions should cover self-introduction, job competency, project experience, collaboration experience, and problem-solving experience, flowing naturally in that order.
            21. Generate at least 8 INTERVIEWER questions, of which at least 5 must be questions the USER must answer (common questions + USER-specific questions).
            22. APPLICANT should answer the interviewer's questions in a way that reflects their persona.
            23. Every provided interviewer must speak at least once.
            24. Every provided competing applicant must speak at least once.
            25. Generate the entire scenario with 15 to 30 turns total.
            26. Mix the following three turn types appropriately throughout the interview flow.
                - Common questions: INTERVIEWER asks a question, then APPLICANT and USER answer in turn. The order is random (e.g. self-introduction, job competency)
                - USER-specific questions: INTERVIEWER asks the USER a question based on the USER's documents, and only the USER answers.
                - APPLICANT-specific questions: INTERVIEWER asks a specific APPLICANT a question, and only that APPLICANT answers.
                  (The answer to an APPLICANT-specific question must not contradict that applicant's persona, and must stay consistent with their own earlier answers.)

            [Applicant Type Description]
            - NEW: New graduate hiring. An applicant with no experience aiming for a full-time position.
            - INTERN: Internship hiring. An applicant for an internship position prior to full-time employment.
            - EXPERIENCED: Experienced hiring. An applicant who holds relevant job experience.

            [Input Data]
            Room ID: {roomId}
            Company name: {companyName}
            Job role: {jobRole}
            Applicant type: {applicantType}
            Difficulty: {difficulty}
            Number of AI interviewers: {interviewerCount}
            Number of AI competing applicants: {applicantCount}

            [User Document Text]
            Resume:
            {resumeText}
            Portfolio:
            {portfolioText}

            [AI Interviewer Personas]
            {interviewerPersonas}

            [AI Competing Applicant Personas]
            {applicantPersonas}

            [Output JSON Format]
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

            All speechText values must be written in English.
            """;

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

    // 프롬프트 생성 → OpenAI 호출 → 프롬프트 기록 저장 → 응답 파싱
    public InterviewStartResultDto generateScript(InterviewRoomDto room,
                                             List<AiInterviewerDto> interviewers,
                                             List<AiApplicantDto> applicants) {
        boolean isEnglish = "EN".equals(room.getLanguage());
        String prompt = buildPrompt(room, interviewers, applicants, isEnglish);
        String responseBody = callOpenAi(prompt,
                "당신은 모의면접 대본을 생성하는 AI입니다. 반드시 JSON 형식으로만 응답하세요.", 0.7);

        savePromptRecord(room.getRoomId(), isEnglish ? "SCRIPT_EN" : "SCRIPT_KR", prompt, responseBody);

        InterviewStartResultDto script = parseScript(responseBody);
        // AI가 반환한 roomId 대신 실제 roomId로 덮어씀 (환각 방지)
        script.setRoomId(room.getRoomId());
        return script;
    }

    // 면접 종료 후 Q&A 분석 프롬프트 생성 → OpenAI 호출 → 프롬프트 기록 저장 → JSON 파싱
    // (분석 결과를 DB 3개 테이블에 저장하는 건 ReportService 담당)
    public JsonNode generateReportAnalysis(InterviewRoomDto room,
                                            List<InterviewScenarioDto> scenarios,
                                            Map<Long, String> applicantNames) {
        String prompt = buildAnalysisPrompt(room, scenarios, applicantNames);
        String responseBody = callOpenAi(prompt,
                "당신은 면접 평가 전문가입니다. 반드시 JSON 형식으로만 응답하세요.", 0.3);

        savePromptRecord(room.getRoomId(), "REPORT", prompt, responseBody);

        return extractContent(responseBody);
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

    private String buildPrompt(InterviewRoomDto room,
                               List<AiInterviewerDto> interviewers,
                               List<AiApplicantDto> applicants,
                               boolean isEnglish) {
        // 면접 언어별로 프롬프트 템플릿 자체를 분리 (KO/EN 각각 완전한 규칙 세트 보유)
        String template = isEnglish ? PROMPT_TEMPLATE_EN : PROMPT_TEMPLATE_KO;

        return template
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

    // 대본 생성·리포트 분석이 공통으로 쓰는 OpenAI 호출 (system 메시지/temperature만 다름)
    private String callOpenAi(String userPrompt, String systemMessage, double temperature) {
        try {
            String escapedSystem = objectMapper.writeValueAsString(systemMessage);
            String escapedPrompt = objectMapper.writeValueAsString(userPrompt);
            String requestBody = """
                    {
                      "model": "gpt-4o",
                      "messages": [
                        { "role": "system", "content": %s },
                        { "role": "user", "content": %s }
                      ],
                      "response_format": { "type": "json_object" },
                      "temperature": %s
                    }
                    """.formatted(escapedSystem, escapedPrompt, temperature);

            return openAiRestClient.post()
                    .uri(chatPath)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 호출 실패", e);
        }
    }

    // OpenAI 응답 envelope(choices[0].message.content)에서 실제 JSON 본문만 추출
    private JsonNode extractContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return objectMapper.readTree(content);
        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 실패", e);
        }
    }
    
    // 프롬프트 저장
    private void savePromptRecord(Long roomId, String promptType, String promptText, String responseText) {
        InterviewPromptDto dto = new InterviewPromptDto();
        dto.setRoomId(roomId);
        dto.setPromptType(promptType);
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
