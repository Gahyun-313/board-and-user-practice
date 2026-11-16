package com.study.board_practice.dto;

// 로그인 응답
// Jackson 이 getter 를 보고 {"token": "..."}로 변환
public class LoginResponse {

    private final String token;

    public LoginResponse(String token) { this.token = token; }

    public String getToken() { return token; }
}
