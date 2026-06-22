package com.ssafy.mvc.service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private record VerificationEntry(String code, LocalDateTime expiry, boolean verified) {}

    private final ConcurrentHashMap<String, VerificationEntry> store = new ConcurrentHashMap<>();
    private final JavaMailSender mailSender;
    private final Random random = new Random();

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendCode(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(email, new VerificationEntry(code, LocalDateTime.now().plusMinutes(5), false));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[다대다] 이메일 인증코드");
        message.setText("인증코드: " + code + "\n\n5분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        VerificationEntry entry = store.get(email);
        if (entry == null || LocalDateTime.now().isAfter(entry.expiry())) return false;
        if (!entry.code().equals(code)) return false;
        store.put(email, new VerificationEntry(code, entry.expiry(), true));
        return true;
    }

    public boolean isVerified(String email) {
        VerificationEntry entry = store.get(email);
        return entry != null && entry.verified();
    }

    public void clearCode(String email) {
        store.remove(email);
    }

    // 아이디/비밀번호 찾기에서 발급한 임시 비밀번호 안내 메일
    public void sendTempPasswordMail(String email, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[다대다] 임시 비밀번호 안내");
        message.setText("임시 비밀번호: " + tempPassword + "\n\n로그인 후 마이페이지에서 비밀번호를 변경해주세요.");
        mailSender.send(message);
    }

    // 면접 종료 1일 후 리포트 열람이 가능해졌을 때 보내는 안내 메일
    public void sendReportReadyMail(String email, String userName, String companyName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[다대다] 면접 리포트가 도착했어요");
        message.setText((userName != null && !userName.isBlank() ? userName : "회원") + "님,\n\n"
                + companyName + " 모의면접 리포트 분석이 완료되어 열람 가능합니다.\n"
                + "마이페이지 > 리포트에서 확인해보세요.");
        mailSender.send(message);
    }
}
