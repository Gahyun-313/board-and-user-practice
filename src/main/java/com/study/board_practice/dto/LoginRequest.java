package com.study.board_practice.dto;

public class LoginRequest {

    // 필드
    private String userId;
    private String password;

    // getter
    public String getUserId() { return userId; }
    public String getPassword() { return password; }

    // setter
    public void setUserId(String userId) { this.userId = userId; }
    public void setPassword(String password) { this.password = password; }
}
