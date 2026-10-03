package com.study.board_practice.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String userId;
    private String password;
    private String nickname;
    private LocalDateTime createdAt;

    // 회원가입용 생성자 - id는 저장 시 DB가 채움
    public User(String userId, String password, String nickname) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.createdAt = LocalDateTime.now();   // 가입일시는 생성자로 받지 않고 자동으로 채움
    }

    // JPA가 리플렉션으로 객체 만들 때 필요한 기본 생성자 (파라미터 없음)
    public User() {     }

    public int getId() {return id;}
    public String getUserId() {return userId;}
    public String getPassword() {return password;}
    public String getNickname() {return nickname;}
    public LocalDateTime getCreatedAt() {return createdAt;}
}
