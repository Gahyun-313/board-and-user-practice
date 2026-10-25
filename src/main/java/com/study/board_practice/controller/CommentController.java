package com.study.board_practice.controller;

import com.study.board_practice.dto.CommentCreateRequest;
import com.study.board_practice.dto.CommentResponse;
import com.study.board_practice.dto.CommentUpdateRequest;
import com.study.board_practice.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 작성
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<Integer> createComment(
            @PathVariable int postId, @RequestBody CommentCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.createComment(postId, request));
    }

    // 특정 글에 달린 댓글 목록 조회
    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable int postId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.getComments(postId));
    }

    // 특정 댓글 조회
    @GetMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable int postId, @PathVariable int commentId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.getComment(commentId));
    }

    // 댓글 수정
    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable int commentId, @RequestBody CommentUpdateRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.updateComment(commentId, request));
    }

    // 댓글 삭제
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable int commentId, @RequestParam int authorId) {
        commentService.deleteComment(commentId, authorId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build(); // 응답 바디가 없을 떄는 build() 사용
    }
}
