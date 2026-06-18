package com.ssafy.mvc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.mvc.dto.PostDto;

@Mapper
public interface PostDao {
    List<PostDto> selectAll(@Param("category") String category);
    List<PostDto> selectAllPaged(@Param("category") String category, @Param("keyword") String keyword,
                                 @Param("sort") String sort, @Param("offset") int offset, @Param("size") int size);
    int selectCount(@Param("category") String category, @Param("keyword") String keyword);
    List<PostDto> selectRelated(@Param("postId") Long postId, @Param("category") String category);
    PostDto selectById(@Param("postId") Long postId);
    void insert(PostDto dto);
    void update(PostDto dto);
    void delete(@Param("postId") Long postId);
    void incrementViews(@Param("postId") Long postId);
    void insertLike(@Param("userId") Long userId, @Param("postId") Long postId);
    void deleteLike(@Param("userId") Long userId, @Param("postId") Long postId);
    boolean existsLike(@Param("userId") Long userId, @Param("postId") Long postId);
    List<Long> selectLikedPostIds(@Param("userId") Long userId);
    List<PostDto> selectByUserId(@Param("userId") Long userId);
}
