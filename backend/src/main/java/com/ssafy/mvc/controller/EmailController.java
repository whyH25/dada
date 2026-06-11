package com.ssafy.mvc.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.mvc.service.EmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> body) {
        emailService.sendCode(body.get("email"));
        return ResponseEntity.ok(Map.of("success", true, "message", "인증코드가 발송되었습니다."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        boolean result = emailService.verifyCode(body.get("email"), body.get("code"));
        if (!result) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "인증코드가 올바르지 않거나 만료되었습니다."));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "이메일 인증이 완료되었습니다."));
    }
}
