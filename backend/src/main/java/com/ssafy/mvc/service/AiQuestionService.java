package com.ssafy.mvc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AiQuestionService {

    private final RestClient openAiRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.chat-path:/v1/chat/completions}")
    private String chatPath;

    private static final int MAX_RESUME_CHARS    = 3000;
    private static final int MAX_PORTFOLIO_CHARS = 2000;
    // 의미 있는 내용으로 판단하는 최소 글자 수
    private static final int MEANINGFUL_MIN_LEN  = 30;

    public record GeneratedQuestion(String type, String text) {}

    public List<GeneratedQuestion> generateQuestions(
            String companyName,
            String jobName,
            String difficulty,
            String resumeText,
            String portfolioText) {

        String prompt = buildPrompt(companyName, jobName, difficulty, resumeText, portfolioText);
        String responseBody = callOpenAi(prompt);
        return parseQuestions(responseBody);
    }

    private String buildPrompt(String companyName, String jobName, String difficulty,
                                String resumeText, String portfolioText) {
        String resume    = truncate(resumeText,    MAX_RESUME_CHARS);
        String portfolio = truncate(portfolioText, MAX_PORTFOLIO_CHARS);

        boolean hasResume    = isMeaningful(resume);
        boolean hasPortfolio = isMeaningful(portfolio);

        String dataSection;
        String questionGuide;

        if (hasResume && hasPortfolio) {
            // 둘 다 있는 경우: 이력서+포트폴리오 기반 개인 질문 포함
            dataSection = """
                    [이력서]
                    %s

                    [포트폴리오]
                    %s
                    """.formatted(resume, portfolio);
            questionGuide = """
                    - COMMON(공통 질문): 2~3개 (지원 동기, 자기소개, 직무 이해 등)
                    - PERSONAL(개인 질문): 5~6개 (이력서/포트폴리오의 실제 내용에 근거한 구체적인 질문. 반드시 제공된 이력서/포트폴리오 내용에 있는 사실만 언급할 것)
                    - FOLLOWUP(꼬리 질문): 1~2개 (PERSONAL 질문 중 하나에 대한 예상 꼬리 질문)
                    """;
        } else if (hasResume) {
            // 이력서만 있는 경우
            dataSection = """
                    [이력서]
                    %s

                    [포트폴리오] 미제출
                    """.formatted(resume);
            questionGuide = """
                    - COMMON(공통 질문): 3~4개 (지원 동기, 자기소개, 직무 이해, 팀워크 등)
                    - PERSONAL(개인 질문): 4~5개 (이력서의 실제 내용에 근거한 구체적인 질문. 반드시 제공된 이력서 내용에 있는 사실만 언급할 것. 포트폴리오는 없으므로 포트폴리오 관련 질문 생성 금지)
                    - FOLLOWUP(꼬리 질문): 1개 (PERSONAL 질문 중 하나에 대한 예상 꼬리 질문)
                    """;
        } else if (hasPortfolio) {
            // 포트폴리오만 있는 경우
            dataSection = """
                    [이력서] 미제출

                    [포트폴리오]
                    %s
                    """.formatted(portfolio);
            questionGuide = """
                    - COMMON(공통 질문): 3~4개 (지원 동기, 자기소개, 직무 이해, 목표 등)
                    - PERSONAL(개인 질문): 4~5개 (포트폴리오의 실제 내용에 근거한 구체적인 질문. 반드시 제공된 포트폴리오 내용에 있는 사실만 언급할 것. 이력서는 없으므로 이력서 관련 질문 생성 금지)
                    - FOLLOWUP(꼬리 질문): 1개 (PERSONAL 질문 중 하나에 대한 예상 꼬리 질문)
                    """;
        } else {
            // 둘 다 없는 경우: 직무/회사 기반 일반 질문만
            dataSection = """
                    [이력서] 미제출
                    [포트폴리오] 미제출
                    """;
            questionGuide = """
                    - 이력서와 포트폴리오가 없으므로 PERSONAL 질문은 생성하지 마세요.
                    - COMMON(공통 질문): 6~8개 (지원 동기, 자기소개, 직무 관련 지식, 협업 경험, 문제 해결 능력, 커리어 목표 등 일반적인 질문)
                    - FOLLOWUP(꼬리 질문): 2개 (COMMON 질문 중 하나에 대한 예상 꼬리 질문)
                    """;
        }

        return """
                [면접 정보]
                지원 회사: %s
                지원 직무: %s
                면접 난이도: %s

                %s
                총 8~10개의 면접 질문을 생성해주세요.
                %s
                주의사항:
                - 제공되지 않은 자료(이력서/포트폴리오)를 근거로 한 질문은 절대 생성하지 마세요.
                - 질문은 구체적이고 답변하기 적합한 형태로 작성하세요.

                반드시 아래 JSON 형식으로만 응답하세요:
                {"questions":[{"type":"COMMON","text":"질문 내용"},{"type":"PERSONAL","text":"질문 내용"},{"type":"FOLLOWUP","text":"질문 내용"}]}
                """.formatted(
                nvl(companyName, "미정"),
                nvl(jobName,     "미정"),
                nvl(difficulty,  "MEDIUM"),
                dataSection,
                questionGuide
        );
    }

    private boolean isMeaningful(String text) {
        if (text == null || text.isBlank()) return false;
        // 공백 제거 후 최소 글자 수 이상이어야 의미 있는 내용으로 판단
        return text.replaceAll("\\s+", "").length() >= MEANINGFUL_MIN_LEN;
    }

    private String callOpenAi(String userPrompt) {
        String requestBody = """
                {
                  "model": "gpt-4o",
                  "messages": [
                    {
                      "role": "system",
                      "content": "당신은 전문 채용 면접관입니다. 지원자의 이력서와 포트폴리오를 분석하여 심층 면접 질문을 생성합니다. 반드시 JSON 형식으로만 응답하세요."
                    },
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "response_format": { "type": "json_object" },
                  "temperature": 0.7
                }
                """.formatted(toJsonString(userPrompt));

        return openAiRestClient.post()
                .uri(chatPath)
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }

    private List<GeneratedQuestion> parseQuestions(String responseBody) {
        try {
            JsonNode root    = objectMapper.readTree(responseBody);
            String content   = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode qNodes  = objectMapper.readTree(content).path("questions");

            List<GeneratedQuestion> result = new ArrayList<>();
            for (JsonNode q : qNodes) {
                String type = q.path("type").asText("COMMON");
                String text = q.path("text").asText();
                if (!text.isBlank()) {
                    result.add(new GeneratedQuestion(type, text));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("AI 질문 생성 중 오류가 발생했습니다.", e);
        }
    }

    private String toJsonString(String text) {
        try {
            return objectMapper.writeValueAsString(text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) : text;
    }

    private String nvl(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
