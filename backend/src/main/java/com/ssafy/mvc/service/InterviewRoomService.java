package com.ssafy.mvc.service;

import com.ssafy.mvc.dto.InterviewRoomCreateResponse;
import com.ssafy.mvc.dto.JobCategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewRoomService {

    private final JdbcTemplate jdbc;
    private final DocumentParseService documentParseService;

    public List<JobCategoryDto> getJobCategories() {
        String sql = "SELECT job_id, job_code, job_name, sort_order FROM category_job ORDER BY sort_order";
        return jdbc.query(sql, (rs, row) -> new JobCategoryDto(
                rs.getInt("job_id"),
                rs.getString("job_code"),
                rs.getString("job_name"),
                rs.getInt("sort_order")
        ));
    }

    @Transactional
    public InterviewRoomCreateResponse createRoom(
            Long userId,
            String companyName,
            Integer jobId,
            String difficulty,
            Integer interviewerCount,
            Integer aiApplicantCount,
            MultipartFile resumeFile,
            MultipartFile portfolioFile) throws IOException {

        Long resumeId = null;
        Long portfolioId = null;

        // 1. 이력서 및 자기소개서 파싱 후 저장
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String parsedText = documentParseService.parseFile(resumeFile);
            resumeId = insertResume(userId, resumeFile.getOriginalFilename(), parsedText);
        }

        // 2. 포트폴리오 파싱 후 저장
        if (portfolioFile != null && !portfolioFile.isEmpty()) {
            String parsedText = documentParseService.parseFile(portfolioFile);
            portfolioId = insertPortfolio(userId, portfolioFile.getOriginalFilename(), parsedText);
        }

        // 3. 면접방 생성
        int hasAiApplicant = (aiApplicantCount != null && aiApplicantCount > 0) ? 1 : 0;
        Long roomId = insertInterviewRoom(userId, companyName, jobId, difficulty,
                interviewerCount, aiApplicantCount, hasAiApplicant, resumeId, portfolioId);

        return InterviewRoomCreateResponse.builder()
                .roomId(roomId)
                .resumeId(resumeId)
                .portfolioId(portfolioId)
                .message("면접방이 생성되었습니다.")
                .build();
    }

    private Long insertResume(Long userId, String fileName, String parsedText) {
        String sql = "INSERT INTO user_resume (user_id, title, file_name, parsed_text, is_primary) VALUES (?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, "이력서 및 자기소개서");
            ps.setString(3, fileName);
            ps.setString(4, parsedText);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private Long insertPortfolio(Long userId, String fileName, String parsedText) {
        String sql = "INSERT INTO user_portfolio (user_id, title, parsed_text) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, fileName);
            ps.setString(3, parsedText);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private Long insertInterviewRoom(Long userId, String companyName, Integer jobId,
            String difficulty, Integer interviewerCount, Integer aiApplicantCount,
            int hasAiApplicant, Long resumeId, Long portfolioId) {
        String sql = "INSERT INTO interview_room " +
                "(user_id, company_name, job_id, difficulty, interviewer_count, " +
                "ai_applicant_count, has_ai_applicant, resume_id, portfolio_id, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'READY')";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, companyName);
            if (jobId != null) ps.setInt(3, jobId); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, difficulty);
            ps.setInt(5, interviewerCount != null ? interviewerCount : 1);
            ps.setInt(6, aiApplicantCount != null ? aiApplicantCount : 0);
            ps.setInt(7, hasAiApplicant);
            if (resumeId != null) ps.setLong(8, resumeId); else ps.setNull(8, Types.BIGINT);
            if (portfolioId != null) ps.setLong(9, portfolioId); else ps.setNull(9, Types.BIGINT);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
