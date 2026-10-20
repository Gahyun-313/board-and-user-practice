package com.study.board_practice.service;

import com.study.board_practice.domain.Comment;
import com.study.board_practice.domain.Post;
import com.study.board_practice.domain.User;
import com.study.board_practice.dto.CommentCreateRequest;
import com.study.board_practice.dto.CommentResponse;
import com.study.board_practice.dto.CommentUpdateRequest;
import com.study.board_practice.exception.CommentAccessDeniedException;
import com.study.board_practice.exception.CommentNotFoundException;
import com.study.board_practice.exception.PostNotFoundException;
import com.study.board_practice.exception.UserNotFoundException;
import com.study.board_practice.repository.CommentRepository;
import com.study.board_practice.repository.PostRepository;
import com.study.board_practice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    // ----------------------------------------------------------------
    // createComment
    // ----------------------------------------------------------------
    @Test
    void 댓글생성_실패_글_없으면_예외() {
        // given
        int postId = 3;
        int authorId = 2;

        // 글이 없다고 가정
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        CommentCreateRequest request = new CommentCreateRequest();
        request.setAuthorId(authorId);
        request.setContent("content1");

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, request))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("존재하지 않는 글입니다.");
    }

    @Test
    void 댓글생성_실패_글_있지만_유저_없으면_예외() {
        // given
        int postId = 3;
        int authorId = 2;

        User author = new User();
        Post post = new Post(author, "title", "content");
        // 글이 있다고 가정
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        // 유저가 없다고 가정
        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        CommentCreateRequest request = new CommentCreateRequest();
        request.setAuthorId(authorId);
        request.setContent("content");

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("존재하지 않는 유저입니다.");
    }

    @Test
    void 댓글생성_성공_글과_유저_모두_있으면_성공() {
        // given
        int postId = 3;
        int authorId = 2;

        User user = new User();
        Post post = new Post(user, "title", "content");

        // 글과 유저 모두 존재한다고 가정
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(authorId)).thenReturn(Optional.of(user));

        // save()가 id=10인 댓글을 돌려준다고 가정
        Comment savedComment = mock(Comment.class);
        when(savedComment.getId()).thenReturn(10);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // 요청
        CommentCreateRequest request = new CommentCreateRequest();
        request.setAuthorId(authorId);
        request.setContent("content");

        // when - 실제 요청
        int result = commentService.createComment(postId, request);

        // then
        // Repository.save()가 딱 한 번 호출됐는지 확인
        assertThat(result).isEqualTo(10);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    // ----------------------------------------------------------------
    // getComments
    // ----------------------------------------------------------------
    @Test
    void 댓글목록조회_실패_글_없으면_예외() {
        // given
        // 글이 없다고 가정
        int postId = 3;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getComments(3))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void 댓글목록조회_성공_글_있으면_성공() {
        // given
        // 댓글이 2개 있다고 가정
        int postId = 3;

        // mock post 생성
        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // 댓글 2개의 유저 생성
        User author1 = new User("author1_userId", "1234", "author1");
        User author2 = new User("author2_userId", "1234", "author2");

        // 댓글 2개 생성
        Comment comment1 = new Comment(post, author1, "comment1");
        Comment comment2 = new Comment(post, author2, "comment2");

        when(commentRepository.findAllByPost(post)).thenReturn(List.of(comment1, comment2));

        // when
        List<CommentResponse> results = commentService.getComments(postId);

        // then
        // 응답 배열 길이가 2인지 확인
        assertThat(results.size()).isEqualTo(2);
        // comment1, comment2 내용 일치 확인
        // isEqualTo(comment1.getAuthor().getNickname())는 맞든 틀리든 양쪽이 같기만 하면 통과되므로 직접적인 기대값을 써주는 게 더 좋음
        assertThat(results.get(0).getAuthorNickname()).isEqualTo("author1");
        assertThat(results.get(1).getAuthorNickname()).isEqualTo("author2");
    }

    @Test
    void 댓글목록조회_성공_빈_리스트() {
        // given
        // 댓글이 없다고 가정
        Post post = mock(Post.class);
        when(postRepository.findById(3)).thenReturn(Optional.of(post));

        // when
        List<CommentResponse> results = commentService.getComments(3);

        // then
        // 응답 배열 길이가 0인지 확인
        assertThat(results).isEmpty();
    }

    // ----------------------------------------------------------------
    // getComment
    // ----------------------------------------------------------------
    @Test
    void 댓글조회_실패_댓글_없으면_예외() {
        // given
        int commentId = 3;
        // 댓글이 없다고 가정
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getComment(commentId))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining("존재하지 않는 댓글입니다.");
    }

    @Test
    void 댓글조회_성공_댓글_있으면_성공() {
        // given
        int commentId = 3;

        Post post = mock(Post.class);
        User author = mock(User.class);
        when(author.getNickname()).thenReturn("nickname1");

        // 댓글이 있다고 가정
        Comment comment = new Comment(post, author, "content");
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        CommentResponse result = commentService.getComment(commentId);

        // then
        assertThat(result.getAuthorNickname()).isEqualTo("nickname1");
    }

    // ----------------------------------------------------------------
    // updateComment
    // ----------------------------------------------------------------
    @Test
    void 댓글수정_실패_댓글_없으면_예외() {
        // given
        int commentId = 3;
        // 댓글이 없다고 가정;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setAuthorId(1);

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(commentId, request))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining("존재하지 않는 댓글입니다.");
    }

    @Test
    void 댓글수정_실패_본인확인_실패() {
        // given
        int commentId = 3;
        // 댓글은 있지만 작성자 id와 authorId가 다르다고 가정
        Comment comment = mock(Comment.class);
        User author = mock(User.class);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn(author);
        when(author.getId()).thenReturn(55);

        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setAuthorId(44);
        request.setContent("update content");

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(commentId, request))
                .isInstanceOf(CommentAccessDeniedException.class)
                .hasMessageContaining("댓글 작성자만 수정/삭제할 수 있습니다.");
    }

    @Test
    void 댓글수정_성공_댓글_있고_본인확인까지_성공() {
        // given
        int commentId = 3;
        int authorId = 55;
        // 댓글이 있고 작성자 id와 authorId가 같다고 가정
        Comment comment = mock(Comment.class);
        User author = mock(User.class);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn(author);
        when(author.getId()).thenReturn(authorId);
        when(author.getNickname()).thenReturn("nickname1");

        // when
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setAuthorId(authorId);
        request.setContent("update content");

        CommentResponse result = commentService.updateComment(commentId, request);

        // then
        assertThat(result.getAuthorNickname()).isEqualTo("nickname1");
        // update 호출 여부 verify
        verify(comment).update("update content");
        verify(commentRepository, times(1)).flush();
    }

    // ----------------------------------------------------------------
    // deleteComment
    // ----------------------------------------------------------------
    @Test
    void 댓글삭제_실패_댓글_없으면_예외() {
        // given
        int commentId = 3;
        // 댓글이 없다고 가정
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(commentId, 44))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining("존재하지 않는 댓글입니다.");
    }

    @Test
    void 댓글삭제_실패_댓글_있지만_본인확인_실패() {
        // given
        int commentId = 3;
        int authorId = 55;
        // 댓글은 있지만 작성자 id와 authorId가 다르다고 가정
        Comment comment = mock(Comment.class);
        User author = mock(User.class);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn(author);
        when(author.getId()).thenReturn(authorId);

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(commentId, 44))
                .isInstanceOf(CommentAccessDeniedException.class)
                .hasMessageContaining("댓글 작성자만 수정/삭제할 수 있습니다.");
    }

    @Test
    void 댓글삭제_성공_댓글_있고_본인확인까지_성공() {
        // given
        int commentId = 3;
        int authorId = 55;

        // 댓글이 있고 작성자 id와 authorId가 같다고 가정
        Comment comment = mock(Comment.class);
        User author = mock(User.class);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getAuthor()).thenReturn(author);
        when(author.getId()).thenReturn(authorId);

        // when
        commentService.deleteComment(commentId, authorId);

        // then
        verify(commentRepository).delete(comment); // 호출됐는지 확인
    }

}
