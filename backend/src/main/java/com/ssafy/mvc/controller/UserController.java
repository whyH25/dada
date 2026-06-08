package com.ssafy.mvc.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.mvc.dto.UserDto;
import com.ssafy.mvc.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class UserController {

//    private final UserService userService;
//
//    public UserController(UserService userService) {
//        this.userService = userService;
//    }

    @Autowired
    private UserService userService;

    // 로그인
    @PostMapping("/api/users/login")
    public ResponseEntity<?> userLogin(@RequestBody UserDto loginRequest, HttpSession session) {
        String strEmail = loginRequest.getUserEmail();
        String strPwd = loginRequest.getUserPwd();

        UserDto user = userService.userLogin(strEmail, strPwd);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of(
				        "success", false,
				        "message", "이메일 또는 비밀번호가 일치하지 않습니다."
				));
        }

        session.setAttribute("loginUser", user);

        return ResponseEntity.ok(
			Map.of(
				"success", true,
				"message", "로그인 성공",
				"data", user
			)
        );
    }

    // 회원가입
    @PostMapping("/api/users/signup")
    public ResponseEntity<?> signup(@RequestBody UserDto signupRequest) {
        userService.signup(signupRequest);
        return ResponseEntity.ok(Map.of("success", true, "message", "회원가입이 완료되었습니다."));
    }

    // 세션 유저 조회
    @GetMapping("/api/users/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", user));
    }

    // 로그아웃
    @PostMapping("/api/users/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃 성공"));
    }
}
