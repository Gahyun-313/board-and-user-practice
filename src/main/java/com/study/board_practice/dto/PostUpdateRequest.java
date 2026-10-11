package com.study.board_practice.dto;

public class PostUpdateRequest {

    private int authorId;
    private String title;
    private String content;

    public int getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public void setContent(String content) { this.content = content; }
}
