package com.ssafy.mvc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.mvc.dao.NoticeDao;
import com.ssafy.mvc.dto.NoticeDto;

@RestController
public class NoticeController {

    private final NoticeDao noticeDao;

    public NoticeController(NoticeDao noticeDao) {
        this.noticeDao = noticeDao;
    }

    @GetMapping("/api/notices")
    public ResponseEntity<?> listNotices() {
        List<NoticeDto> notices = noticeDao.selectAll();
        return ResponseEntity.ok(Map.of("success", true, "data", notices));
    }

    @GetMapping("/api/notices/{id}")
    public ResponseEntity<?> getNotice(@PathVariable("id") Long noticeId) {
        NoticeDto notice = noticeDao.selectById(noticeId);
        if (notice == null) return ResponseEntity.notFound().build();
        noticeDao.incrementViews(noticeId);
        notice.setViews(notice.getViews() + 1);
        return ResponseEntity.ok(Map.of("success", true, "data", notice));
    }

    /** 합격스토리 배너용: 최신 이벤트 공지 반환 */
    @GetMapping("/api/notices/event-banner")
    public ResponseEntity<?> getEventBanner() {
        NoticeDto notice = noticeDao.selectLatestByCategory("이벤트");
        if (notice == null) return ResponseEntity.ok(Map.of("success", true, "data", (Object) null));
        return ResponseEntity.ok(Map.of("success", true, "data", notice));
    }
}
