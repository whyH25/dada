package com.ssafy.mvc.service;

import com.ssafy.mvc.dao.ReportDao;
import com.ssafy.mvc.dto.ReportNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// 면접 종료(ended_at) + 1일이 지나 리포트 열람이 가능해진 회원에게 안내 메일을 보냄
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportNotificationScheduler {

    private final ReportDao reportDao;
    private final EmailService emailService;

    // 10분마다 대상자를 조회해 메일 발송 후 notified_at 기록 (중복 발송 방지)
    @Scheduled(fixedRate = 600000)
    public void notifyReadyReports() {
        log.info("[ReportNotificationScheduler] 실행됨");
        List<ReportNotificationDto> pending = reportDao.selectPendingNotifications();
        log.info("[ReportNotificationScheduler] 대상 {}건 조회됨", pending.size());
        for (ReportNotificationDto p : pending) {
            try {
                emailService.sendReportReadyMail(p.getUserEmail(), p.getUserName(), p.getCompanyName());
                reportDao.markNotified(p.getReportId());
            } catch (Exception e) {
                log.error("리포트 알림 메일 발송 실패 (reportId={}): {}", p.getReportId(), e.getMessage());
            }
        }
    }
}
