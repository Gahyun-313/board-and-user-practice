package com.study.board_practice.controller;

import com.study.board_practice.dto.SignupRequest;
import com.study.board_practice.exception.DuplicateUserException;
import com.study.board_practice.service.SignupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Controller 계층만 띄워서 실제 HTTP 요청/응답을 검증하는 테스트
@WebMvcTest(SignupController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 끄기 - 로그인 없이 가입 API 테스트
class SignupControllerTest {

    // 필드
    @Autowired
    private MockMvc mockMvc; // 실제 서버 안 띄우고 HTTP 요청을 가짜로 흉내내주는 도구

    @Autowired
    private ObjectMapper objectMapper; // 자바 객체 <-> JSON 문자열 변환해주는 Jackson 도구, Spring이 자동으로 Bean 등록해줌
    
    @MockitoBean // Controller가 의존하는 SignupService를 Mock으로 Bean 등록
    private SignupService signupService;

    // 메서드
    @Test
    void 회원가입_요청_성공() throws Exception {
        // given - signup 호출되면 아무것도 안 하고 그냥 통과하도록 설정
        SignupRequest request = new SignupRequest();
        request.setUserId("testId1");
        request.setPassword("1234");
        request.setNickname("testUser1");
        doNothing().when(signupService).signup(any(SignupRequest.class));

        // when & then - 실제 POST 요청 보내고 응답 검증
        mockMvc.perform(post("/api/users/signup")
                        //.param("userId", "testId1") // DTO 도입하기 전 방식
                        //.param("password", "1234")
                        //.param("nickname", "testUser1"))
                        .contentType(MediaType.APPLICATION_JSON) // 요청 body가 JSON 형식이라고 알림 (DTO 도입)
                        .content(objectMapper.writeValueAsString(request))) // request 객체를 JSON 문자열로 변환해서 body에 담음
                .andExpect(status().isOk()) // 응답이 200 OK 인지 확인
                .andExpect(content().string("가입 완료"));  // 응답 body가 이 문자열과 일치하는지 확인
    }

    @Test
    void 중복_아이디로_가입_요청시_409_반환() throws Exception {
        // given - signup 호출되면 DuplicationUserException을 던지도록 설정
        SignupRequest request = new SignupRequest();
        request.setUserId("testId1");
        request.setPassword("1234");
        request.setNickname("testUser1");
        doThrow(new DuplicateUserException()).when(signupService).signup(any(SignupRequest.class));

        // when & then - 409 Conflict랑 메시지가 오는지 확인
        mockMvc.perform(post("/api/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("존재하는 id입니다."));
    }
}