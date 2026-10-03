package com.study.board_practice.service;

import com.study.board_practice.domain.User;
import com.study.board_practice.dto.LoginRequest;
import com.study.board_practice.exception.InvalidCredentialsException;
import com.study.board_practice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Spring 없이 순수 Mockito로 테스트
class LoginServiceTest {

    // 필드
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    // @Mock이 아니라 @InjectMocks인 이유
    // - loginService는 테스트 대상임 (가짜로 대체할 대상이 아님)
    // - Mock으로 설정할 경우, 호출해도 진짜 로직이 하나도 안 돌아가고 빈 껍데기만 호출
    private LoginService loginService;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * [ assertThat ]
     *      - 일반적인 값/객체 검증용. (정상 흐름).
     *      - 메서드 리턴값이나 어떤 객체를 받아서 그 상태를 체이닝으로 확인
     * [ assertThatThrownBy ]
     *      - 이 코드(람다)를 실행하면 예외가 떠져야 한다를 검증. (예외 흐름).
     *      - 람다를 인자로 받아서 그 블록을 실행했을 때 예외가 터지는지, 어떤 예외인지, 메시지가 뭔지까지 체이닝으로 검증
     */

    // 메서드
    @Test
    void 로그인_성공() {
        // given - 해당 아이디가 존재하고, 비밀번호가 일치함
        User user = new User("testId", "encodedPassword", "testUser1");

        LoginRequest request = new LoginRequest();
        request.setUserId("testId");
        request.setPassword("1234");

        when(userRepository.findByUserId("testId")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);

        // when - 로그인 메서드 호출
        String result = loginService.login(request);
        // then - 로그인 성공 메시지 리턴 확인
        assertThat(result).isEqualTo("로그인 성공");
    }

    @Test
    void 아이디가_존재하지_않음() {
        // given - 해당 아이디가 존재하지 않음
        LoginRequest request = new LoginRequest();
        request.setUserId("wrongId");
        request.setPassword("1234");

        when(userRepository.findByUserId("wrongId")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 비밀번호_불일치() {
        // given - 해당 아이디가 존재하지만, 비밀번호가 일치하지 않음
        User user = new User("testId", "encodedPassword", "testUser1");

        LoginRequest request = new LoginRequest();
        request.setUserId("testId");
        request.setPassword("0000");

        when(userRepository.findByUserId("testId")).thenReturn(Optional.of(user));
        // 비밀번호와 일치하는 값이 없음
        when(passwordEncoder.matches("0000", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}
