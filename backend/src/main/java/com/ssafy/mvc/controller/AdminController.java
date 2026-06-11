package com.ssafy.mvc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.mvc.dao.JobScheduleDao;
import com.ssafy.mvc.dao.UserDao;
import com.ssafy.mvc.dto.JobScheduleDto;
import com.ssafy.mvc.dto.UserDto;

@RestController
public class AdminController {

    private final UserDao userDao;
    private final JobScheduleDao jobScheduleDao;

    public AdminController(UserDao userDao, JobScheduleDao jobScheduleDao) {
        this.userDao = userDao;
        this.jobScheduleDao = jobScheduleDao;
    }

    // ── 공개 채용일정 조회 (비로그인 사용자도 캘린더 조회 가능) ──────

    @GetMapping("/api/job-schedules")
    public ResponseEntity<?> getPublicJobSchedules() {
        List<JobScheduleDto> schedules = jobScheduleDao.selectAll();
        return ResponseEntity.ok(Map.of("success", true, "data", schedules));
    }

    // ── 관리자 - 사용자 관리 ─────────────────────────────────────

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

    // ── 관리자 - 채용일정 관리 ────────────────────────────────────

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
}
