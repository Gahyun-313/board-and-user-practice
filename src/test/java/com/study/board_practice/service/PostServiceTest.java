package com.study.board_practice.service;

import com.study.board_practice.domain.Post;
import com.study.board_practice.domain.User;
import com.study.board_practice.dto.PostCreateRequest;
import com.study.board_practice.dto.PostResponse;
import com.study.board_practice.dto.PostUpdateRequest;
import com.study.board_practice.exception.PostAccessDeniedException;
import com.study.board_practice.exception.PostDeletedException;
import com.study.board_practice.exception.PostNotFoundException;
import com.study.board_practice.exception.UserNotFoundException;
import com.study.board_practice.repository.PostRepository;
import com.study.board_practice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    // ----------------------------------------------------------------
    // createPost
    // ----------------------------------------------------------------
    @Test
    void 작성자가_없으면_예외() {
        // given - 작성자가 없다고 가정
        when(userRepository.findById(1)).thenReturn(empty());

        PostCreateRequest request = new PostCreateRequest();
        request.setAuthorId(1);
        request.setTitle("Title1");
        request.setContent("Content1");

        // when & then - createPost 호출시 UserNotFoundException 터지는지 확인
        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("존재하지 않는 유저입니다.");

        // save는 절대 호출되면 안 됨
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void 작성자가_존재하면_저장() {
        // given (준비)
        // 작성자가 존재한다고 가정
        User author = new User("userId", "pw1234", "nickname");
        when(userRepository.findById(1)).thenReturn(Optional.of(author));

        // save가 id=10인 글을 돌려준다고 가정
        Post savedPost = mock(Post.class);
        when(savedPost.getId()).thenReturn(10);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostCreateRequest request = new PostCreateRequest();
        request.setAuthorId(1);
        request.setTitle("Title1");
        request.setContent("Content1");

        // when - createPost 호출
        int result = postService.createPost(request);

        // then - save가 딱 1번 호출됐는지 확인
        assertThat(result).isEqualTo(10); // 저장된 글의 id가 리턴되는지
        verify(postRepository, times(1)).save(any(Post.class)); // save 1번 호출
    }

    // ----------------------------------------------------------------
    // getPost
    // ----------------------------------------------------------------
    @Test
    void 상세_조회_성공() {
        // given
        User user1 = new User("userId1", "1234", "userNickname1");
        Post post = new Post(user1, "title1", "content1");
        // post id -> 1
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // when
        PostResponse result = postService.getPost(1);

        // then
        //assertThat(result).isEqualTo(PostResponse.from(post));
        // -> PostResponse는 eqauls()를 오버라이드하지 않아서 값이 아닌 참조(주소)를 비교함
        // -> 값이 같아도 다른 객체라 isEqualTo가 실패함
        assertThat(result.getAuthorNickname()).isEqualTo(post.getAuthor().getNickname());
        assertThat(result.getTitle()).isEqualTo(post.getTitle());
        assertThat(result.getContent()).isEqualTo(post.getContent());
    }

    @Test
    void 글_없음_예외() {
        // given
        int postId = 3;
        when(postRepository.findById(postId)).thenReturn(empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(postId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("존재하지 않는 글입니다.");
    }

    @Test
    void 글_상세_조회_삭제된_글_예외() {
        // given
        // post 생성
        int postId = 1;
        Post post = new Post(mock(User.class), "title", "content");
        // 삭제된 상태로 만들기
        post.markDeleted();
        // post 에 id 연결
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.getPost(postId))
                .isInstanceOf(PostDeletedException.class)
                .hasMessageContaining("삭제된 글입니다.");
    }

    // ----------------------------------------------------------------
    // getPosts
    // ----------------------------------------------------------------

    @Test
    void 목록_조회_성공() {
        // given
        User user1 = new User("userId1", "1234", "userNickname1");
        User user2 = new User("userId2", "1234", "userNickname2");
        Post post1 = new Post(user1, "title1", "content1");
        Post post2 = new Post(user2, "title2", "content2");

        when(postRepository.findAllByDeletedFalse()).thenReturn(List.of(post1, post2));

        // when
        List<PostResponse> results = postService.getPosts();

        // then
        // 리스트 개수 일치 확인
        assertThat(results).hasSize(2); // before: assertThat(results.size()).isEqualTo(2);

        // post1 내용 일치 확인
        assertThat(results.get(0).getAuthorNickname())
                .isEqualTo(post1.getAuthor().getNickname());
        assertThat(results.get(0).getTitle()).isEqualTo(post1.getTitle());
        assertThat(results.get(0).getContent()).isEqualTo(post1.getContent());

        // post2 내용 일치 확인
        assertThat(results.get(1).getAuthorNickname())
                .isEqualTo(post2.getAuthor().getNickname());
        assertThat(results.get(1).getTitle()).isEqualTo(post2.getTitle());
        assertThat(results.get(1).getContent()).isEqualTo(post2.getContent());
    }

    @Test
    void 글이_하나도_없을_때_빈_리스트_반환() {
        // given
        when(postRepository.findAllByDeletedFalse()).thenReturn(List.of());

        // when
        List<PostResponse> results = postService.getPosts();

        // then
        assertThat(results).isEmpty();
    }

    // ----------------------------------------------------------------
    // updatePost
    // ----------------------------------------------------------------

    @Test
    void 글_수정_성공() {
        // given
        // 가짜 User 만들기 : id = 1, 닉네임은 정해두기
        User user1 = mock(User.class);
        when(user1.getNickname()).thenReturn("name1");
        when(user1.getId()).thenReturn(1);

        // 그 User로 Post 만들기 (원래 제목/내용)
        Post post1 = new Post(user1, "title1", "content1");

        // findById(1)이 그 Post를 반환하게 하기
        when(postRepository.findById(1)).thenReturn(Optional.of(post1));

        // request 만들기: authorId는 가짜 User의 id와 같게(1), 새 제목/내용
        PostUpdateRequest request = new PostUpdateRequest();
        request.setAuthorId(1);
        request.setTitle("updated title");
        request.setContent("updated content");

        // when
        // updatePost(1, request) 호출해서 결과 받기
        PostResponse response = postService.updatePost(1, request);

        // then
        // 결과의 title/content가 새 값(리터럴)인지 검증
        assertThat(response.getTitle()).isEqualTo("updated title");
        assertThat(response.getContent()).isEqualTo("updated content");

        // 결과의 작성자 닉네임이 정해둔 닉네임인지 검증
        assertThat(response.getAuthorNickname()).isEqualTo("name1");

        // verify(postRepository, times(1)).flush()로 flush가 불렸는지 확인
        verify(postRepository, times(1)).flush();
    }

    @Test
    void 글_수정_글_없음_예외() {
        // given
        // 빈 값을 줘야 함
        when(postRepository.findById(1)).thenReturn(empty());

        PostUpdateRequest request = new PostUpdateRequest();
        request.setAuthorId(3);
        request.setTitle("Title3");
        request.setContent("Content3");

        // when & then
        assertThatThrownBy(() -> postService.updatePost(1, request))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("존재하지 않는 글입니다.");
    }

    @Test
    void 글_수정_작성자_불일치_예외() {
        // given
        int userId = 1;
        int requestUserId = 3;
        int postId = 44;
        User user1 = mock(User.class);
        when(user1.getId()).thenReturn(userId);

        Post post = new Post(user1, "title1", "content1");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostUpdateRequest request = new PostUpdateRequest();
        request.setAuthorId(requestUserId);
        request.setTitle("Title3");
        request.setContent("Content3");

        // when & then
        assertThatThrownBy(() -> postService.updatePost(postId, request))
                .isInstanceOf(PostAccessDeniedException.class)
                .hasMessage("작성자만 수정/삭제할 수 있습니다.");

        // 권한 검사가 수정보다 먼저 실행됨을 검증
        verify(postRepository, never()).flush();
    }

    @Test
    void 글_수정_삭제된_글_예외() {
        // given
        int userId = 1;
        int postId = 33;
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        Post post = new Post(user, "title1", "content1");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostUpdateRequest request = new PostUpdateRequest();
        request.setAuthorId(userId);
        request.setTitle("Title3");
        request.setContent("Content3");

        post.markDeleted(); // 해당 글 삭제 처리
        assertThat(post.isDeleted()).isTrue(); // 삭제됐는지 확인

        // when & then
        assertThatThrownBy(() -> postService.updatePost(postId, request))
                .isInstanceOf(PostDeletedException.class)
                .hasMessageContaining("삭제된 글입니다.");
        
        verify(postRepository, never()).flush(); // 수정 전에 막혔는지 확인
    }

    // ----------------------------------------------------------------
    // deletePost
    // ----------------------------------------------------------------

    @Test
    void 글_삭제_성공() {
        // given
        User user1 = mock(User.class);
        when(user1.getId()).thenReturn(1);

        Post post = new Post(user1, "title1", "content1");
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // when
        postService.deletePost(1, 1);

        // then
        assertThat(post.isDeleted()).isTrue();
    }

    @Test
    void 글_삭제_글_없음_예외() {
        // given
        when(postRepository.findById(1)).thenReturn(empty());

        // when & then
        assertThatThrownBy(() -> postService.deletePost(1, 1))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("존재하지 않는 글입니다.");
    }

    @Test
    void 글_삭제_작성자_불일치_예외() {
        // given
        int rightUserId = 1;
        int wrongUserId = 2;
        User user1 = mock(User.class);
        when(user1.getId()).thenReturn(rightUserId);

        Post post = new Post(user1, "title1", "content1");
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(1, wrongUserId))
                .isInstanceOf(PostAccessDeniedException.class)
                .hasMessage("작성자만 수정/삭제할 수 있습니다.");

        // 삭제되지 않았음을 검증
        assertThat(post.isDeleted()).isFalse();
    }

    @Test
    void 글_삭제_이미_삭제된_글_예외_작성자가_시도() {
        // given
        int rightUserId = 1;
        User user1 = mock(User.class);
        when(user1.getId()).thenReturn(rightUserId);

        Post post = new Post(user1, "title1", "content1");
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        
        post.markDeleted(); // 해당 글 삭제 처리
        assertThat(post.isDeleted()).isTrue(); // 삭제됐는지 확인

        // when & then
        assertThatThrownBy(() -> postService.deletePost(1, rightUserId))
                .isInstanceOf(PostDeletedException.class)
                .hasMessageContaining("삭제된 글입니다.");
    }

    @Test
    void 글_삭제_이미_삭제된_글_예외_다른_사람이_시도() {
        // given
        int rightUserId = 1;
        int wrongUserId = 2;
        User user1 = mock(User.class);
        when(user1.getId()).thenReturn(rightUserId);

        Post post = new Post(user1, "title1", "content1");
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        post.markDeleted(); // 해당 글 삭제 처리
        assertThat(post.isDeleted()).isTrue(); // 삭제됐는지 확인

        // when & then
        assertThatThrownBy(() -> postService.deletePost(1, wrongUserId))
                .isInstanceOf(PostAccessDeniedException.class)
                .hasMessageContaining("작성자만 수정/삭제할 수 있습니다.");
    }

    // ----------------------------------------------------------------
    // getMyPosts
    // ----------------------------------------------------------------
    @Test
    void 내가_쓴_글_조회_작성자_없음_예외() {
        // given
        when(userRepository.findById(1)).thenReturn(empty());

        // when & then
        assertThatThrownBy(() -> postService.getMyPosts(1))
                .isInstanceOf(UserNotFoundException.class);

        // 글 목록 조회가 호출되지 않음 확인
        verify(postRepository, never()).findAllByAuthorAndDeletedFalse(any(User.class));
    }

    @Test
    void 내가_쓴_글_조회_글_없으면_빈_리스트() {
        // given
        User user1 = mock(User.class);
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(postRepository.findAllByAuthorAndDeletedFalse(user1)).thenReturn(List.of()); // 빈 리스트 반환

        // when
        List<PostResponse> result = postService.getMyPosts(1);

        // then - 빈 리스트 반환 확인
        assertThat(result).isEmpty();
    }

    @Test
    void 내가_쓴_글_조회_성공() {
        // given
        User user1 = mock(User.class);
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(user1.getNickname()).thenReturn("name of user1");

        Post post1 = new Post(user1, "title1", "content1");
        Post post2 = new Post(user1, "title2", "content2");
        when(postRepository.findAllByAuthorAndDeletedFalse(user1)).thenReturn(List.of(post1, post2));

        // when
        List<PostResponse> result = postService.getMyPosts(1);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("title1");
        assertThat(result.get(0).getAuthorNickname()).isEqualTo("name of user1");
        assertThat(result.get(1).getTitle()).isEqualTo("title2");
        assertThat(result.get(1).getAuthorNickname()).isEqualTo("name of user1");
    }
}
