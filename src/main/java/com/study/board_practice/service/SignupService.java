package com.study.board_practice.service;

import com.study.board_practice.domain.User;
import com.study.board_practice.dto.SignupRequest;
import com.study.board_practice.exception.DuplicateUserException;
import com.study.board_practice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** 회원가입 비즈니스 로직 서비스 */
@Service    // Spring이 관리하는 Bean이라는 의미
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 생성자 주입 - Spring이 UserRepository 구현체를 자동으로 만들어서 넣어줌
    public SignupService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signup(SignupRequest signupRequest) {
        // 1. 중복 체크
        if (userRepository.existsByUserId(signupRequest.getUserId())) {
            throw new DuplicateUserException();
        }
                
        // 2. 중복이 아니면 새 User 만들어서 저장
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword()); // 비밀번호 암호화
        userRepository.save(new User(
                signupRequest.getUserId(), encodedPassword, signupRequest.getNickname()
        ));
    }
}
