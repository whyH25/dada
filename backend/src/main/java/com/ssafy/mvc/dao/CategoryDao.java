package com.ssafy.mvc.dao;

import com.ssafy.mvc.dto.CategoryJobGroupDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryDao {

    // 대분류 + 소속 직무 목록을 한 번에 조회 (LEFT JOIN + resultMap collection)
    List<CategoryJobGroupDto> selectAllGroupsWithJobs();
}
