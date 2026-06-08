package com.ssafy.mvc.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "다대다 통신 성공";
    }
}
