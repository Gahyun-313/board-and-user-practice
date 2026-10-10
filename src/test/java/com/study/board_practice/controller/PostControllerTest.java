package com.study.board_practice.controller;

import com.study.board_practice.dto.PostCreateRequest;
import com.study.board_practice.dto.PostResponse;
import com.study.board_practice.exception.PostNotFoundException;
import com.study.board_practice.exception.UserNotFoundException;
import com.study.board_practice.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    // 필드
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    // 메서드
    @Test
    void 글_작성_성공() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest();
        request.setAuthorId(1);
        request.setTitle("title1");
        request.setContent("content1");

        when(postService.createPost(any(PostCreateRequest.class)))
                .thenReturn(10);

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("10"));
    }

    @Test
    void 유효하지_않은_작성자로_작성시_404_반환() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest();
        request.setAuthorId(1);
        request.setTitle("title1");
        request.setContent("content1");

        when(postService.createPost(any(PostCreateRequest.class)))
                .thenThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())   // 404인지 확인
                .andExpect(content().string("존재하지 않는 유저입니다.")); //문구 확인
    }

    @Test
    void 목록_조회_성공() throws Exception {
        // given
        PostResponse response1 = new PostResponse(1, "name1", "title1", "content1", LocalDateTime.now(), null);
        PostResponse response2 = new PostResponse(2, "name2", "title2", "content2", LocalDateTime.now(), null);

        when(postService.getPosts()).thenReturn(List.of(response1, response2));

        // when & then
        mockMvc.perform(get("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // 글 개수(배열 길이) 체크
                .andExpect(jsonPath("$[0].title").value("title1"))
                .andExpect(jsonPath("$[1].title").value("title2"));
    }

    @Test
    void 상세_조회_성공() throws Exception {
        // given
        PostResponse response = new PostResponse(2, "name1", "title1", "content1", LocalDateTime.now(), null);
        when(postService.getPost(2)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/posts/2")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title1"))
                .andExpect(jsonPath("$.content").value("content1"))
                .andExpect(jsonPath("$.authorNickname").value("name1"));

    }

    @Test
    void 상세_조회_글_없음() throws Exception {
        // given
        when(postService.getPost(1)).thenThrow(new PostNotFoundException());

        // when & then
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 글입니다."));

    }

}
