package com.ssafy.mvc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.mvc.dao.JobScheduleDao;
import com.ssafy.mvc.dao.NoticeDao;
import com.ssafy.mvc.dao.StoryDao;
import com.ssafy.mvc.dao.UserDao;
import com.ssafy.mvc.dto.AdminDto;
import com.ssafy.mvc.dto.AdminUserDetailsDto;
import com.ssafy.mvc.dto.JobScheduleDto;
import com.ssafy.mvc.dto.NoticeDto;
import com.ssafy.mvc.dto.StoryDto;
import com.ssafy.mvc.dto.UserDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class AdminController {

    private final UserDao userDao;
    private final JobScheduleDao jobScheduleDao;
    private final StoryDao storyDao;
    private final NoticeDao noticeDao;
    private final AuthenticationManager adminAuthManager;

    public AdminController(
            UserDao userDao,
            JobScheduleDao jobScheduleDao,
            StoryDao storyDao,
            NoticeDao noticeDao,
            @Qualifier("adminAuthManager") AuthenticationManager adminAuthManager) {
        this.userDao = userDao;
        this.jobScheduleDao = jobScheduleDao;
        this.storyDao = storyDao;
        this.noticeDao = noticeDao;
        this.adminAuthManager = adminAuthManager;
    }

    // ── 관리자 인증 ──────────────────────────────────────────────────

    @PostMapping("/api/admin/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            Authentication auth = adminAuthManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    body.get("adminEmail"), body.get("adminPwd"))
            );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            request.getSession(true); // 세션 생성

            AdminDto admin = ((AdminUserDetailsDto) auth.getPrincipal()).getAdminDto();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "adminId", admin.getAdminId(),
                    "adminEmail", admin.getAdminEmail(),
                    "adminName", admin.getAdminName()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "이메일 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    @GetMapping("/api/admin/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal AdminUserDetailsDto adminDetails) {
        if (adminDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        AdminDto admin = adminDetails.getAdminDto();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "adminId", admin.getAdminId(),
                "adminEmail", admin.getAdminEmail(),
                "adminName", admin.getAdminName()
            )
        ));
    }

    @PostMapping("/api/admin/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃 되었습니다."));
    }

    // ── 공개 채용일정 조회 ────────────────────────────────────────────

    @GetMapping("/api/job-schedules")
    public ResponseEntity<?> getPublicJobSchedules() {
        List<JobScheduleDto> schedules = jobScheduleDao.selectAll();
        return ResponseEntity.ok(Map.of("success", true, "data", schedules));
    }

    // ── 관리자 - 사용자 관리 ──────────────────────────────────────────

    @GetMapping("/api/admin/users")
    public ResponseEntity<?> getUsers() {
        List<UserDto> users = userDao.selectAllUsers();
        return ResponseEntity.ok(Map.of("success", true, "data", users));
    }

    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long userId) {
        userDao.deleteUser(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "사용자가 삭제되었습니다."));
    }

    // ── 관리자 - 채용일정 관리 ────────────────────────────────────────

    @GetMapping("/api/admin/job-schedules")
    public ResponseEntity<?> getJobSchedules() {
        List<JobScheduleDto> schedules = jobScheduleDao.selectAll();
        return ResponseEntity.ok(Map.of("success", true, "data", schedules));
    }

    @PostMapping("/api/admin/job-schedules")
    public ResponseEntity<?> createJobSchedule(@RequestBody JobScheduleDto schedule) {
        jobScheduleDao.insert(schedule);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "채용일정이 등록되었습니다.", "data", schedule));
    }

    @PutMapping("/api/admin/job-schedules/{id}")
    public ResponseEntity<?> updateJobSchedule(
            @PathVariable("id") Long scheduleId,
            @RequestBody JobScheduleDto schedule) {
        schedule.setScheduleId(scheduleId);
        jobScheduleDao.update(schedule);
        return ResponseEntity.ok(Map.of("success", true, "message", "채용일정이 수정되었습니다."));
    }

    @DeleteMapping("/api/admin/job-schedules/{id}")
    public ResponseEntity<?> deleteJobSchedule(@PathVariable("id") Long scheduleId) {
        jobScheduleDao.delete(scheduleId);
        return ResponseEntity.ok(Map.of("success", true, "message", "채용일정이 삭제되었습니다."));
    }

    // ── 관리자 - 합격스토리 관리 ──────────────────────────────────────

    @GetMapping("/api/admin/stories")
    public ResponseEntity<?> getStories() {
        return ResponseEntity.ok(Map.of("success", true, "data", storyDao.selectAll()));
    }

    @PostMapping("/api/admin/stories")
    public ResponseEntity<?> createStory(@RequestBody StoryDto story) {
        storyDao.insert(story);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "스토리가 등록되었습니다.", "data", story));
    }

    @PutMapping("/api/admin/stories/{id}")
    public ResponseEntity<?> updateStory(
            @PathVariable("id") Long storyId, @RequestBody StoryDto story) {
        story.setStoryId(storyId);
        storyDao.update(story);
        return ResponseEntity.ok(Map.of("success", true, "message", "스토리가 수정되었습니다."));
    }

    @DeleteMapping("/api/admin/stories/{id}")
    public ResponseEntity<?> deleteStory(@PathVariable("id") Long storyId) {
        storyDao.delete(storyId);
        return ResponseEntity.ok(Map.of("success", true, "message", "스토리가 삭제되었습니다."));
    }

    // ── 관리자 - 공지사항 관리 ────────────────────────────────────────

    @GetMapping("/api/admin/notices")
    public ResponseEntity<?> getNotices() {
        return ResponseEntity.ok(Map.of("success", true, "data", noticeDao.selectAll()));
    }

    @PostMapping("/api/admin/notices")
    public ResponseEntity<?> createNotice(@RequestBody NoticeDto notice) {
        noticeDao.insert(notice);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "공지사항이 등록되었습니다.", "data", notice));
    }

    @PutMapping("/api/admin/notices/{id}")
    public ResponseEntity<?> updateNotice(
            @PathVariable("id") Long noticeId, @RequestBody NoticeDto notice) {
        notice.setNoticeId(noticeId);
        noticeDao.update(notice);
        return ResponseEntity.ok(Map.of("success", true, "message", "공지사항이 수정되었습니다."));
    }

    @DeleteMapping("/api/admin/notices/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable("id") Long noticeId) {
        noticeDao.delete(noticeId);
        return ResponseEntity.ok(Map.of("success", true, "message", "공지사항이 삭제되었습니다."));
    }
}
