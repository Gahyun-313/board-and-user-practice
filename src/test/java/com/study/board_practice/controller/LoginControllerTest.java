package com.study.board_practice.controller;

import com.study.board_practice.dto.LoginRequest;
import com.study.board_practice.exception.InvalidCredentialsException;
import com.study.board_practice.service.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {
    // 필드
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginService loginService;

    // 메서드
    @Test
    void 로그인_요청_성공() throws Exception {
        /** given - loginService.login()이 성공 결과를 리턴하도록 mock */
        LoginRequest request = new LoginRequest();
        request.setUserId("testId");
        request.setPassword("1234");

        // any()로 감싼 이유
        // - login(request)로 request 객체를 그대로 넘기면,
        //   실제 호출 시 Jackson이 새로 만든 인스턴스랑 달라서 equals()가 false가 됨
        //   (테스트에서 만든 request를 컨트롤러가 Jacson으로 다시 객체화 -> 새로운 인스턴스가 생성되기 때문)
        // - any(LoginRequest.class)로 넘기면 어떤 인스턴스든 매칭되므로 이 에러를 피할 수 있음.
        when(loginService.login(any(LoginRequest.class))).thenReturn("로그인 성공");

        /** when & then - POST 요청 보내고 상태코드 + 응답 바디 확인 */
        mockMvc.perform(post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())

                // login() 메서드는 String을 리턴함 -> 응답 타입 - text/plain, 바디 - 순수 텍스트(따옴표 없음).
                // - content.json(~)은 응답 바디를 JSON으로 파싱해서 비교하는 매처이므로 content.string() 사용
                .andExpect(content().string("로그인 성공"));
    }

    @Test
    void 로그인_인증불일치시_401_반환() throws Exception {
        /** given - loginService.login()이 InvalidCredentialsException을 던지도록 mock */
        LoginRequest request = new LoginRequest();
        request.setUserId("testId");
        request.setPassword("1234");

        when(loginService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        /** when & then - POST 요청 보내고 상태코드 + 응답 바디 확인 */
        mockMvc.perform(post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
