package com.study.board_practice.repository;

import com.study.board_practice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// User를 다루는 저장소 인터페이스
// JpaRepository<User, Integer> 상속만으로 save, findById, findAll, deleteById 등 기본 CRUD 자동 제공됨
public interface UserRepository extends JpaRepository<User, Integer> {

    // "userId 컬럼 값으로 존재 여부 확인하는 쿼리"를 자동 생성함 (메서드 이름 규칙(existsBy + UserId)을 Spring이 해석)
    boolean existsByUserId(String userId);

    Optional<User> findByUserId(String userId);
    // ↘ Optional인 이유: 있을 수도, 없을 수도 있는 값이기 때문
}
