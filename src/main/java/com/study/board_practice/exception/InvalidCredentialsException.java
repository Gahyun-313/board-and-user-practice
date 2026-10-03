package com.study.board_practice.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {

        // 아이디 불일치', '비밀번호 불일치'를 묶은 이유: 공격자가 아이디 존재 여부를 알 수 있기 때문
        super("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}
