package com.study.board_practice.exception;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException() {

        super("존재하는 id입니다."); // RuntimeException의 생성자에게 메시지를 전달
        // ↘ 고정 메시지로 설계한 이유: id 중복 검사 예외이므로 항상 같은 메시지를 반환하기 때문에 고정.
    }
}
