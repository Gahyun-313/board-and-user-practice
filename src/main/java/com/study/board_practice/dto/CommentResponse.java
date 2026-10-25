package com.study.board_practice.dto;

import com.study.board_practice.domain.Comment;

import java.time.LocalDateTime;

public class CommentResponse {

    private final int id;
    private final String authorNickname;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommentResponse(
            int id,
            String authorNickname,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.authorNickname = authorNickname;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getNickname(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public int getId() { return id; }
    public String getAuthorNickname() { return authorNickname; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
