package com.study.board_practice.domain;

import com.study.board_practice.exception.CommentAccessDeniedException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    private int id;

    // 댓글이 달린 글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {this.createdAt = LocalDateTime.now();}
    @PreUpdate
    void onUpdate() {this.updatedAt = LocalDateTime.now();}

    // 생성자
    public Comment(Post post, User author, String content) {
        this.post = post;
        this.author = author;
        this.content = content;
    }

    protected Comment() { }

    // 댓글 수정 메서드
    public void update(String content) {
        this.content = content;
    }
    // 댓글 작성자 본인 확인 메서드 (다르면 CommentAccessDeniedException)
    public void validateAuthor(int authorId) {
        if (this.author.getId() != authorId) {
            throw new CommentAccessDeniedException();
        }
    }

    public int getId() {return id;}
    public Post getPost() {return post;}
    public User getAuthor() {return author;}
    public String getContent() {return content;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public LocalDateTime getUpdatedAt() {return updatedAt;}
}
