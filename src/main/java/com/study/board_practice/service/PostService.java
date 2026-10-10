package com.study.board_practice.service;

import com.study.board_practice.domain.Post;
import com.study.board_practice.domain.User;
import com.study.board_practice.dto.PostCreateRequest;
import com.study.board_practice.dto.PostResponse;
import com.study.board_practice.exception.PostNotFoundException;
import com.study.board_practice.exception.UserNotFoundException;
import com.study.board_practice.repository.PostRepository;
import com.study.board_practice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/** 글 게시 로직 서비스 */
@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 생성자 주입
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // 글 작성
    // return 타입이 int인 이유 : 저장된 글의 id를 return
    public int createPost(PostCreateRequest request) {

        // 작성자가 있는지 확인 및 작성자 정보 불러오기
        User author = userRepository.findById(request.getAuthorId())
                // 작성자가 없으면 예외
                .orElseThrow(UserNotFoundException::new);

        // post 저장
        Post post = new Post(author, request.getTitle(), request.getContent());
        Post savedPost = postRepository.save(post);

        return savedPost.getId();
    }

    // 글 목록 조회
    @Transactional(readOnly = true)
    public List<PostResponse> getPosts() {
        List<Post> postList = postRepository.findAll();
        
        // postList를 DTO에 맞게 변환
        return postList.stream()    // postList를 하나씩 꺼내는 흐름 만들기
                .map(post -> PostResponse.from(post))   // 꺼낸 post를 PostResponse로 변환
                .toList();  // 바꾼 결과를 리스트로 모으기
    }

    // 글 상세 조회
    @Transactional(readOnly = true)
    public PostResponse getPost(int id) {
        // post id로 post 불러오기
        Post post = postRepository.findById(id)
                // 해당 id의 post가 없으면 예외
                .orElseThrow(PostNotFoundException::new);
        // PostResponse DTO에 맞춰 리턴
        return PostResponse.from(post);
    }

    // 글 수정

    // 글 삭제

    // 내가 쓴 글
}
