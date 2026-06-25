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
import java.util.concurrent.ThreadLocalRandom;
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
            당신은 실제 다대다 면접처럼 진행되는 모의면접 대본을 생성하는 AI입니다.
            아래 입력 데이터를 바탕으로 면접관, 실제 사용자, AI 경쟁 지원자가 함께 참여하는 면접 시나리오를 JSON 형식으로 생성하세요.

            [가장 중요한 목표]
            - 매번 같은 질문 패턴을 반복하지 말고, 회사·직무·지원유형·난이도·서류 내용에 맞게 다양한 면접 흐름을 구성하세요.
            - 질문은 실제 면접처럼 자연스럽고 구체적이어야 합니다.
            - 단, 제공되지 않은 사실, 경력, 기술, 인물, ID는 만들지 마세요.

            [출력 규칙]
            1. 반드시 JSON만 응답하세요. 설명, 마크다운, 코드블록은 출력하지 마세요.
            2. JSON은 반드시 유효해야 하며 trailing comma를 사용하지 마세요.
            3. 모든 대사는 scenario 배열 안에 작성하세요.

            [턴 규칙]
            - turnOrder는 1부터 시작해 1씩 증가합니다.
            - INTERVIEWER: 면접관 발화
            - APPLICANT: AI 경쟁 지원자 답변
            - USER: 실제 사용자가 답변해야 하는 차례
            - USER의 speechText는 반드시 null입니다.
            - USER의 turnRefId는 반드시 null입니다.
            - INTERVIEWER의 turnRefId는 제공된 interviewer_id 중 하나만 사용하세요.
            - APPLICANT의 turnRefId는 제공된 applicant_id 중 하나만 사용하세요.
            - INTERVIEWER, APPLICANT의 timeoutSec는 null입니다.
            - USER의 timeoutSec는 질문 난이도에 맞게 30~60초 사이로 설정하세요.

            [questionSeq 규칙]
            - 답변이 뒤따르는 INTERVIEWER 질문은 1부터 순서대로 questionSeq를 부여하세요.
            - 해당 질문에 답하는 USER/APPLICANT 턴은 같은 questionSeq를 사용하세요.
            - 답변이 없는 단순 인사나 마무리 발언만 questionSeq를 0으로 설정하세요.

            [면접 구성 규칙]
            전체 INTERVIEWER 질문은 정확히 10개입니다. questionSeq는 1부터 순서대로 부여하세요.

            ▶ 고정 위치 (2개)
            - 첫 번째 질문 : 자기소개 (공통)
              "자기소개 부탁드립니다." 한 가지 요청만 하세요. 추가 질문을 붙이지 마세요.
            - 마지막 질문 : "마지막으로 하고 싶은 말씀이 있으신가요?" (공통)
              마지막 멘트 요청 하나만 하세요. 추가 질문을 붙이지 마세요.

            ▶ 나머지 8개 — 아래 구성을 자연스러운 면접 흐름으로 자유 배치
            구성 : 직무·주제 공통 질문 3개 + 서류 기반 개별 질문 3개 + 꼬리질문 2개
            배치 원칙: 꼬리질문은 반드시 참조 질문 바로 다음 순번에 위치시킵니다.
              예) 공통 Q에서 APPLICANT 발언 → 바로 다음 순번에 꼬리질문(추가관점)
                  개인 Q에서 USER 답변 → 바로 다음 순번에 꼬리질문(심화)
            꼬리질문 2개가 연속으로 붙는 것은 금지합니다.

            ▶ 공통 질문 규칙 (자기소개·마지막멘트 포함, 총 5개)
            - INTERVIEWER speechText는 질문 말미에 반드시 실제 참가자 이름을 나열하며
              "A님, B님, C님 순으로 답변 부탁드립니다." 처럼 답변 순서를 안내하세요.
              (이름은 {userName}과 각 APPLICANT의 실제 이름을 사용하며, 순서는 매 질문마다 자유롭게 바꾸세요.)
            - 공통 질문에서 특정 참가자의 이름을 질문 앞머리에 호명하지 마세요.
              이름은 답변 순서 안내 문구에만 사용합니다.

            ▶ 개인 질문 규칙 (서류 기반 3개 + 꼬리질문 2개, 총 5개)
            - USER만 답변하며, APPLICANT 답변 없습니다.
            - 반드시 "{userName}님," 처럼 사용자 이름을 호명하며 시작하세요.
            - 꼬리질문 — 추가 관점 유도형 (1개):
              반드시 아래 3단계 구조로 구성하세요.
              ① INTERVIEWER가 특정 APPLICANT 1명에게만 가치관·조직 적합성·직무 태도 관련 질문을 던집니다.
                 ("A님, ~에 대해 어떻게 생각하시나요?" 형식으로 이름을 직접 호명합니다.)
              ② 해당 APPLICANT만 답변합니다. (다른 참가자는 이 질문에 답변하지 않습니다.)
              ③ INTERVIEWER가 바로 다음 순번에 USER에게 꼬리질문을 합니다.
                 "A님이 ~~이라고 하셨는데, {userName}님은 그 외에 생각나는 것이 있다면 말씀해 주세요." 형식입니다.
              ※ 공통 질문(모두 답변)에서 파생된 꼬리질문은 허용하지 않습니다.
                 APPLICANT 개인 경험·사례 질문도 대상이 될 수 없습니다.
                 APPLICANT가 직접 발언한 단어·표현만 인용하세요.
            - 꼬리질문 — 심화형 (1개):
              직전 USER 개인 질문 답변에서 구체적인 근거·방법·결과를 추가로 묻는 질문입니다.

            기타 규칙:
            - 모든 면접관은 최소 1회 이상 발화해야 합니다.
            - 모든 AI 경쟁 지원자는 최소 2회 이상 발화해야 합니다.
            - 같은 주제나 같은 문장 구조를 반복하지 마세요.

            [질문 생성 방향]
            - 질문은 다음 요소를 종합해 만드세요.
              1. 회사명
              2. 직무
              3. 지원 유형
              4. 난이도
              5. 사용자 이력서/포트폴리오
              6. 면접관 페르소나
              7. AI 경쟁 지원자 페르소나

            - 서류가 제출된 경우, 서류에 실제로 있는 프로젝트·경험·기술·활동을 바탕으로 질문하세요.
            - 서류가 미제출이면 직무와 지원 유형에 맞는 일반 면접 질문으로 대체하세요.
            - 질문 유형은 AI가 상황에 맞게 선택하되, 아래 유형을 적절히 섞으세요.
              · 경험 기반 질문
              · 직무 역량 질문
              · 문제 해결 질문
              · 협업/갈등 질문
              · 상황형 질문 (반드시 현실적 맥락 서론 1~2문장 후 가정 상황 제시, "만약 ~이라면?"으로 바로 시작 금지)
              · 가치관/조직 적합성 질문
              · HARD인 경우 전문성 검증 질문

            [난이도 기준]
            EASY:
            - 친절하고 편안한 질문을 중심으로 구성하세요.
            - 지원자의 경험, 역할 이해, 직무 관심도를 확인하세요.
            - 지나치게 전문적인 지식이나 수치 검증은 피하세요.

            NORMAL:
            - 경험의 배경, 선택 이유, 문제 해결 과정, 협업 방식을 확인하세요.
            - 지원자가 자신의 기여와 결과를 구체적으로 설명하게 만드세요.

            HARD:
            - 날카롭고 간결하며 압박적인 톤. 칭찬·공감 표현 일절 금지.
            - "~설명해 보세요.", "~말씀해 보시죠." 형태를 선호하고, "~해 주시겠어요?"는 지양합니다.
            - 직무 전문 용어, 방법론, 프레임워크 이름을 질문에 직접 사용하세요.
            - NEW/INTERN 지원자에게 시니어급 실무 권한이나 의사결정을 전제하지 마세요.

            [지원 유형 기준]
            NEW:
            - 학교 프로젝트, 대외활동, 공모전, 인턴, 팀 경험을 직무와 연결해 질문하세요.
            - 실무 경력보다 학습력, 잠재력, 문제 해결 태도를 확인하세요.

            INTERN:
            - 기본 이해도, 태도, 커뮤니케이션, 성장 가능성을 확인하세요.
            - 완성된 실무자 수준을 요구하지 마세요.

            EXPERIENCED:
            - 실무 성과, 의사결정 근거, 문제 해결 과정, 조직 기여도를 확인하세요.
            - 성과 지표, 실패 경험, 대안 비교 질문을 포함할 수 있습니다.

            [회사명 활용 규칙]
            - 잘 알려진 회사명이라면 공개적으로 알려진 사업/서비스 맥락을 자연스럽게 반영하세요.
            - 확실하지 않은 회사명이라면 내부 정보, 수치, 전략을 단정하지 말고 직무 중심으로 질문하세요.

            [면접관 페르소나 반영]
            - 각 면접관의 성격과 관점을 질문 톤에 반영하세요.
            - 실무형 면접관은 성과·문제 해결·근거를 중점적으로 질문하세요.
            - 인성형 면접관은 협업·가치관·태도를 중점적으로 질문하세요.
            - 압박형 면접관은 간결하고 날카롭게 질문하세요.
            - 친절형 면접관은 부드럽고 안내하는 말투를 사용하세요.

            [AI 경쟁 지원자 답변 규칙]
            - AI 경쟁 지원자는 자신의 페르소나에 맞는 구체적인 경험을 바탕으로 답변하세요.
            - 답변은 최소 120자 이상으로 작성하세요.
            - 추상적인 의견만 말하지 말고, 상황·행동·결과가 드러나게 작성하세요.
            - 같은 AI 지원자가 앞에서 말한 경험과 뒤에서 말한 경험은 일관되어야 합니다.

            [꼬리질문 규칙]
            - 꼬리질문은 총 2개이며 자연스러운 흐름 안에 배치합니다.
            - 꼬리질문 대상은 반드시 USER입니다. APPLICANT는 꼬리질문에 답변하지 않습니다.
            - 꼬리질문 2개가 연속으로 붙는 것은 금지합니다.
            - 추가 관점 유도형:
              공통 질문(모든 참가자 답변)에서 파생하지 않습니다.
              대신 다음 3단계 구조를 엄격히 따르세요:
              ① INTERVIEWER → 특정 APPLICANT에게만 가치관·직무 태도 관련 질문 (해당 이름 호명)
              ② 해당 APPLICANT만 답변 (다른 참가자 답변 없음)
              ③ INTERVIEWER → USER에게 꼬리질문 (바로 다음 questionSeq)
                 "A님이 ~~이라고 하셨는데, {userName}님은 그 외에 생각나는 것이 있다면 말씀해 주세요."
              APPLICANT가 직접 발언한 표현만 인용하고, 답변을 평가하거나 의견을 묻지 마세요.
            - 심화형:
              USER 개인 질문 바로 다음 순번에 배치하세요.
              USER의 직전 답변에서 근거·방법·결과를 추가로 묻는 질문입니다.
            - 꼬리질문도 새로운 questionSeq를 부여하세요.

            [다양성 규칙]
            - 같은 입력으로 여러 번 생성하더라도 질문 순서, 질문 관점, 면접관 배정, APPLICANT 답변 사례가 달라지도록 하세요.
            - 흔한 질문 문장만 반복하지 마세요.
            - 예: "어떤 역할을 담당했나요?", "어려웠던 점은 무엇인가요?", "어떻게 해결했나요?" 같은 표현은 필요할 때만 사용하고, 가능한 한 구체적인 맥락으로 바꾸세요.
            - 이번 생성에서는 하나의 자연스러운 면접 흐름을 자유롭게 설계하세요.

            [입력 데이터]
            방 ID: {roomId}
            사용자 이름: {userName}
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
            You are an AI that generates a mock interview script for a multi-participant interview.
            Based on the input data below, generate an interview scenario in JSON format where interviewers, the real user, and AI competing applicants all participate.

            [Primary Goal]
            - Never repeat the same question patterns — design a varied interview flow tailored to the company, job role, applicant type, difficulty, and document content.
            - Questions must be natural and specific, as in a real interview.
            - Do not invent facts, experience, skills, people, or IDs that are not provided.

            [Output Rules]
            1. Respond with JSON only. Do not include explanations, markdown, or code blocks.
            2. JSON must be valid — no trailing commas.
            3. Write all dialogue inside the scenario array.

            [Turn Rules]
            - turnOrder starts at 1 and increments by 1.
            - INTERVIEWER: interviewer speaks
            - APPLICANT: AI competing applicant answers
            - USER: the real user's answer turn
            - USER speechText must always be null.
            - USER turnRefId must always be null.
            - INTERVIEWER turnRefId must be one of the provided interviewer_id values only.
            - APPLICANT turnRefId must be one of the provided applicant_id values only.
            - INTERVIEWER and APPLICANT timeoutSec is null.
            - USER timeoutSec: set between 30–60 seconds based on question difficulty.

            [questionSeq Rules]
            - INTERVIEWER questions followed by answers: assign sequentially from 1.
            - USER/APPLICANT turns answering that question: use the same questionSeq.
            - Pure greetings or closing remarks with no answers: set to 0.

            [Interview Structure Rules]
            Generate exactly 10 INTERVIEWER questions. Assign questionSeq sequentially from 1.

            ▶ Fixed positions (2 questions)
            - First question: Self-introduction (common)
              Ask only "Please introduce yourself." — do not attach any additional question.
            - Last question: "Is there anything you'd like to say in closing?" (common)
              Ask only the closing-statement request — do not attach any additional question.

            ▶ Remaining 8 questions — arrange freely in a natural interview flow:
            Composition: 3 common job/topic questions + 3 individual document-based questions + 2 follow-up questions
            Placement rule: each follow-up must appear immediately after the question it references.
              e.g. APPLICANT answers a common question → next seq: perspective-extension follow-up
                   USER answers an individual question → next seq: depth follow-up
            The two follow-up questions must NOT appear consecutively.

            ▶ Common Question Rules (self-intro + closing + 3 others = 5 total)
            - At the end of each common question's speechText, the INTERVIEWER must announce the answer order
              using actual participant names: "A, B, and C, please answer in that order."
              ({userName} and each APPLICANT's real name; vary the order across questions.)
            - Do NOT open a common question by addressing a specific participant by name.
              Names are only used in the answer-order announcement at the end.

            ▶ Individual Question Rules (3 document-based + 2 follow-ups = 5 total)
            - USER only answers; no APPLICANT answers.
            - Every individual question must begin by addressing the USER by name: "{userName},"
            - Follow-up — perspective extension (1 question):
              Must follow this exact 3-step structure:
              ① INTERVIEWER asks ONE specific APPLICANT a values/culture-fit/job-attitude question by name.
                 ("A, what do you think about...?" — address that APPLICANT directly.)
              ② Only that APPLICANT answers. No other participant answers this question.
              ③ INTERVIEWER immediately asks USER a follow-up (next questionSeq):
                 "A said [exact phrase], {userName} — is there anything else that comes to mind?"
              Do NOT derive this from a common question (where everyone answers).
              Do NOT cite personal experience stories from the APPLICANT.
              Do NOT ask the USER to evaluate or react to the APPLICANT's answer.
            - Follow-up — depth question (1 question):
              Based on the USER's most recent individual answer, ask for specific reasoning, method, or outcome.

            Additional rules:
            - Every interviewer must speak at least once.
            - Every AI competing applicant must speak at least twice.
            - Do not repeat the same topic or sentence structure.

            [Question Generation]
            - Synthesize the following to generate questions:
              1. Company name
              2. Job role
              3. Applicant type
              4. Difficulty
              5. User's resume/portfolio
              6. Interviewer persona
              7. AI competing applicant persona

            - If documents are submitted, base questions on actual projects, experiences, skills, and activities in those documents.
            - If no documents, use general job/applicant-type questions instead.
            - Mix question types as the AI sees fit for the context:
              · Experience-based questions
              · Job competency questions
              · Problem-solving questions
              · Collaboration/conflict questions
              · Situational questions (must open with 1–2 sentences of realistic context before the hypothetical; starting directly with "If [situation]..." is prohibited)
              · Values/culture fit questions
              · For HARD: domain expertise verification questions

            [Difficulty Guidelines]
            EASY:
            - Warm, encouraging tone. Focus on motivations, role experience, and job fit.
            - No technical knowledge required. Avoid numbers/evidence, failure probes, or technical decisions.

            NORMAL:
            - Neutral tone. Probe the context, reasoning, and problem-solving behind experiences.
            - Applicants should explain their specific contribution and outcome.

            HARD:
            - Sharp, terse, pressured tone. No praise or empathy expressions.
            - Direct commands: "Walk me through X." / "Explain Y." rather than "Could you tell me about X?"
            - Use {jobRole}-specific terminology, methodologies, and framework names in questions.
            - If applicant type is NEW or INTERN, do not assume senior-level authority or decision-making.

            [Applicant Type Guidelines]
            NEW:
            - Focus on school projects, extracurriculars, competitions, team experiences.
            - Assess learning ability, potential, and problem-solving attitude over professional experience.

            INTERN:
            - Focus on basic understanding, attitude, communication, and growth potential.
            - Do not expect completed professional-level output.

            EXPERIENCED:
            - Probe actual results, decision rationale, problem-solving process, and org contribution.
            - May include metrics, failure cases, and trade-off comparisons.

            [Company Name Guidelines]
            - Well-known companies: naturally reflect publicly known business/service context.
            - Unknown or ambiguous companies: don't assert internal details — ask job-role-centric questions.

            [Interviewer Persona Reflection]
            - Reflect each interviewer's personality in their question tone.
            - Results-focused: prioritize performance, evidence, problem-solving.
            - People-focused: prioritize values, collaboration, motivation.
            - Pressure-type: ask sharply and concisely.
            - Friendly-type: use a gentle, guiding tone.

            [AI Competing Applicant Answer Rules]
            - Each applicant should answer based on specific experiences fitting their persona.
            - Minimum 100 words in English per answer.
            - Include situation, action, and result — not just abstract opinions.
            - Keep experiences consistent across all turns for the same applicant.

            [Follow-up Question Rules]
            - There are exactly 2 follow-up questions, placed naturally within questionSeq 2–9.
            - Follow-up target is always the USER. APPLICANTs do not answer follow-up questions.
            - The two follow-up questions must NOT appear consecutively.
            - Perspective extension:
              Must be placed immediately after the common question it references (next questionSeq).
              Only applicable when the referenced common question is about values, culture fit, or job attitude —
              NOT about a personal experience or specific story the APPLICANT shared.
              Briefly reference what the APPLICANT said, then ask "[userName], is there anything else that comes to mind?" style.
              Do NOT ask the USER to evaluate or react to the APPLICANT's answer.
            - Depth follow-up:
              Must be placed immediately after the USER individual question it references (next questionSeq).
              Ask for specific reasoning, method, or outcome based on the USER's previous individual answer.
            - Assign a new questionSeq to each follow-up question.

            [Diversity Rules]
            - Even with the same inputs, vary question order, angle, interviewer assignment, and APPLICANT answer examples across runs.
            - Avoid overused phrasing like "What was the hardest part?", "What role did you play?", "How did you solve it?" — use contextually specific wording instead.
            - Design a natural, unique interview flow for this generation.

            [Input Data]
            Room ID: {roomId}
            Applicant name: {userName}
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
		당신은 삼성, 네이버, 카카오, 현대자동차 등 국내 대기업 면접관과 채용 컨설턴트 경력을 가진 AI 면접 평가 전문가입니다.
		
		아래 면접 정보와 질문/답변 내용을 기반으로 지원자를 매우 객관적이고 상세하게 평가하세요.
		
		이 리포트는 사용자가 비용을 지불하고 받는 프리미엄 면접 분석 리포트입니다.
		따라서 단순한 칭찬이나 추상적인 피드백이 아니라, 실제 면접관이 남길 수준의 깊이 있는 분석을 제공해야 합니다.
		
		모든 평가는 반드시 실제 답변 내용을 근거로 작성하세요.
		
		답변 내용에 없는 내용을 추측하여 작성하지 마세요.
		
		좋았다, 아쉽다와 같은 추상적인 표현만 사용하는 것은 금지합니다.
		
		반드시
		"왜 그렇게 평가했는지",
		"어떤 답변 때문에 그런 점수를 주었는지",
		"어떻게 개선하면 되는지"
		까지 설명하세요.
		
		반드시 JSON만 응답하세요.
		
		JSON 외의 문자, 설명, 코드블록, 마크다운은 절대 출력하지 마세요.
		
		
		====================
		[면접 정보]
		====================
		
		회사 : {company}
		직무 : {job}
		지원 유형 : {type}
		난이도 : {difficulty}
		
		
		====================
		[AI 경쟁 지원자]
		====================
		
		{applicantList}
		
		
		====================
		[면접 질문 및 답변]
		====================
		
		(나 = 실제 사용자)
		(나머지 = AI 경쟁 지원자)
		{qaSection}
		
		
		====================
		[평가 기준]
		====================
		
		overallScore
		- 직무 전문성 30%
		- 논리적 사고력 20%
		- 커뮤니케이션 20%
		- 조직 적합성 15%
		- 압박 대응력 15%
		
		단순 평균이 아니라 실제 합격 가능성을 반영하여 계산한다.
		
		점수 기준
		
		95~100 : 즉시 합격 가능한 매우 뛰어난 수준
		90~94 : 매우 우수
		80~89 : 합격권
		70~79 : 평균 이상
		60~69 : 보완 필요
		40~59 : 개선 필요
		0~39 : 답변 부족 또는 무응답
		
		
		====================
		[평가 원칙]
		====================
		
		모든 평가는 실제 답변을 근거로 작성한다.
		답변 내용이 부족하면 부족한 이유를 설명한다.
		답변 내용이 좋다면 어떤 표현이 좋았는지 설명한다.
		AI 경쟁 지원자가 있는 경우 반드시 비교 분석을 수행한다.
		
		예시
		"2번 지원자보다 경험의 구체성이 높았다."
		"3번 지원자는 STAR 방식으로 설명했지만 사용자는 결과 중심 설명이 부족했다."
		처럼 반드시 비교하여 작성한다.
		
		
		
		====================
		[항목별 작성 규칙]
		====================
		
		overallScore
		
		0~100
		
		
		aiComment
		
		반드시 아래 내용을 모두 포함한다.
		
		1. 전체 면접 총평
		2. 가장 뛰어났던 강점 3가지
		3. 가장 부족했던 점 3가지
		4. 실제 면접관이 느꼈을 인상
		5. 채용 가능성 평가
		6. 다음 면접에서 가장 먼저 개선해야 할 행동
		
		최소 700자 이상 작성한다.
		
		
		compExpertise
		0~100
		
		
		compExpertiseDetail
		반드시 아래 내용을 포함한다.
		
		- 점수를 준 이유
		- 좋았던 답변 사례
		- 부족했던 답변 사례
		- 감점 요소
		- 어떻게 개선하면 높은 점수를 받을 수 있는지
		최소 250자 이상 작성한다.
		
		
		compLogicDetail
		
		반드시 아래 내용을 포함한다.
		- 답변 구조 분석
		- 논리 전개의 장점
		- 부족한 부분
		- 더 설득력 있게 말하는 방법
		최소 250자 이상 작성한다.
		
		
		compCommuDetail
		
		반드시 아래 내용을 포함한다.
		- 전달력
		- 표현의 명확성
		- 문장 구성
		- 듣는 사람 입장에서 이해하기 쉬웠는지
		- 개선 방법
		최소 250자 이상 작성한다.
		
		
		compCultureDetail
		반드시 아래 내용을 포함한다.
		- 조직 적합성
		- 협업 태도
		- 성장 가능성
		- 기업 문화와의 적합성
		- 개선 방법
		최소 250자 이상 작성한다.
		
		
		compPressureDetail
		반드시 아래 내용을 포함한다.
		- 어려운 질문 대응
		- 침착함
		- 답변 유지 능력
		- 부족했던 부분
		- 개선 방법
		최소 250자 이상 작성한다.
		
		
		speechWpm
		
		총 답변 어절 수를 총 답변 시간으로 나누어 추정한다.
		
		
		
		speechFiller
		
		"음"
		"어"
		"그"
		"저"
		"뭐"
		등의 필러 사용 횟수를 계산한다.
		
		
		
		====================
		지원자 분석
		====================
		
		모든 지원자를 포함한다.
		
		사용자는
		isUser=true
		personaId=null
		
		AI 지원자는
		isUser=false
		
		personaId=AI 경쟁 지원자 목록의 persona_id
		
		
		
		strength
		
		경쟁 지원자와 비교하여 가장 뛰어났던 점을 작성한다.
		최소 120자.
		
		
		
		weakness
		
		경쟁 지원자와 비교하여 부족했던 점과 개선 방법을 작성한다.
		최소 120자.
		
		
		
		====================
		다음 면접 체크리스트
		====================

		이번 면접 분석을 바탕으로 다음 면접 전에 반드시 준비해야 할 행동 항목 3~5개를 작성한다.

		- 구체적이고 실행 가능한 행동으로 작성한다.
		- "~하기" 형태로 끝나는 짧은 문장으로 작성한다.
		- 이번 면접에서 부족했던 점을 직접적으로 개선할 수 있는 항목 위주로 작성한다.

		예시
		"프로젝트 성과를 수치로 정리하기"
		"STAR 방식으로 답변 구조 연습하기"
		"자기소개를 1분 안에 말하는 연습하기"


		====================
		질문별 평가
		====================
		
		모든 질문을 포함한다.
		
		답변이 없는 경우
		
		score=0
		label="미흡"
		feedback에는 어떤 내용을 답했어야 하는지 작성한다.
		
		
		
		feedback
		반드시 아래 내용을 모두 포함한다.
		1. 좋았던 점
		2. 부족했던 점
		3. 면접관이라면 이어서 했을 후속 질문
		4. 더 좋은 답변 방향
		최소 180자 이상 작성한다.
		
		
		
		label
		
		90 이상 = 우수
		75 이상 = 양호
		60 이상 = 보통
		60 미만 = 미흡
		
		
		
		tags
		
		반드시 3~6개의 키워드를 쉼표로 구분하여 작성한다.
		
		예시
		
		논리성,구체성,STAR기법
		직무이해도,성과지표,협업
		문제해결,리더십,성장가능성
		
		
		
		====================
		출력 JSON
		====================
		
		{
		  "overallScore": number,
		  "aiComment": string,
		
		  "compExpertise": number,
		  "compExpertiseDetail": string,
		
		  "compLogic": number,
		  "compLogicDetail": string,
		
		  "compCommu": number,
		  "compCommuDetail": string,
		
		  "compCulture": number,
		  "compCultureDetail": string,
		
		  "compPressure": number,
		  "compPressureDetail": string,
		
		  "speechWpm": number,
		  "speechFiller": number,

		  "checklistItems": ["string", "string", "string"],

		  "applicants":[
		    {
		      "isUser":true,
		      "personaId":null,
		      "score":number,
		      "strength":string,
		      "weakness":string
		    },
		    {
		      "isUser":false,
		      "personaId":number,
		      "score":number,
		      "strength":string,
		      "weakness":string
		    }
		  ],
		
		  "questions":[
		    {
		      "questionSeq":number,
		      "participants":[
		        {
		          "turnRole":"USER",
		          "personaId":null,
		          "score":number,
		          "label":"우수",
		          "feedback":string,
		          "tags":string
		        },
		        {
		          "turnRole":"APPLICANT",
		          "personaId":number,
		          "score":number,
		          "label":"우수",
		          "feedback":string,
		          "tags":string
		        }
		      ]
		    }
		  ]
		}
		
		
		
		====================
		매우 중요
		====================
		
		- JSON 외의 어떠한 문자도 출력하지 않는다.
		- 모든 필드는 반드시 채운다.
		- 문자열은 줄바꿈(\n)을 포함해도 된다.
		- detail, feedback, aiComment는 절대로 한두 문장으로 끝내지 않는다.
		- 실제 대기업 면접관이 작성한 리포트처럼 깊이 있게 분석한다.
		- 점수와 피드백의 내용이 서로 모순되지 않아야 한다.
		- 근거 없는 칭찬이나 비판을 하지 않는다.
		- JSON 형식이 깨지지 않도록 반드시 유효한 JSON만 출력한다.
            """;

    // 프롬프트 생성 → OpenAI 호출 → 프롬프트 기록 저장 → 응답 파싱
    public InterviewStartResultDto generateScript(InterviewRoomDto room,
                                             List<AiInterviewerDto> interviewers,
                                             List<AiApplicantDto> applicants) {
        boolean isEnglish = "EN".equals(room.getLanguage());
        String prompt = buildPrompt(room, interviewers, applicants, isEnglish);
        String responseBody = callOpenAi(prompt,
                "당신은 모의면접 대본을 생성하는 AI입니다. 반드시 JSON 형식으로만 응답하세요.", 0.95);

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
        String template = isEnglish ? PROMPT_TEMPLATE_EN : PROMPT_TEMPLATE_KO;

        int seed = ThreadLocalRandom.current().nextInt(100000, 999999);
        String seedNote = isEnglish
                ? "\n\n[Variation Seed: " + seed + "]\nUse this seed to vary question angles, order, interviewer assignments, and applicant answer examples from previous runs."
                : "\n\n[이번 생성 시드: " + seed + "]\n이 시드를 참고하여 이전과 다른 질문 관점, 질문 순서, 면접관 배정, AI 지원자 사례를 선택하세요.";

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
                .replace("{userName}",            nvl(room.getUserName(), "지원자"))
                .replace("{interviewerPersonas}", formatInterviewers(interviewers))
                .replace("{applicantPersonas}",   formatApplicants(applicants))
                + seedNote;
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
