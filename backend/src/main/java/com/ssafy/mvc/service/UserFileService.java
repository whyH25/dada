package com.ssafy.mvc.service;

import com.ssafy.mvc.dao.UserPortfolioDao;
import com.ssafy.mvc.dao.UserResumeDao;
import com.ssafy.mvc.dto.UserFileType;
import com.ssafy.mvc.dto.UserPortfolioDto;
import com.ssafy.mvc.dto.UserResumeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 마이페이지 이력서/포트폴리오 등록·조회·삭제
// 테이블(DAO)은 RESUME/PORTFOLIO로 분리되어 있지만, 업로드 흐름이 동일해 서비스는 type으로 분기해 하나로 통합
@Service
@RequiredArgsConstructor
public class UserFileService {

    private final UserResumeDao userResumeDao;
    private final UserPortfolioDao userPortfolioDao;
    private final DocumentParseService documentParseService;
    private final GcsStorageService gcsStorageService;

    // signed URL 유효 시간 (목록 조회 시마다 새로 발급하므로 짧게 유지)
    private static final Duration SIGNED_URL_EXPIRY = Duration.ofMinutes(15);

    // 내 서류 목록 (다운로드용 signed URL 포함)
    public List<Map<String, Object>> getMyFiles(UserFileType type, Long userId) {
        if (type == UserFileType.RESUME) {
            return userResumeDao.selectByUserId(userId).stream()
                    .map(d -> toResponse(d.getResumeId(), d.getFileName(), d.getFileUrl(), d.getCreatedAt()))
                    .collect(Collectors.toList());
        }
        return userPortfolioDao.selectByUserId(userId).stream()
                .map(d -> toResponse(d.getPortfolioId(), d.getFileName(), d.getFileUrl(), d.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // 파일을 GCS에 저장하고 텍스트를 추출해 DB에 등록
    public Map<String, Object> upload(UserFileType type, Long userId, MultipartFile file) throws IOException {
        String parsedText = documentParseService.parseFile(file);
        // GCS 저장 경로: user/resume/, user/portfolio/
        String fileUrl = gcsStorageService.upload("user/" + type.name().toLowerCase(), file);

        if (type == UserFileType.RESUME) {
            UserResumeDto dto = new UserResumeDto();
            dto.setUserId(userId);
            dto.setFileUrl(fileUrl);
            dto.setFileName(file.getOriginalFilename());
            dto.setParsedText(parsedText);
            userResumeDao.insertResume(dto);
            return toResponse(dto.getResumeId(), dto.getFileName(), dto.getFileUrl(), dto.getCreatedAt());
        }

        UserPortfolioDto dto = new UserPortfolioDto();
        dto.setUserId(userId);
        dto.setFileUrl(fileUrl);
        dto.setFileName(file.getOriginalFilename());
        dto.setParsedText(parsedText);
        userPortfolioDao.insertPortfolio(dto);
        return toResponse(dto.getPortfolioId(), dto.getFileName(), dto.getFileUrl(), dto.getCreatedAt());
    }

    // 본인 소유 서류만 삭제 허용, GCS 원본도 함께 제거
    public void delete(UserFileType type, Long userId, Long id) {
        if (type == UserFileType.RESUME) {
            UserResumeDto resume = userResumeDao.selectById(id);
            if (resume == null || !resume.getUserId().equals(userId)) {
                throw new IllegalArgumentException("이력서를 찾을 수 없습니다.");
            }
            userResumeDao.deleteByIdAndUserId(id, userId);
            gcsStorageService.delete(resume.getFileUrl());
            return;
        }

        UserPortfolioDto portfolio = userPortfolioDao.selectById(id);
        if (portfolio == null || !portfolio.getUserId().equals(userId)) {
            throw new IllegalArgumentException("포트폴리오를 찾을 수 없습니다.");
        }
        userPortfolioDao.deleteByIdAndUserId(id, userId);
        gcsStorageService.delete(portfolio.getFileUrl());
    }

    // 목록/업로드 응답을 RESUME·PORTFOLIO 공통 형태로 변환
    private Map<String, Object> toResponse(Long id, String fileName, String fileUrl, LocalDateTime createdAt) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", id);
        view.put("fileName", fileName);
        view.put("downloadUrl", gcsStorageService.generateSignedUrl(fileUrl, SIGNED_URL_EXPIRY));
        view.put("createdAt", createdAt);
        return view;
    }
}
