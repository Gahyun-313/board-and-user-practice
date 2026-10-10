package com.study.board_practice.dto;

import com.study.board_practice.domain.Post;

import java.time.LocalDateTime;

public class PostResponse {

    private final int id;
    private final String authorNickname;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PostResponse(
            int id,
            String authorNickname,
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.authorNickname = authorNickname;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Post 엔티티를 API 응답용 DTO로 바꾸는 변환 메서드
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthor().getNickname(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public int getId() { return id; }
    public String getAuthorNickname() { return authorNickname; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
