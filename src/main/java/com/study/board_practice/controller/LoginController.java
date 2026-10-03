package com.study.board_practice.controller;

import com.study.board_practice.dto.LoginRequest;
import com.study.board_practice.service.LoginService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    // 필드
    private final LoginService loginService;

    // 생성자
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    // 메서드
    @PostMapping("/api/users/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        return loginService.login(loginRequest);
    }

}
