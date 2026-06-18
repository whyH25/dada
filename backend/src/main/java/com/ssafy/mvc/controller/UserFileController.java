package com.ssafy.mvc.controller;

import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.UserFileType;
import com.ssafy.mvc.service.UserFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// 마이페이지 서류 관리: /api/user-files/resume, /api/user-files/portfolio 공통 처리
@RestController
@RequestMapping("/api/user-files/{type}")
@RequiredArgsConstructor
public class UserFileController {

    private final UserFileService userFileService;

    // 내 서류 목록 조회
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMyFiles(
            @PathVariable String type,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        Long userId = userDetails.getUserDto().getUserId();
        return ResponseEntity.ok(userFileService.getMyFiles(parseType(type), userId));
    }

    // 서류 업로드 (파일 저장 + 텍스트 파싱)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable String type,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails,
            @RequestParam MultipartFile file) throws IOException {
        Long userId = userDetails.getUserDto().getUserId();
        return ResponseEntity.ok(userFileService.upload(parseType(type), userId, file));
    }

    // 서류 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable String type,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        Long userId = userDetails.getUserDto().getUserId();
        userFileService.delete(parseType(type), userId, id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // URL의 소문자 type("resume"/"portfolio")을 enum으로 변환
    private UserFileType parseType(String type) {
        try {
            return UserFileType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 서류 종류입니다: " + type);
        }
    }
}
