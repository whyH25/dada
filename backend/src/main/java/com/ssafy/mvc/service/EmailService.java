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
}
