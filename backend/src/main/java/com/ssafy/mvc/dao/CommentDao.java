package com.ssafy.mvc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.mvc.dto.CommentDto;

@Mapper
public interface CommentDao {
    List<CommentDto> selectByPostId(@Param("postId") Long postId);
    CommentDto selectById(@Param("commentId") Long commentId);
    void insert(CommentDto dto);
    void delete(@Param("commentId") Long commentId);
}
