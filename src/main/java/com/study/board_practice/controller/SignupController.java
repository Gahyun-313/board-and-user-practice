package com.study.board_practice.controller;

import com.study.board_practice.dto.SignupRequest;
import com.study.board_practice.service.SignupService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController // HTTP 요청을 받아서 처리하고, 반환값을 그대로 응답 body에 담아주는 클래스라는 의미
public class SignupController {

    // 필드
    private final SignupService signupService;

    // 생성자
    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    // 메서드
    @PostMapping("/api/users/signup")
    public String signup(@RequestBody SignupRequest signupRequest) {
        signupService.signup(signupRequest);
        return "가입 완료";
    }
}
