package com.study.board_practice.dto;

public class SignupRequest {

    // 필드
    private String userId;
    private String password;
    private String nickname;

    // getter
    public String getUserId() { return userId; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }

    // setter
    public void setUserId(String userId) { this.userId = userId; }
    public void setPassword(String password) { this.password = password; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
