package com.study.board_practice.repository;

import com.study.board_practice.domain.Post;
import com.study.board_practice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    // existsById, findById는 JpaRepository 상속을 통해 이미 들어옴.

    List<Post> findAllByAuthor(User author); // 결과가 여러 개일 수 있어서 List로 선언.
    // Optional로 선언할 경우 결과가 2개 이상 일 때 IncorrectResultSizeDataAccessException 터짐. Optional은 있거나 없거나 일 때(0, 1)만 사용.
}
