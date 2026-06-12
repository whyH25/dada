package com.ssafy.mvc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.mvc.dto.StoryDto;

@Mapper
public interface StoryDao {

    List<StoryDto> selectAll();
    StoryDto selectById(@Param("storyId") Long storyId);

    void insert(StoryDto dto);
    void update(StoryDto dto);
    void delete(@Param("storyId") Long storyId);

    void incrementViews(@Param("storyId") Long storyId);
    void incrementLikes(@Param("storyId") Long storyId);
    void decrementLikes(@Param("storyId") Long storyId);

    void insertLike(@Param("userId") Long userId, @Param("storyId") Long storyId);
    void deleteLike(@Param("userId") Long userId, @Param("storyId") Long storyId);
    boolean existsLike(@Param("userId") Long userId, @Param("storyId") Long storyId);
    List<Long> selectLikedStoryIds(@Param("userId") Long userId);
}
