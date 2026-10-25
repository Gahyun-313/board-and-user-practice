package com.study.board_practice.repository;

import com.study.board_practice.domain.Comment;
import com.study.board_practice.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // 기본 제공: save, findById, delete

    // 특정 글의 댓글 목록
    List<Comment> findAllByPost(Post post);
}
