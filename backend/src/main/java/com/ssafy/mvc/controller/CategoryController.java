package com.ssafy.mvc.controller;

import com.ssafy.mvc.dto.JobCategoryDto;
import com.ssafy.mvc.service.InterviewRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final InterviewRoomService interviewRoomService;

    @GetMapping("/jobs")
    public ResponseEntity<List<JobCategoryDto>> getJobCategories() {
        return ResponseEntity.ok(interviewRoomService.getJobCategories());
    }
}
