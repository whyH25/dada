package com.ssafy.mvc.controller;

import com.ssafy.mvc.dao.CategoryDao;
import com.ssafy.mvc.dto.CategoryJobGroupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryDao categoryDao;

    // 직무 대분류 목록 + 각 대분류의 직무 목록 반환
    @GetMapping("/job-groups")
    public List<CategoryJobGroupDto> getJobGroups() {
        return categoryDao.selectAllGroupsWithJobs();
    }
}
