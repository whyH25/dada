package com.ssafy.mvc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.mvc.dao.BookmarkDao;
import com.ssafy.mvc.dto.CustomUserDetailsDto;

@RestController
public class ScheduleController {

    private final BookmarkDao bookmarkDao;

    public ScheduleController(BookmarkDao bookmarkDao) {
        this.bookmarkDao = bookmarkDao;
    }

    /**
     * 로그인한 사용자의 북마크된 채용일정 ID 목록 반환.
     * 프론트에서 마운트 시 호출해 savedIds 초기화에 사용.
     */
    @GetMapping("/api/job-schedules/bookmarks")
    public ResponseEntity<?> getBookmarks(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        Long userId = userDetails.getUserDto().getUserId();
        List<Long> ids = bookmarkDao.selectScheduleIdsByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", ids));
    }

    /**
     * 멱등 토글: 북마크 있으면 삭제, 없으면 INSERT IGNORE로 추가.
     * 동시 중복 요청은 INSERT IGNORE가 흡수하므로 중복 행 발생 없음.
     */
    @PostMapping("/api/job-schedules/{id}/bookmark")
    public ResponseEntity<?> toggleBookmark(
            @PathVariable("id") Long scheduleId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        Long userId = userDetails.getUserDto().getUserId();
        boolean exists = bookmarkDao.existsByUserAndSchedule(userId, scheduleId);
        if (exists) {
            bookmarkDao.delete(userId, scheduleId);
            return ResponseEntity.ok(Map.of("success", true, "saved", false));
        } else {
            bookmarkDao.insert(userId, scheduleId);
            return ResponseEntity.ok(Map.of("success", true, "saved", true));
        }
    }
}
