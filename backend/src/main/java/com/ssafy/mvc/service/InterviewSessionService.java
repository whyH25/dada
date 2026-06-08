package com.ssafy.mvc.service;

import com.ssafy.mvc.dto.ScenarioDto;
import com.ssafy.mvc.dto.SessionStartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionService {

    private final JdbcTemplate        jdbc;
    private final AiQuestionService    aiQuestionService;

    @Transactional
    public SessionStartResponse startSession(Long roomId, Long userId) {

        // 1. 면접방 정보 조회
        Map<String, Object> room = jdbc.queryForMap("""
                SELECT r.company_name, r.difficulty, r.resume_id, r.portfolio_id,
                       j.job_name
                FROM   interview_room r
                LEFT   JOIN category_job j ON r.job_id = j.job_id
                WHERE  r.room_id = ?
                """, roomId);

        String companyName  = (String) room.get("company_name");
        String difficulty   = (String) room.get("difficulty");
        String jobName      = (String) room.get("job_name");
        Long   resumeId     = toLong(room.get("resume_id"));
        Long   portfolioId  = toLong(room.get("portfolio_id"));

        // 2. 이력서 / 포트폴리오 텍스트 조회
        String resumeText    = fetchParsedText("user_resume",    "resume_id",    resumeId);
        String portfolioText = fetchParsedText("user_portfolio", "portfolio_id", portfolioId);

        // 3. 세션 생성
        Long sessionId = insertSession(userId, roomId);
        log.info("interview_session 생성 완료: sessionId={}", sessionId);

        // 4. OpenAI로 질문 생성
        List<AiQuestionService.GeneratedQuestion> questions =
                aiQuestionService.generateQuestions(companyName, jobName, difficulty, resumeText, portfolioText);
        log.info("질문 {}개 생성 완료", questions.size());

        // 5. interview_scenario 일괄 INSERT
        List<ScenarioDto> scenarios = insertScenarios(sessionId, questions);

        return new SessionStartResponse(sessionId, scenarios);
    }

    // ── private helpers ──────────────────────────────────────────

    private Long insertSession(Long userId, Long roomId) {
        String sql = """
                INSERT INTO interview_session (user_id, room_id, started_at, status)
                VALUES (?, ?, NOW(), 'IN_PROGRESS')
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, roomId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private List<ScenarioDto> insertScenarios(Long sessionId,
                                               List<AiQuestionService.GeneratedQuestion> questions) {
        String sql = """
                INSERT INTO interview_scenario
                    (session_id, question_type, question_text, question_order, is_asked)
                VALUES (?, ?, ?, ?, 0)
                """;

        List<ScenarioDto> result = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            AiQuestionService.GeneratedQuestion q = questions.get(i);
            int order = i + 1;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, sessionId);
                ps.setString(2, q.type());
                ps.setString(3, q.text());
                ps.setInt(4, order);
                return ps;
            }, keyHolder);

            result.add(new ScenarioDto(
                    keyHolder.getKey().longValue(),
                    q.type(),
                    q.text(),
                    order
            ));
        }
        return result;
    }

    private String fetchParsedText(String table, String idColumn, Long id) {
        if (id == null) return "";
        try {
            String text = jdbc.queryForObject(
                    "SELECT parsed_text FROM " + table + " WHERE " + idColumn + " = ?",
                    String.class, id);
            return text != null ? text : "";
        } catch (Exception e) {
            return "";
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        return null;
    }
}
