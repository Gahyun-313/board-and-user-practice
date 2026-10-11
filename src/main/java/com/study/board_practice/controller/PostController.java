package com.study.board_practice.controller;

import com.study.board_practice.domain.Post;
import com.study.board_practice.dto.PostCreateRequest;
import com.study.board_practice.dto.PostResponse;
import com.study.board_practice.dto.PostUpdateRequest;
import com.study.board_practice.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 글 작성
    @PostMapping("/api/posts")
    public ResponseEntity<Integer> createPost(
            @RequestBody PostCreateRequest request) {
        // 글 작성 응답은 "방금 만든 글의 번호"만 주고, 클라이언트가 내용을 보고 싶을 경우 GET /api/posts/{id}로 조회
        // Post 자체를 응답으로 내려주면 민감정보가 노출될 수 있기 때문에 id만 리턴
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createPost(request));
    }

    // 목록 조회
    @GetMapping("/api/posts")
    public ResponseEntity<List<PostResponse>> getPosts() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getPosts());
    }

    // 글 상세 조회
    @GetMapping("/api/posts/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable int id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getPost(id));
    }

    // 글 수정
    @PutMapping("/api/posts/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable int id, @RequestBody PostUpdateRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.updatePost(id, request));
    }

    // 글 삭제
    @DeleteMapping("/api/posts/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable int id, @RequestParam int authorId) {
        postService.deletePost(id, authorId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build(); // 응답 바디가 없을 때는 build() 사용
    }
}
