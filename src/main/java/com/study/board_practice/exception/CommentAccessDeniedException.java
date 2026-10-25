package com.study.board_practice.exception;

public class CommentAccessDeniedException extends RuntimeException {
    public CommentAccessDeniedException() {
        super("댓글 작성자만 수정/삭제할 수 있습니다.");
    }
}
