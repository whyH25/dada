package com.ssafy.mvc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.mvc.dao.StoryDao;
import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.StoryDto;

@RestController
public class StoryController {

    private final StoryDao storyDao;

    public StoryController(StoryDao storyDao) {
        this.storyDao = storyDao;
    }

    @GetMapping("/api/stories")
    public ResponseEntity<?> listStories(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        List<StoryDto> stories = storyDao.selectAll();
        if (userDetails != null) {
            Long userId = userDetails.getUserDto().getUserId();
            List<Long> likedIds = storyDao.selectLikedStoryIds(userId);
            stories.forEach(s -> s.setLiked(likedIds.contains(s.getStoryId())));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", stories));
    }

    @GetMapping("/api/stories/{id}")
    public ResponseEntity<?> getStory(
            @PathVariable("id") Long storyId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        StoryDto story = storyDao.selectById(storyId);
        if (story == null) {
            return ResponseEntity.notFound().build();
        }
        storyDao.incrementViews(storyId);
        story.setViews(story.getViews() + 1);
        if (userDetails != null) {
            story.setLiked(storyDao.existsLike(userDetails.getUserDto().getUserId(), storyId));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", story));
    }

    /** 로그인 사용자 좋아요 토글 */
    @PostMapping("/api/stories/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable("id") Long storyId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        Long userId = userDetails.getUserDto().getUserId();
        boolean already = storyDao.existsLike(userId, storyId);
        if (already) {
            storyDao.deleteLike(userId, storyId);
            storyDao.decrementLikes(storyId);
            return ResponseEntity.ok(Map.of("success", true, "liked", false));
        } else {
            storyDao.insertLike(userId, storyId);
            storyDao.incrementLikes(storyId);
            return ResponseEntity.ok(Map.of("success", true, "liked", true));
        }
    }
}
