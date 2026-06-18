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
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        int offset = (page - 1) * size;
        List<PostDto> posts = postDao.selectAllPaged(category, keyword, sort, offset, size);
        int total = postDao.selectCount(category, keyword);

        if (userDetails != null) {
            Long userId = userDetails.getUserDto().getUserId();
            List<Long> likedIds = postDao.selectLikedPostIds(userId);
            posts.forEach(p -> p.setLiked(likedIds.contains(p.getPostId())));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", posts, "total", total, "page", page, "size", size));
    }

    @GetMapping("/api/posts/{id}")
    public ResponseEntity<?> getPost(
            @PathVariable("id") Long postId,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        PostDto post = postDao.selectById(postId);
        if (post == null) return ResponseEntity.status(404).body(Map.of("success", false, "message", "게시글을 찾을 수 없습니다."));
        postDao.incrementViews(postId);
        post.setViews(post.getViews() + 1);
        if (userDetails != null) {
            post.setLiked(postDao.existsLike(userDetails.getUserDto().getUserId(), postId));
        }
        List<PostDto> related = postDao.selectRelated(postId, post.getCategory());
        return ResponseEntity.ok(Map.of("success", true, "data", post, "related", related));
    }

    @GetMapping("/api/posts/mine")
    public ResponseEntity<?> listMyPosts(@AuthenticationPrincipal CustomUserDetailsDto userDetails) {
        Long userId = userDetails.getUserDto().getUserId();
        List<PostDto> posts = postDao.selectByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", posts, "total", posts.size()));
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
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        PostDto post = postDao.selectById(postId);
        if (post == null) return ResponseEntity.notFound().build();
        if (!post.getUserId().equals(userDetails.getUserDto().getUserId())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "수정 권한이 없습니다."));
        }
        post.setCategory((String) body.get("category"));
        post.setTitle((String) body.get("title"));
        post.setContent((String) body.get("content"));
        post.setAnonymous(Boolean.TRUE.equals(body.get("anonymous")));
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

        if (postDao.selectById(postId) == null)
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "게시글을 찾을 수 없습니다."));

        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "댓글 내용을 입력하세요."));

        CommentDto dto = new CommentDto();
        dto.setPostId(postId);
        dto.setUserId(userDetails.getUserDto().getUserId());
        dto.setContent(content.trim());
        dto.setAnonymous(Boolean.TRUE.equals(body.get("anonymous")));
        commentDao.insert(dto);
        return ResponseEntity.ok(Map.of("success", true, "commentId", dto.getCommentId()));
    }

    @PutMapping("/api/posts/{id}/comments/{cid}")
    public ResponseEntity<?> updateComment(
            @PathVariable("id") Long postId,
            @PathVariable("cid") Long commentId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetailsDto userDetails) {

        CommentDto comment = commentDao.selectById(commentId);
        if (comment == null) return ResponseEntity.notFound().build();
        if (!comment.getUserId().equals(userDetails.getUserDto().getUserId()))
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "수정 권한이 없습니다."));

        String content = body.get("content");
        if (content == null || content.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "댓글 내용을 입력하세요."));

        comment.setContent(content.trim());
        comment.setAnonymous("true".equals(body.get("anonymous")));
        commentDao.update(comment);
        return ResponseEntity.ok(Map.of("success", true));
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
