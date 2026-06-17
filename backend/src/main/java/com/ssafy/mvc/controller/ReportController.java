package com.ssafy.mvc.controller;

import com.ssafy.mvc.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/interview-rooms")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 면접 완료 후 AI 분석 리포트 생성 요청
    @PostMapping("/{roomId}/report")
    public ResponseEntity<Map<String, Object>> generateReport(@PathVariable Long roomId) {
        Long reportId = reportService.generateReport(roomId);
        return ResponseEntity.ok(Map.of("reportId", reportId, "success", true));
    }

    // 마이페이지 리포트 탭 전체 데이터 조회
    @GetMapping("/{roomId}/report")
    public ResponseEntity<?> getReport(@PathVariable Long roomId) {
        Map<String, Object> result = reportService.getFullReport(roomId);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
