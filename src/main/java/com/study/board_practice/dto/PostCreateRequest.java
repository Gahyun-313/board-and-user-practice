package com.study.board_practice.dto;

public class PostCreateRequest {

    // authorId = User의 PK(int id), 로그인용 userId(String)와 다름
    // DTO는 id 숫자만 받고, User 조회/검증은 PostService에서 userReposioty.findById()로 처리
    private int authorId;

    private String title;
    private String content;

    // 생성자
    public int getAuthorId() { return authorId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }

    // 메서드
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}
