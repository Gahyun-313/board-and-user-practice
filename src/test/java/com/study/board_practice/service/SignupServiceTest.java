package com.study.board_practice.service;

import com.study.board_practice.domain.User;
import com.study.board_practice.dto.SignupRequest;
import com.study.board_practice.exception.DuplicateUserException;
import com.study.board_practice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

/** Repository를 Mock으로 대체해서 Service 로직만 빠르게 검증하는 테스트 (-> DB 없이 동작) */
@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    // 필드
    @Mock // 가짜 구현체 만듦
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks // Mock Repository를 SignupService 생성자에 자동으로 주입해줌
    private SignupService signupService;

    // 메서드
    @Test
    void 회원가입_성공() {
        // given - "이 아이디는 존재하지 않는다"고 가정
        when(userRepository.existsByUserId("testId1")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("encodedPassword");

        SignupRequest request = new SignupRequest();
        request.setUserId("testId1");
        request.setPassword("1234");
        request.setNickname("testUser1");

        // when - 회원가입 메서드 호출
        signupService.signup(request);

        // then - save가 딱 1번 호출됐는지 확인 (저장 로직이 실제로 실행됐는지 검증)
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 중복_아이디면_예외발생() {
        // given - "이 아이디는 이미 존재한다"고 가정
        SignupRequest request = new SignupRequest();
        request.setUserId("testId1");
        request.setPassword("1234");
        request.setNickname("testUser1");
        when(userRepository.existsByUserId("testId1")).thenReturn(true);

        // when & then - signup 호출하면 RuntimeException이 터지는지 확인
        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(DuplicateUserException.class) // 터진 예외가 DuplicateUserException 타입인지 확인
                .hasMessage("존재하는 id입니다."); // 예외 메시지가 이 문자열과 일치하는지 확인

        // save는 절대 호출되면 안 됨 (중복이면 저장 안 하고 막아야 하니까)
        verify(userRepository, never()).save(any(User.class));
    }
}
