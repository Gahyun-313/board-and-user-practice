package com.study.board_practice.exception;

public class PostDeletedException extends RuntimeException {
    public PostDeletedException() {
        super("삭제된 글입니다.");
    }
}
