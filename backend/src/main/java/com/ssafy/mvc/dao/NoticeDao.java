package com.ssafy.mvc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.mvc.dto.NoticeDto;

@Mapper
public interface NoticeDao {

    List<NoticeDto> selectAll();
    NoticeDto selectById(@Param("noticeId") Long noticeId);
    NoticeDto selectLatestByCategory(@Param("category") String category);

    void insert(NoticeDto dto);
    void update(NoticeDto dto);
    void delete(@Param("noticeId") Long noticeId);

    void incrementViews(@Param("noticeId") Long noticeId);
}
