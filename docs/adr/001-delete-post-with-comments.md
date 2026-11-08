
## 001. 댓글 달린 글 삭제 처리
- 날짜: 2026-11-06
- 관련 브랜치: `fix/post-delete-with-comments`

---
### ❗문제
댓글이 달린 글을 삭제(`DELETE /api/posts/{id}`)하면 500 에러가 발생.

---
### 💡원인
`comments.post_id`는 `post.id`를 가리키는 외래키임.

```text
posts                     comments
+----+-------+            +----+---------+---------+
| id | title |            | id | post_id | content |
+----+-------+            +----+---------+---------+
| 5  | 제목  |  <-------  | 1  |    5    | 댓글1   |
+----+-------+            | 2  |    5    | 댓글2   |
                          +----+---------+---------+
```

`PostService.deletePost`는 `postRepository.delete(post)`만 호출한다.

1. DB가 5번 글 삭제를 거절한다. 
   - 외래키 제약조건 때문에 글을 지우면 댓글들이 존재하지 않는 글을 가리키게 되기 때문.
   - 외래키에 `ON DELETE SET NULL` 등을 따로 설정하지 않았으므로 기본 동작은 "거절".
2. Spring이 이 거절을 `DataIntegrityViolationException`으로 감싸 던진다.
3. `GlobalExcpetionHandler`에 이 예외의 처리가 없으므로 500으로 응답한다.

즉, 글을 지우려면 그 글을 가리키는 댓글이 먼저 처리되어야 한다.

---
### 📄검토한 선택지
|   | 방식                      | 설명                                           |
|---|-------------------------|----------------------------------------------|
| 1 | Service에서 댓글 먼저 삭제      | `commentRepository`로 해당 글의 댓글을 지운 뒤 글 삭제     |
| 2 | JPA cascade             | Post에 댓글 목록 필드를 두고, "글 삭제 시 댓글도 삭제"를 JPA에 설정 |
| 3 | DB `ON DELETE CASCADE`  | 외래키 자체에 "글이 지워지면 댓글도 지움" 규칙을 두기              |
| 4 | 정책 변경 (soft delete 등)   | 글을 실제로 지우지 않고 "삭제됨"으로 표시하거나, 댓글 있는 글의 삭제를 막음 |

    💡`cascade`란?

    "부모에게 한 작업을 자식에게도 똑같이 전파한다"는 JPA 설정.
    폭포, 종속의 뜻을 가짐.

    - cascade가 없다면 `댓글 지우기 & 글 지우기` 과정을 위해서 댓글 지우는 코드, 글 지우는 코드를 모두 구현해야 함. 
    - cascade 사용 시, 글을 지우는 코드만 구현하면 JPA가 댓글도 알아서 먼저 지우고 글을 지움
    - cascade 사용을 위해 Post에 댓글 목록 필드가 필요한 이유
        : JPA가 지워주려면 "어떤 댓글을 지워야 하는지" 알아야 하기 때문. JPA는 자바 객체의 필드를 따라가서 그걸 찾음.
    - cascade의 종류
        - REMOVE : 부모 삭제 -> 자식도 삭제
        - PERSIST : 부모 저장 -> 자식도 저장
        - ALL : 모든 작업 전파

### 💫 검토 과정
|   | 방식                     | 설명                                                                                                                                                                                                                                                                |
|---|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | Service에서 댓글 먼저 삭제     | - `PostService`가 `CommentRepository`를 의존하게 된다. <br/>- 삭제 순서(댓글->글)가 Service 코드에 그대로 드러난다.                                                                                                                                                                         |
| 2 | JPA cascade            | - 좋아지는 점: `post.getComments()`로 댓글을 바로 꺼낼 수 있고, 글 삭제 시 댓글 삭제가 자동으로 된다.<br/>- 복잡해지는 점: Post와 Comment가 양방향 관계가 되어 `mappedBy`등 새 개념이 필요하고, 양쪽을 함께 맞춰야 하는 관리 포인트가 생긴다.<br/>- 성능: LAZY이므로 평소에는 댓글 목록을 불러오지 않는다. 하지만 cascade 삭제 시에는 댓글을 모두 메모리로 불러온 뒤 하나씩 DELETE를 보내야 한다. |
| 3 | DB `ON DELETE CASCADE` | - 삭제가 DB 안에서 한 번에 처리되어 빠르다.<br/>- JPA를 거치지 않으므로 JPA는 댓글이 지워진 것을 모른다. JPA가 들고 있는 Comment 객체와 DB 상태가 어긋날 수 있다. |
| 4 | 정책 변경                  | - soft delete : 실제 커뮤니키는 대부분 글을 실제로 지우지 않고 "삭제된 게시글입니다"로 표시하며 댓글은 남겨둔다. <br/>- 구현하려면 Post에 삭제 여부 필드를 추가하고, 목록/상세/내가 쓴 글 등 모든 조회에 "삭제되지 않은 것만" 조건을 붙여야 함. 정책 자체가 바뀌는 큰 작업. |

---
### ✅ 결정 내용

- 정책 변경 ➡️ soft delete

### ❓ 결정 이유

- 글이 삭제되어도 사용자가 작성한 댓글은 사용자의 기록으로 남아야 한다고 판단했다.
- 이후 "내가 쓴 댓글 조회" 기능을 제공할 때, 글이 삭제됐다는 이유만으로 내 댓글까지 사라진다면 사용자가 납득하기 어렵다.
- soft delete는 글을 실제로 지우지 않고 "삭제됨" 상태만 표시하므로, 댓글을 그대로 보존하면서 글 삭제 요청도 처리할 수 있다.


- [ 다른 선택지와 비교 ]
    - ① Service에서 댓글 먼저 삭제, ② JPA cascade, ③ DB ON DELETE CASCADE는
      모두 글과 함께 댓글을 실제로 삭제하므로 위 요구를 만족하지 못한다.
- [ 부수 효과 ]
    - 실제 DELETE가 일어나지 않고 UPDATE만 발생하므로 외래키 제약조건 문제(500)가 함께 해결된다.
    - 삭제된 데이터를 나중에 복구하거나 확인할 수 있다.
- [ 감수하는 점 ]
    - 글 목록, 상세, 내가 쓴 글 등 조회마다 "삭제되지 않은 글"인지 확인하는 조건이 필요하다.
    - 삭제 시각을 updatedAt으로 대신하므로, 삭제 시각만 따로 다뤄야 하는 요구가 생기면 deletedAt 필드 도입을 다시 검토한다.


---
### 📋 삭제된 글 처리 정책

삭제된 글은 "없는 글"처럼 다루되, id로 직접 접근하면 410 Gone으로 "삭제된 글입니다."를 안내한다.

| 대상 | 삭제된 글일 때 | 비고 |
|---|---|---|
| 글 목록 | 제외 | |
| 내가 쓴 글 | 제외 | |
| 글 상세 | 410 | |
| 글 수정 / 삭제 | 410 | 작성자가 아니면 403이 먼저 |
| 새 댓글 작성 | 410 | |
| 댓글 목록 조회 | 410 | |
| 댓글 단건 조회 | 가능 | "내가 쓴 댓글 목록"에서 삭제된 글의 댓글을 눌렀을 때 대응 |
| 댓글 수정 | 410 | 글이 삭제됐는데 댓글만 고치는 것은 어색함 |
| 댓글 삭제 | 가능 | 내 기록은 내가 지울 수 있어야 함 |
 
---
### 🛠️ 설계

**Post 엔티티**

| 항목 | 내용 |
|---|---|
| 필드 | `boolean deleted` (기본값 false) |
| `markDeleted()` | 삭제 상태로 표시 (실제 DELETE 아님) |
| `validateNotDeleted()` | 삭제된 글이면 `PostDeletedException` |
| `isDeleted()` | 삭제 여부 반환 |

**예외**

| 예외 | 메시지 | 상태 코드 |
|---|---|---|
| `PostDeletedException` | 삭제된 글입니다. | 410 GONE |

**PostRepository**

| 용도 | 메서드 |
|---|---|
| 글 목록 | `findAllByDeletedFalse()` |
| 내가 쓴 글 | `findAllByAuthorAndDeletedFalse(User author)` |

**Service 흐름**

| 메서드 | 변경 |
|---|---|
| `getPosts` | `findAll()` -> `findAllByDeletedFalse()` |
| `getPost` | `post.validateNotDeleted()` |
| `updatePost` | `post.validateAuthor()` -> `post.validateNotDeleted()` -> 수정 |
| `deletePost` | `post.validateAuthor()` -> `post.validateNotDeleted()` -> `post.markDeleted()` |
| `getMyPosts` | `findAllByAuthor()` -> `findAllByAuthorAndDeletedFalse()` |
| `createComment` | `post.validateNotDeleted()` |
| `getComments` | `post.validateNotDeleted()` |
| `getComment` | 변경 없음 |
| `updateComment` | `comment.validateAuthor()` -> `comment.getPost().validateNotDeleted()` |
| `deleteComment` | 변경 없음 (댓글은 실제 DELETE) |
 
---
### 🤔 세부 결정

- [ 상태 코드: 404 대신 410 ]
    - 삭제된 리소스는 404로 처리하는 경우가 가장 흔하다. 단순하고 삭제 사실도 숨길 수 있다.
    - 410 Gone은 "있었지만 삭제됨"을 정확히 표현한다. 화면에서 "삭제된 글입니다."를 안내하기 좋아 410을 선택했다.
    - 상세만 410이고 나머지가 404면 같은 글이 API마다 다르게 보이므로, "삭제된 글을 id로 직접 건드리면 410"으로 통일했다.


- [ 검사 순서: 권한 확인 먼저 ]
    - 본인만 가능한 API(글 수정/삭제, 댓글 수정)는 권한 확인 -> 삭제 확인 순서로 검사한다.
    - 권한이 없는 사람은 시도 자체가 불가능해야 한다고 판단했다.
    - 결과: 남이 삭제된 글을 삭제하면 403, 작성자가 삭제된 글을 다시 삭제하면 410.


- [ 필드: `boolean deleted` vs `LocalDateTime deletedAt` ]
    - `deletedAt` 하나로 삭제 여부와 삭제 시각을 함께 표현할 수 있다.
    - 하지만 삭제 시 dirty checking으로 `@PreUpdate`가 실행되어 `updatedAt`에 삭제 시각이 남고, 삭제 후에는 수정이 막혀 값이 바뀌지 않는다.
    - 연습 범위에서는 이것으로 충분하다고 보고 더 단순한 `boolean deleted`를 선택했다.


- [ 검사 위치: 엔티티 메서드 ]
    - "이 글이 삭제됐는가?"는 Post 자신의 데이터만으로 답할 수 있는 질문이므로, 작성자 확인(`validateAuthor`)과 같은 방식으로 엔티티에 두었다.
    - 글 한 개를 다룰 때는 엔티티에 묻고(`validateNotDeleted`), 목록을 다룰 때는 처음부터 삭제되지 않은 글만 조회한다(`findAllByDeletedFalse`).
    - 댓글은 soft delete 대상이 아니다. 댓글을 가리키는 테이블이 없어 외래키 문제가 없으므로 지금처럼 실제로 DELETE 한다.