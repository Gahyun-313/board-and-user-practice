package com.study.board_practice.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException() {
        super("존재하지 않는 글입니다.");
    }
}
