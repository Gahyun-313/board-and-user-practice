package com.study.board_practice.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    // 필드
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    // ManyToOne -> N:1 관계 (글 N : 작성자 1)
    // fetch = LAZY -> 글을 조회할 때 작성자를 바로 가져오지 않고, 실제로 쓸 때 가져옴. Transactional, dirty checking(변경감지)과 연관.
    @JoinColumn(name = "author_userId")
    private User author;

    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }
    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // 생성자
    public Post(User author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public Post() {     }

    // 메서드
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public int getId() {return id;}
    public User getAuthor() {return author;}
    public String getTitle() {return title;}
    public String getContent() {return content;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public LocalDateTime getUpdatedAt() {return updatedAt;}
}
