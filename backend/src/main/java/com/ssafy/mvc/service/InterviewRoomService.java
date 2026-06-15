package com.ssafy.mvc.service;

import com.ssafy.mvc.dao.AiPersonaDao;
import com.ssafy.mvc.dao.InterviewRoomDao;
import com.ssafy.mvc.dao.InterviewRoomPersonaDao;
import com.ssafy.mvc.dao.InterviewScenarioDao;
import com.ssafy.mvc.dto.AiApplicantDto;
import com.ssafy.mvc.dto.AiInterviewerDto;
import com.ssafy.mvc.dto.InterviewRoomDto;
import com.ssafy.mvc.dto.InterviewRoomPersonaDto;
import com.ssafy.mvc.dto.InterviewScenarioDto;
import com.ssafy.mvc.dto.InterviewStartResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final AiPromptService aiPromptService;
    private final DocumentParseService documentParseService;

    // 서류 파일 텍스트 추출 후 면접방 생성 (resumeId/portfolioId는 현재 미사용으로 0 저장)
    public InterviewRoomDto createRoom(InterviewRoomDto dto, MultipartFile resumeFile, MultipartFile portfolioFile) throws IOException {
        String resumeText = (resumeFile != null && !resumeFile.isEmpty())
                ? documentParseService.parseFile(resumeFile) : "";
        String portfolioText = (portfolioFile != null && !portfolioFile.isEmpty())
                ? documentParseService.parseFile(portfolioFile) : "";

        dto.setResumeId(0L);
        dto.setPortfolioId(0L);
        dto.setResumeText(resumeText);
        dto.setPortfolioText(portfolioText);
        dto.setStatus("READY");

        interviewRoomDao.insertInterviewRoom(dto);
        return dto;
    }

    public List<AiInterviewerDto> getRandomInterviewers(int count) {
        return aiPersonaDao.selectRandomInterviewers(count);
    }

    public List<AiApplicantDto> getRandomApplicants(int count, String applicantType) {
        return aiPersonaDao.selectRandomApplicants(count, applicantType);
    }

    // 페르소나 선정 → DB 저장 → AI 대본 생성 → 시나리오 저장 → 상태 전환
    public InterviewStartResultDto startInterview(Long roomId) {
        InterviewRoomDto room = interviewRoomDao.selectByRoomId(roomId);

        List<AiInterviewerDto> interviewers = aiPersonaDao.selectRandomInterviewers(room.getAiInterviewerCnt());
        // aiApplicantCnt가 0이면 조회 생략
        List<AiApplicantDto> applicants = room.getAiApplicantCnt() > 0
                ? aiPersonaDao.selectRandomApplicants(room.getAiApplicantCnt(), room.getApplicantType())
                : new ArrayList<>();

        saveRoomPersonas(roomId, interviewers, applicants);

        InterviewStartResultDto script = aiPromptService.generateScript(room, interviewers, applicants);

        saveScenarios(roomId, script.getScenario());
        // batch INSERT는 생성된 ID를 반환하지 않으므로 재조회로 scenarioId 채움
        script.setScenario(interviewScenarioDao.selectByRoomId(roomId));
        script.setInterviewerPersonaIds(interviewers.stream().map(AiInterviewerDto::getInterviewerId).toList());
        script.setApplicantPersonaIds(applicants.stream().map(AiApplicantDto::getApplicantId).toList());
        script.setPersonaNames(buildPersonaNames(interviewers, applicants));

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

    public void updateStatus(Long roomId, String status) {
        interviewRoomDao.updateStatus(roomId, status);
    }

    private Map<Long, String> buildPersonaNames(List<AiInterviewerDto> interviewers, List<AiApplicantDto> applicants) {
        Map<Long, String> map = new LinkedHashMap<>();
        for (AiInterviewerDto i : interviewers) map.put(i.getInterviewerId(), i.getInterviewerName());
        for (AiApplicantDto a : applicants)     map.put(a.getApplicantId(),   a.getApplicantName());
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
