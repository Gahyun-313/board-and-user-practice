package com.study.board_practice.exception;

public class PostAccessDeniedException extends RuntimeException {
    public PostAccessDeniedException() {
        super("작성자만 수정할 수 있습니다.");
    }
}
