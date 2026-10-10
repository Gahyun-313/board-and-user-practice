package com.study.board_practice.service;

import com.study.board_practice.domain.Post;
import com.study.board_practice.domain.User;
import com.study.board_practice.dto.PostCreateRequest;
import com.study.board_practice.dto.PostResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    /** createPost 예외 */
    @Test
    void 작성자가_없으면_예외() {
        // given - 작성자가 없다고 가정
        when(userRepository.findById(1)).thenReturn(Optional.empty());

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

    /** createPost 성공 */
    @Test
    void 작성자가_존재하면_저장() {
        // given (준비)
        // 작성자가 존재한다고 가정
        User author = new User("gahyun", "pw1234", "가현");
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

    /** getPost 성공*/
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

    /** getPost 예외 */
    @Test
    void 글_없음_예외() {
        // given
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(1))
                .isInstanceOf(PostNotFoundException.class);
    }

    /** getPosts 성공 */
    @Test
    void 목록_조회_성공() {
        // given
        User user1 = new User("userId1", "1234", "userNickname1");
        User user2 = new User("userId2", "1234", "userNickname2");
        Post post1 = new Post(user1, "title1", "content1");
        Post post2 = new Post(user2, "title2", "content2");

        when(postRepository.findAll()).thenReturn(List.of(post1, post2));

        // when
        List<PostResponse> results = postService.getPosts();

        // then
        assertThat(results).hasSize(2); // before: assertThat(results.size()).isEqualTo(2);

        assertThat(results.get(0).getAuthorNickname())
                .isEqualTo(post1.getAuthor().getNickname());
        assertThat(results.get(0).getTitle()).isEqualTo(post1.getTitle());
        assertThat(results.get(0).getContent()).isEqualTo(post1.getContent());

        assertThat(results.get(1).getAuthorNickname())
                .isEqualTo(post2.getAuthor().getNickname());
        assertThat(results.get(1).getTitle()).isEqualTo(post2.getTitle());
        assertThat(results.get(1).getContent()).isEqualTo(post2.getContent());
    }

    /** getPosts 빈 목록 */
    @Test
    void 글이_하나도_없을_때_빈_리스트_반환() {
        // given
        when(postRepository.findAll()).thenReturn(List.of());

        // when
        List<PostResponse> results = postService.getPosts();

        // then
        assertThat(results).isEmpty();
    }
}
