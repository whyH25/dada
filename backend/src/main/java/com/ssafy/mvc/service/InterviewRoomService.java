package com.ssafy.mvc.service;

import com.ssafy.mvc.dao.InterviewRoomDao;
import com.ssafy.mvc.dto.InterviewRoomDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class InterviewRoomService {

    private final InterviewRoomDao interviewRoomDao;
    private final DocumentParseService documentParseService;

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
}
