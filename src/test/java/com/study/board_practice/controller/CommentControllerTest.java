package com.study.board_practice.controller;

import com.study.board_practice.dto.CommentCreateRequest;
import com.study.board_practice.dto.CommentResponse;
import com.study.board_practice.dto.CommentUpdateRequest;
import com.study.board_practice.exception.CommentAccessDeniedException;
import com.study.board_practice.exception.CommentNotFoundException;
import com.study.board_practice.exception.PostNotFoundException;
import com.study.board_practice.exception.UserNotFoundException;
import com.study.board_practice.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    // 필드
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    // ----------------------------------------------------------------
    // createComment
    // @PathVariable int postId, @RequestBody CommentCreateRequest request
    // ----------------------------------------------------------------
    @Test
    void 댓글_작성_성공() throws Exception {
        // given
        int authorId = 99;
        int postId = 33;

        // 요청 생성
        CommentCreateRequest request = new CommentCreateRequest();
        request.setAuthorId(authorId);
        request.setContent("Create comment!");

        when(commentService.createComment(eq(postId), any(CommentCreateRequest.class)))
                .thenReturn(10);

        // when
        mockMvc.perform(post("/api/posts/"+postId+"/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("10"));

        // then
        // 반환된 id 검증
        verify(commentService, times(1))
                .createComment(eq(postId), any(CommentCreateRequest.class));
    }

    @Test
    void 댓글_작성_실패_없는_글() throws Exception {
        // given
        int authorId = 99;
        int postId = 33;

        // 요청 생성
        CommentCreateRequest request = new CommentCreateRequest();
        request.setAuthorId(authorId);
        request.setContent("Create comment!");

        // PostNotFoundException 발생
        when(commentService.createComment(eq(postId), any(CommentCreateRequest.class)))
                .thenThrow(new PostNotFoundException());

        // ✅ any() 이유 : Spring이 JSON에서 새 객체를 만들기 때문에 특정 인스턴스로 매칭 불가
        /*
            Q. any()로 감싸지 않으면 404 아니라 201 되는 이유

            - 클라이언트 -> 서버로 보내는 건 텍스트뿐임
                -> Spring이 그 텍스트를 받아서 CommentCreateRequest 형태의 새 객체를 만들어줌 (<- @RequestBody가 하는 일)
            - MockMvc는 진짜 HTTP 요청이 온 것처럼 시뮬레이션함.
                -> 아래 mockMvc.perform에서 request의 텍스트를 진짜 요청처럼 처리해서 새 객체를 만듦.
            ➡️ 이 때문에 기존 request와 다른 새로운 request 인스턴스가 만들어지고,
                Mockito가 스텁 조건에 안 맞다고 판단해 예외를 안 던지고 기본값(0)을 반환함.
            ➡️ "아무 CommentCreateRequest 형태"에 대해 Exception이 발생하게 하기 위해 ** any **를 붙임

            + 인자 중 하나라도 any()를 쓰면 나머지도 matcher(eq)로 써야 하기 때문에 eq 사용
         */

        // when & then
        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // 에러 일치 확인
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 글입니다."));
    }

    @Test
    void 댓글_작성_실패_없는_유저() throws Exception {
        // given
        int authorId = 99;
        int postId = 33;

        // 요청 생성
        CommentCreateRequest request = new CommentCreateRequest();
        request.setAuthorId(authorId);
        request.setContent("Create comment");

        // UserNotFoundException 발생
        when(commentService.createComment(eq(postId), any(CommentCreateRequest.class)))
                .thenThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 유저입니다."));
    }

    // ----------------------------------------------------------------
    // getComments
    // @PathVariable int postId
    // ----------------------------------------------------------------
    @Test
    void 댓글_목록_조회_성공() throws Exception {
        // given
        int postId = 33;

        // 댓글 목록 생성
        CommentResponse response1 = new CommentResponse(11, "nickname1", "content1", null, null);
        CommentResponse response2 = new CommentResponse(12, "nickname2", "content2", null, null);

        when(commentService.getComments(postId)).thenReturn(List.of(response1, response2));

        // when & then
        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[1].id").value(12));

        verify(commentService, times(1)).getComments(postId);
    }

    @Test
    void 댓글_목록_조회_실패_없는_글() throws Exception {
        // given
        int postId = 33;

        when(commentService.getComments(postId)).thenThrow(new PostNotFoundException());

        // when & then
        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 글입니다."));
    }

    @Test
    void 댓글_목록_조회_빈_리스트() throws Exception {
        // given
        int postId = 33;

        // 빈 리스트 반환
        when(commentService.getComments(postId)).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ----------------------------------------------------------------
    // getComment
    // @PathVariable int postId, @PathVariable int commentId
    // ----------------------------------------------------------------
    @Test
    void 댓글_상세_조회_성공() throws Exception {
        // given
        int postId = 33;
        int commentId = 44;

        CommentResponse response = new CommentResponse(
                commentId, "Nickname", "content", null, null);

        when(commentService.getComment(commentId)).thenReturn(response);

        // when
        mockMvc.perform(get("/api/posts/"+postId+"/comments/"+commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.content").value("content"));

        // then
        verify(commentService, times(1)).getComment(commentId);
    }

    @Test
    void 댓글_상세_조회_실패_없는_댓글() throws Exception {
        // given
        int postId = 33;
        int commentId = 44;

        when(commentService.getComment(commentId)).thenThrow(new CommentNotFoundException());

        // when & then
        mockMvc.perform(get("/api/posts/"+postId+"/comments/"+commentId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 댓글입니다."));
    }

    // ----------------------------------------------------------------
    // updateComment
    // @PathVariable int commentId, @RequestBody CommentUpdateRequest request
    // ----------------------------------------------------------------
    @Test
    void 댓글_수정_성공() throws Exception {
        // given
        int commentId = 33;
        int authorId = 99;

        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setAuthorId(authorId);
        request.setContent("Update comment!");

        CommentResponse response = new CommentResponse(
                commentId, "Nickname", "Update comment!",
                null, null);

        when(commentService.updateComment(eq(commentId), any(CommentUpdateRequest.class)))
                .thenReturn(response);

        // when
        mockMvc.perform(put("/api/comments/"+commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.content").value("Update comment!"));

        // then
        verify(commentService, times(1))
                .updateComment(eq(commentId), any(CommentUpdateRequest.class));
    }

    @Test
    void 댓글_수정_실패_없는_댓글() throws Exception {
        // given
        int commentId = 33;
        int authorId = 99;

        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setAuthorId(authorId);
        request.setContent("Update comment!");

        when(commentService.updateComment(eq(commentId), any(CommentUpdateRequest.class)))
                // thenThrow(new 예외()) : 직접 생성 -> getMessage() 메시지 있음
                // thenThrow(예외.class) : Mockito가 대신 생성 -> getMessage()가 null일 수 있음 → 메시지 검증 시 반드시 new
                .thenThrow(new CommentNotFoundException());

        // when & then
        mockMvc.perform(put("/api/comments/"+commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 댓글입니다."));
    }

    @Test
    void 댓글_수정_실패_권한_없음() throws Exception {
        // given
        int commentId = 33;
        int authorId = 99;

        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setAuthorId(authorId);
        request.setContent("Update comment!");

        when(commentService.updateComment(eq(commentId), any(CommentUpdateRequest.class)))
                .thenThrow(new CommentAccessDeniedException());

        // when & then
        mockMvc.perform(put("/api/comments/"+commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("댓글 작성자만 수정/삭제할 수 있습니다."));
    }

    // ----------------------------------------------------------------
    // deleteComment
    // @PathVariable int commentId, @RequestParam int authorId
    // ----------------------------------------------------------------
    @Test
    void 댓글_삭제_성공() throws Exception {
        int commentId = 33;
        int authorId = 99;

        // when & then
        mockMvc.perform(delete("/api/comments/"+commentId)
                        .param("authorId", String.valueOf(authorId)))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment(commentId, authorId);
    }

    @Test
    void 댓글_삭제_실패_없는_댓글() throws Exception {
        // given
        int commentId = 33;
        int authorId = 99;

        doThrow(new CommentNotFoundException())
                .when(commentService).deleteComment(commentId, authorId);

        // when & then
        mockMvc.perform(delete("/api/comments/"+commentId)
                        .param("authorId", String.valueOf(authorId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 댓글입니다."));
    }

    @Test
    void 댓글_삭제_실패_권한_없음() throws Exception {
        // given
        int commentId = 33;
        int authorId = 99;

        doThrow(new CommentAccessDeniedException())
                .when(commentService).deleteComment(commentId, authorId);

        // when & then
        mockMvc.perform(delete("/api/comments/"+commentId)
                        .param("authorId", String.valueOf(authorId)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("댓글 작성자만 수정/삭제할 수 있습니다."));
    }

}
