package com.ssafy.mvc.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.UserDto;
import com.ssafy.mvc.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/api/users/login")
    public ResponseEntity<?> userLogin(@RequestBody UserDto loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUserEmail(), loginRequest.getUserPwd())
            );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            CustomUserDetailsDto userDetails = (CustomUserDetailsDto) authentication.getPrincipal();
            UserDto user = userDetails.getUserDto();
            user.setUserPwd(null);

            return ResponseEntity.ok(Map.of("success", true, "message", "로그인 성공", "data", user));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "이메일 또는 비밀번호가 일치하지 않습니다."));
        }
    }

    @PostMapping("/api/users/signup")
    public ResponseEntity<?> signup(@RequestBody UserDto signupRequest) {
        userService.signup(signupRequest);
        return ResponseEntity.ok(Map.of("success", true, "message", "회원가입이 완료되었습니다."));
    }

    @GetMapping("/api/users/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        UserDto user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("success", true, "data", user));
    }

    @PatchMapping("/api/users/me")
    public ResponseEntity<?> updateMe(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails,
            @RequestBody UserDto updateRequest) {
        updateRequest.setUserId(userDetails.getUserDto().getUserId());
        userService.updateUser(updateRequest);
        UserDto updated = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("success", true, "message", "회원정보가 수정되었습니다.", "data", updated));
    }

    // 회원정보 수정 전 본인 확인 (현재 비밀번호 일치 여부만 검증, 수정은 별도 PATCH에서 처리)
    @PostMapping("/api/users/me/verify-password")
    public ResponseEntity<?> verifyPassword(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails,
            @RequestBody Map<String, String> body) {
        boolean matches = userService.verifyPassword(userDetails.getUserDto(), body.get("password"));
        if (!matches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "비밀번호가 일치하지 않습니다."));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/api/users/me")
    public ResponseEntity<?> deleteMe(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails,
            HttpSession session, HttpServletResponse response) {
        userService.deleteUser(userDetails.getUserDto().getUserId());
        SecurityContextHolder.clearContext();
        session.invalidate();
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("success", true, "message", "회원탈퇴가 완료되었습니다."));
    }

    @PostMapping("/api/users/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃 성공"));
    }
}
