package com.study.board_practice.service;

import com.study.board_practice.domain.User;
import com.study.board_practice.dto.LoginRequest;
import com.study.board_practice.exception.InvalidCredentialsException;
import com.study.board_practice.repository.UserRepository;
import com.study.board_practice.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 로그인 로직 서비스
 */
@Service
public class LoginService {

    // 필드
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 생성자
    public LoginService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    // 로그인 메서드
    // 아이디/비밀번호 확인 후 토큰 반환
    public String login(LoginRequest loginRequest) {

        // 유저 찾기 - 없으면 예외 던짐
        User user = userRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(InvalidCredentialsException::new);

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // 토큰 생성
        return jwtProvider.createToken(user.getId());
    }
}
