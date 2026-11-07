package com.study.board_practice.domain;

import com.study.board_practice.exception.PostAccessDeniedException;
import com.study.board_practice.exception.PostDeletedException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    // 필드
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    private int id;

    @ManyToOne(fetch = FetchType.LAZY) // N:1 관계 (글 N : 작성자 1)
    // fetch = LAZY -> 작성자를 실제로 쓸 때 조회. 그 시점에 트랜잭션 안이어야 함.
    @JoinColumn(name = "author_id")
    private User author;

    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted = false;    // 삭제 여부

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

    protected Post() {     }

    // 글 수정 메서드
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
    // 작성자 본인 확인 메서드 (다르면 PostAccessDeniedException)
    public void validateAuthor(int authorId) {
        if (author.getId() != authorId) {
            throw new PostAccessDeniedException();
        }
    }
    // 삭제된 글이면 PostDeletedException
    public void validateNotDeleted() {
        // 삭제된 상태인지 확인 후 삭제됐으면 예외.
        if (deleted) {
            throw new PostDeletedException();
        }
    }
    // 삭제 상태로 표시 (실제 DELETE 아님. soft delete)
    public void markDeleted() {
        this.deleted = true;
    }

    public int getId() {return id;}
    public User getAuthor() {return author;}
    public String getTitle() {return title;}
    public String getContent() {return content;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public boolean isDeleted() {return deleted;}
}
