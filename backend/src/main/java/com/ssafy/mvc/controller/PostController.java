package com.ssafy.mvc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.ssafy.mvc.dao.CommentDao;
import com.ssafy.mvc.dao.PostDao;
import com.ssafy.mvc.dto.CommentDto;
import com.ssafy.mvc.dto.CustomUserDetailsDto;
import com.ssafy.mvc.dto.PostDto;

@RestController
public class PostController {

    private final PostDao postDao;
    private final CommentDao commentDao;

    public PostController(PostDao postDao, CommentDao commentDao) {
        this.postDao = postDao;
        this.commentDao = commentDao;
    }

    @GetMapping("/api/posts")
    public ResponseEntity<?> listPosts(
            @RequestParam(value = "category", required = false) String category,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        List<PostDto> posts = postDao.selectAll(category);
        if (userDetails != null) {
            Long userId = userDetails.getUserDto().getUserId();
            List<Long> likedIds = postDao.selectLikedPostIds(userId);
            posts.forEach(p -> p.setLiked(likedIds.contains(p.getPostId())));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", posts));
    }

    @GetMapping("/api/posts/{id}")
    public ResponseEntity<?> getPost(
            @PathVariable("id") Long postId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        PostDto post = postDao.selectById(postId);
        if (post == null) return ResponseEntity.notFound().build();
        postDao.incrementViews(postId);
        post.setViews(post.getViews() + 1);
        if (userDetails != null) {
            post.setLiked(postDao.existsLike(userDetails.getUserDto().getUserId(), postId));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", post));
    }

    @PostMapping("/api/posts")
    public ResponseEntity<?> createPost(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        PostDto dto = new PostDto();
        dto.setUserId(userDetails.getUserDto().getUserId());
        dto.setCategory((String) body.get("category"));
        dto.setTitle((String) body.get("title"));
        dto.setContent((String) body.get("content"));
        dto.setAnonymous(Boolean.TRUE.equals(body.get("anonymous")));
        postDao.insert(dto);
        return ResponseEntity.ok(Map.of("success", true, "postId", dto.getPostId()));
    }

    @PutMapping("/api/posts/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable("id") Long postId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        PostDto post = postDao.selectById(postId);
        if (post == null) return ResponseEntity.notFound().build();
        if (!post.getUserId().equals(userDetails.getUserDto().getUserId())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "수정 권한이 없습니다."));
        }
        post.setCategory(body.get("category"));
        post.setTitle(body.get("title"));
        post.setContent(body.get("content"));
        postDao.update(post);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/api/posts/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable("id") Long postId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        PostDto post = postDao.selectById(postId);
        if (post == null) return ResponseEntity.notFound().build();
        if (!post.getUserId().equals(userDetails.getUserDto().getUserId())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "삭제 권한이 없습니다."));
        }
        postDao.delete(postId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/api/posts/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable("id") Long postId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        Long userId = userDetails.getUserDto().getUserId();
        if (postDao.existsLike(userId, postId)) {
            postDao.deleteLike(userId, postId);
            return ResponseEntity.ok(Map.of("success", true, "liked", false));
        } else {
            postDao.insertLike(userId, postId);
            return ResponseEntity.ok(Map.of("success", true, "liked", true));
        }
    }

    @GetMapping("/api/posts/{id}/comments")
    public ResponseEntity<?> listComments(@PathVariable("id") Long postId) {
        return ResponseEntity.ok(Map.of("success", true, "data", commentDao.selectByPostId(postId)));
    }

    @PostMapping("/api/posts/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable("id") Long postId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        CommentDto dto = new CommentDto();
        dto.setPostId(postId);
        dto.setUserId(userDetails.getUserDto().getUserId());
        dto.setContent((String) body.get("content"));
        dto.setAnonymous(Boolean.TRUE.equals(body.get("anonymous")));
        commentDao.insert(dto);
        return ResponseEntity.ok(Map.of("success", true, "commentId", dto.getCommentId()));
    }

    @DeleteMapping("/api/posts/{id}/comments/{cid}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("id") Long postId,
            @PathVariable("cid") Long commentId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        CommentDto comment = commentDao.selectById(commentId);
        if (comment == null) return ResponseEntity.notFound().build();
        if (!comment.getUserId().equals(userDetails.getUserDto().getUserId())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "삭제 권한이 없습니다."));
        }
        commentDao.delete(commentId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
