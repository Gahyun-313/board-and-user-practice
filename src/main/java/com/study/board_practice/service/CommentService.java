package com.study.board_practice.service;

import com.study.board_practice.domain.Comment;
import com.study.board_practice.domain.Post;
import com.study.board_practice.domain.User;
import com.study.board_practice.dto.CommentCreateRequest;
import com.study.board_practice.dto.CommentResponse;
import com.study.board_practice.dto.CommentUpdateRequest;
import com.study.board_practice.exception.*;
import com.study.board_practice.repository.CommentRepository;
import com.study.board_practice.repository.PostRepository;
import com.study.board_practice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    // 필드
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 생성자 주입
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /** 댓글 작성 */
    // @Transactional 안 붙이는 이유 -> .save() 한 번으로 끝나는 메서드는 save 안에서 자체적으로 트랜잭션을 처리해줌.
    public int createComment(int postId, CommentCreateRequest request) {
        // 글 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        // 유저 찾기
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(UserNotFoundException::new);

        // 댓글 생성
        Comment comment = new Comment(post, author, request.getContent());

        // 댓글 저장
        Comment savedComment = commentRepository.save(comment);

        return savedComment.getId();
    }

    /** 특정 글에 달린 댓글 목록 조회 */
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(int postId) {
        // 글 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        // 댓글 목록 조회
        List<Comment> commentList = commentRepository.findAllByPost(post);

        // CommentList를 DTO에 맞게 변환
        return commentList.stream()
                .map(CommentResponse::from)
                .toList();
    }

    /** 댓글 조회 */
    @Transactional(readOnly = true)
    public CommentResponse getComment(int commentId) {
        // 댓글 찾기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        // DTO에 맞게 변환
        return CommentResponse.from(comment);
    }

    /** 댓글 수정 */
    @Transactional
    public CommentResponse updateComment(int commentId, CommentUpdateRequest request) {
        // 댓글 찾기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        // 본인 확인
        if (comment.getAuthor().getId() != request.getAuthorId()) {
            throw new CommentAccessDeniedException();
        }

        // 댓글 수정 및 flush
        comment.update(request.getContent());
        // saveAndFlush 아닌 flush인 이유
        // - save: 새 객체를 처음 저장할 때,
        // - flush: 이미 관리 중인 객체의 변경을 지금 반영할 때,
        // - saveAndFlush: save + flush인데, 이미 관리 중이면 save가 불필요.
        commentRepository.flush();

        return CommentResponse.from(comment);
    }

    /** 댓글 삭제 */
    @Transactional
    public void deleteComment(int commentId, int authorId) {
        // 댓글 찾기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        // 본인 확인
        if (comment.getAuthor().getId() != authorId) {
            throw new CommentAccessDeniedException();
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }

}
