package com.study.board_practice.controller;

import com.study.board_practice.dto.PostCreateRequest;
import com.study.board_practice.dto.PostResponse;
import com.study.board_practice.dto.PostUpdateRequest;
import com.study.board_practice.exception.PostAccessDeniedException;
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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
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

    @Test
    void 글_수정_성공() throws Exception {
        // given
        // request
        PostUpdateRequest request = new PostUpdateRequest();
        request.setAuthorId(1);
        request.setTitle("title1");
        request.setContent("content1");

        // response
        PostResponse response = new PostResponse(
                1, "name1", "title1",
                "content1", LocalDateTime.now(), null);

        when(postService.updatePost(eq(1), any(PostUpdateRequest.class)))
                .thenReturn(response);

        // when & then
        // put 요청 및 값 검증
        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title1"))
                .andExpect(jsonPath("$.content").value("content1"));
    }

    @Test
    void 글_수정_글_없음() throws Exception {
        // given
        PostUpdateRequest request = new PostUpdateRequest();
        request.setAuthorId(1);
        request.setTitle("title1");
        request.setContent("content1");

        // 파라미터 주의
        // 인자 중 하나라도 any()를 쓰면 나머지도 matcher(eq)로 써야 함
        // request는 Controller가 JSON으로 새로 만들기 때문에 any()로 받음
        when(postService.updatePost(eq(1), any(PostUpdateRequest.class)))
                .thenThrow(new PostNotFoundException());

        // when & then
        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 글입니다."));
    }

    @Test
    void 글_수정_작성자_불일치() throws Exception {
        // given
        PostUpdateRequest request = new PostUpdateRequest();
        request.setAuthorId(1);
        request.setTitle("title1");
        request.setContent("content1");

        when(postService.updatePost(eq(1), any(PostUpdateRequest.class)))
                .thenThrow(new PostAccessDeniedException());

        // when & then
        mockMvc.perform(put("/api/posts/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("작성자만 수정/삭제할 수 있습니다."));

    }

    @Test
    void 글_삭제_성공() throws Exception {
        // given 필요 없음 -> deletePost가 void인 mock은 아무 설정 없이도 예외 없이 지나가기 때문.

        mockMvc.perform(delete("/api/posts/5")
                .param("authorId", "3"))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).deletePost(5, 3);
    }

    @Test
    void 글_삭제_글_없음() throws Exception {
        // given
        // void 메서드가 예외를 던지게 함 -> doThrow().when(mock).메서드(인자)
        // when().thenThrow()를 못 쓰는 이유 -> service.deletePost(1, 1)의 반환값이 void이므로 식이 성립하지 않음
        doThrow(new PostNotFoundException())
                .when(postService).deletePost(1, 1);

        // when & then
        mockMvc.perform(delete("/api/posts/1").param("authorId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 글입니다."));
    }

    @Test
    void 글_삭제_작성자_불일치() throws Exception {
        // given
        doThrow(new PostAccessDeniedException())
                .when(postService).deletePost(1, 1);

        // when & then
        mockMvc.perform(delete("/api/posts/1").param("authorId", "1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("작성자만 수정/삭제할 수 있습니다."));

    }
}
