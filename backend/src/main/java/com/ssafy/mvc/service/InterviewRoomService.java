package com.ssafy.mvc.service;

import com.ssafy.mvc.dao.AiPersonaDao;
import com.ssafy.mvc.dao.InterviewPromptDao;
import com.ssafy.mvc.dao.InterviewRoomDao;
import com.ssafy.mvc.dao.InterviewRoomPersonaDao;
import com.ssafy.mvc.dao.InterviewScenarioDao;
import com.ssafy.mvc.dao.ReportDao;
import com.ssafy.mvc.dao.UserDao;
import com.ssafy.mvc.dao.UserPortfolioDao;
import com.ssafy.mvc.dao.UserResumeDao;
import com.ssafy.mvc.dto.AiApplicantDto;
import com.ssafy.mvc.dto.AiInterviewerDto;
import com.ssafy.mvc.dto.InterviewRoomDto;
import com.ssafy.mvc.dto.UserDto;
import com.ssafy.mvc.dto.InterviewRoomPersonaDto;
import com.ssafy.mvc.dto.InterviewScenarioDto;
import com.ssafy.mvc.dto.InterviewStartResultDto;
import com.ssafy.mvc.dto.UserDto;
import com.ssafy.mvc.dto.UserPortfolioDto;
import com.ssafy.mvc.dto.UserResumeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InterviewRoomService {

    private final InterviewRoomDao interviewRoomDao;
    private final AiPersonaDao aiPersonaDao;
    private final InterviewRoomPersonaDao interviewRoomPersonaDao;
    private final InterviewScenarioDao interviewScenarioDao;
    private final InterviewPromptDao interviewPromptDao;
    private final ReportDao reportDao;
    private final AiPromptService aiPromptService;
    private final UserResumeDao userResumeDao;
    private final UserPortfolioDao userPortfolioDao;
    private final UserDao userDao;

    // 마이페이지에 등록된 이력서/포트폴리오를 선택해 면접방 생성
    // resumeId/portfolioId가 가리키는 row의 parsed_text·file_name을 면접방에 스냅샷으로 복사
    public InterviewRoomDto createRoom(InterviewRoomDto dto) {
        if (dto.getResumeId() != null) {
            UserResumeDto resume = userResumeDao.selectById(dto.getResumeId());
            if (resume == null || !resume.getUserId().equals(dto.getUserId())) {
                throw new IllegalArgumentException("선택한 이력서를 찾을 수 없습니다.");
            }
            dto.setResumeFileName(resume.getFileName());
            dto.setResumeText(resume.getParsedText());
        }

        if (dto.getPortfolioId() != null) {
            UserPortfolioDto portfolio = userPortfolioDao.selectById(dto.getPortfolioId());
            if (portfolio == null || !portfolio.getUserId().equals(dto.getUserId())) {
                throw new IllegalArgumentException("선택한 포트폴리오를 찾을 수 없습니다.");
            }
            dto.setPortfolioFileName(portfolio.getFileName());
            dto.setPortfolioText(portfolio.getParsedText());
        }

        dto.setStatus("READY");
        interviewRoomDao.insertInterviewRoom(dto);
        return dto;
    }

    public List<AiInterviewerDto> getRandomInterviewers(int count, String language) {
        return aiPersonaDao.selectRandomInterviewers(count, language);
    }

    public List<AiApplicantDto> getRandomApplicants(int count, String applicantType, String language) {
        return aiPersonaDao.selectRandomApplicants(count, applicantType, language);
    }

    // 페르소나 선정 → DB 저장 → AI 대본 생성 → 시나리오 저장 → 상태 전환
    public InterviewStartResultDto startInterview(Long roomId, Long userId) {
        InterviewRoomDto room = interviewRoomDao.selectByRoomId(roomId);


        if (!room.getUserId().equals(userId))
                    throw new IllegalStateException("접근 권한이 없습니다.");

        UserDto user = userDao.selectUserById(userId);
        room.setUserName(user != null && user.getUserName() != null ? user.getUserName() : "지원자");


        if (userDao.getTicketCount(userId) <= 0)
            throw new IllegalStateException("티켓이 부족합니다. 충전 후 이용해주세요.");

        userDao.useTicket(userId);

        List<AiInterviewerDto> interviewers = aiPersonaDao.selectRandomInterviewers(room.getAiInterviewerCnt(), room.getLanguage());
        // aiApplicantCnt가 0이면 조회 생략
        List<AiApplicantDto> applicants = room.getAiApplicantCnt() > 0
                ? aiPersonaDao.selectRandomApplicants(room.getAiApplicantCnt(), room.getApplicantType(), room.getLanguage())
                : new ArrayList<>();

        saveRoomPersonas(roomId, interviewers, applicants);

        InterviewStartResultDto script = aiPromptService.generateScript(room, interviewers, applicants);

        saveScenarios(roomId, script.getScenario());
        // batch INSERT는 생성된 ID를 반환하지 않으므로 재조회로 scenarioId 채움
        script.setScenario(interviewScenarioDao.selectByRoomId(roomId));
        script.setInterviewerPersonaIds(interviewers.stream().map(AiInterviewerDto::getInterviewerId).toList());
        script.setApplicantPersonaIds(applicants.stream().map(AiApplicantDto::getApplicantId).toList());
        script.setPersonaNames(buildPersonaNames(applicants));
        script.setInterviewerStopVideos(buildInterviewerStopVideos(interviewers));
        script.setInterviewerMoveVideos(buildInterviewerMoveVideos(interviewers));
        script.setApplicantStopVideos(buildApplicantStopVideos(applicants));
        script.setApplicantMoveVideos(buildApplicantMoveVideos(applicants));

        interviewRoomDao.updateStatus(roomId, "IN_PROGRESS");
        return script;
    }

    // AI 응답 시나리오에 roomId, scenarioType 세팅 후 일괄 저장
    private void saveScenarios(Long roomId, List<InterviewScenarioDto> scenarios) {
        if (scenarios == null || scenarios.isEmpty()) return;
        for (InterviewScenarioDto s : scenarios) {
            s.setRoomId(roomId);
            s.setScenarioType("MAIN");
        }
        interviewScenarioDao.insertScenarios(scenarios);
    }

    public List<InterviewRoomDto> getRoomsByUserId(Long userId) {
        return interviewRoomDao.selectByUserId(userId);
    }

    public void updateStatus(Long roomId, String status) {
        interviewRoomDao.updateStatus(roomId, status);
    }

    // 마이페이지 면접 기록 삭제: interview_room과 그 자식 6개 테이블을 deleted_at만 채워 비활성화 (회원탈퇴 cascade와 동일 패턴)
    @Transactional
    public void deleteRoom(Long roomId, Long userId) {
        InterviewRoomDto room = interviewRoomDao.selectByRoomId(roomId);
        if (room == null || !room.getUserId().equals(userId)) {
            throw new IllegalStateException("접근 권한이 없습니다.");
        }

        List<Long> roomIds = List.of(roomId);
        interviewRoomDao.deactivateByRoomId(roomId);
        interviewScenarioDao.deactivateByRoomIds(roomIds);
        interviewRoomPersonaDao.deactivateByRoomIds(roomIds);
        interviewPromptDao.deactivateByRoomIds(roomIds);
        reportDao.deactivateReportsByRoomIds(roomIds);
        reportDao.deactivateReportApplicantsByRoomIds(roomIds);
        reportDao.deactivateReportQuestionsByRoomIds(roomIds);
    }

    private Map<Long, String> buildPersonaNames(List<AiApplicantDto> applicants) {
        Map<Long, String> map = new LinkedHashMap<>();
        for (AiApplicantDto a : applicants) map.put(a.getApplicantId(), a.getApplicantName());
        return map;
    }

    private Map<Long, String> buildInterviewerStopVideos(List<AiInterviewerDto> interviewers) {
        Map<Long, String> map = new LinkedHashMap<>();
        for (AiInterviewerDto i : interviewers) map.put(i.getInterviewerId(), i.getProfileImageUrl());
        return map;
    }

    private Map<Long, String> buildInterviewerMoveVideos(List<AiInterviewerDto> interviewers) {
        Map<Long, String> map = new LinkedHashMap<>();
        for (AiInterviewerDto i : interviewers) map.put(i.getInterviewerId(), i.getMoveVideoUrl());
        return map;
    }

    private Map<Long, String> buildApplicantStopVideos(List<AiApplicantDto> applicants) {
        Map<Long, String> map = new LinkedHashMap<>();
        for (AiApplicantDto a : applicants) map.put(a.getApplicantId(), a.getProfileImageUrl());
        return map;
    }

    private Map<Long, String> buildApplicantMoveVideos(List<AiApplicantDto> applicants) {
        Map<Long, String> map = new LinkedHashMap<>();
        for (AiApplicantDto a : applicants) map.put(a.getApplicantId(), a.getMoveVideoUrl());
        return map;
    }

    private void saveRoomPersonas(Long roomId, List<AiInterviewerDto> interviewers, List<AiApplicantDto> applicants) {
        List<InterviewRoomPersonaDto> list = new ArrayList<>();

        for (AiInterviewerDto i : interviewers) {
            InterviewRoomPersonaDto p = new InterviewRoomPersonaDto();
            p.setRoomId(roomId);
            p.setPersonaRole("INTERVIEWER");
            p.setPersonaId(i.getInterviewerId());
            list.add(p);
        }

        for (AiApplicantDto a : applicants) {
            InterviewRoomPersonaDto p = new InterviewRoomPersonaDto();
            p.setRoomId(roomId);
            p.setPersonaRole("APPLICANT");
            p.setPersonaId(a.getApplicantId());
            list.add(p);
        }

        if (!list.isEmpty()) {
            interviewRoomPersonaDao.insertRoomPersonas(list);
        }
    }
}
