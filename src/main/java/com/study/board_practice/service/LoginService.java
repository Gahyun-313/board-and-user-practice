package com.study.board_practice.service;

import com.study.board_practice.domain.User;
import com.study.board_practice.dto.LoginRequest;
import com.study.board_practice.exception.InvalidCredentialsException;
import com.study.board_practice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** 로그인 로직 서비스 */
@Service
public class LoginService {

    // 필드
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 생성자
    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 로그인 메서드
    public String login(LoginRequest loginRequest) {

        // 1. 유저 찾기 - 없으면 예외 던짐
        User user = userRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException());

        // 2. 비밀번호 일치 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return "로그인 성공";
    }
}
