package com.study.board_practice.dto;

public class CommentUpdateRequest {

    private int authorId;
    private String content;

    // postId는 넣지 않음 -> commentId로 댓글을 찾으면 어느 글에 달렸는지는 DB에 이미 있음
    // (body에도 넣으면 실수로라도 경로와 값이 다를 수 있어 애초에 받지 않음.)

    // getter
    public int getAuthorId() { return authorId; }
    public String getContent() { return content; }

    // setter
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public void setContent(String content) { this.content = content; }

}
