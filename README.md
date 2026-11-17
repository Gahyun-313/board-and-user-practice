# 📋 게시판 백엔드

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![H2](https://img.shields.io/badge/H2-004088?style=flat-square&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=flat-square&logo=junit5&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)

**회원 기능이 포함된 게시판 REST API**입니다.
글·댓글 CRUD부터 작성자 권한 검사, soft delete, JWT 인증까지 직접 설계·구현하고, 기능마다 테스트로 검증했습니다.

<br>

## 📖 서비스 소개

회원가입/로그인 후 게시글과 댓글을 작성하고, 작성자 본인만 수정·삭제할 수 있는 게시판 백엔드입니다.

```
Client
  │  POST /api/users/login  ->  {"token": "eyJ..."}
  │
  │  요청 (Authorization: Bearer 토큰)
  ▼
Controller  ->  Service  ->  Repository  ->  H2
                  │
                  └─ 엔티티 메서드로 권한/삭제 여부 검사 (validateAuthor, validateNotDeleted)
```

| 기능 | 상태 |
|---|---|
| 회원가입 / 로그인 | ✅ 완료 |
| 게시글 CRUD + 내가 쓴 글 | ✅ 완료 |
| 댓글 CRUD | ✅ 완료 |
| 작성자 권한 검사 | ✅ 완료 |
| 게시글 soft delete | ✅ 완료 |
| JWT 토큰 발급 | ✅ 완료 |
| JWT 요청 검증 (필터) | 🚧 진행 중 |
| 회원 정보 수정 / 탈퇴 | ⏳ 예정 |

<br>

## 📌 프로젝트 정보

| 항목 | 내용 |
|---|---|
| 유형 | 개인 학습 프로젝트 |
| 인원 | 1인 (백엔드) |
| 기간 | 2026.10 ~ 진행 중 |
| 서버 | Spring Boot REST API |
| DB | H2 (인메모리) |

<br>

## 👨‍💻 담당 역할

- 도메인(User, Post, Comment) 및 연관관계 설계
- REST API 설계 및 구현 (회원 2개, 게시글 6개, 댓글 5개)
- 작성자 권한 검사, 삭제 정책 등 비즈니스 규칙 설계
- 전역 예외 처리 및 상태 코드 설계
- Service / Controller 단위 테스트 작성
- 주요 설계 결정 ADR 문서화

<br>

## 🛠️ 기술 스택

| 분류 | 기술 | 상세 |
|---|---|---|
| Language | Java 17 | |
| Framework | Spring Boot 4.1 | Spring Web MVC |
| Security | Spring Security, BCrypt | 비밀번호 암호화 |
| Auth | JWT (jjwt 0.12.6) | HS256 서명, PK id를 subject로 사용 |
| ORM | Spring Data JPA, Hibernate | LAZY 로딩, dirty checking |
| DB | H2 | 인메모리 (재시작 시 초기화) |
| Test | JUnit 5, Mockito, MockMvc, AssertJ | Service / Controller 단위 테스트 |
| Build | Gradle | |

<br>

## 🔌 API

로그인과 회원가입, 글 목록을 제외한 API는 로그인이 필요합니다. 수정/삭제는 작성자 본인만 가능합니다.

| 도메인 | 기능 | 엔드포인트 |
|---|---|---|
| User | 회원가입, 로그인, 내가 쓴 글 | `/api/users/**` |
| Post | 작성, 목록, 상세, 수정, 삭제 | `/api/posts/**` |
| Comment | 작성, 목록, 단건, 수정, 삭제 | `/api/posts/{postId}/comments/**`, `/api/comments/**` |

### API 목록

| Method | Endpoint | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/users/signup` | 누구나 | 회원가입 |
| POST | `/api/users/login` | 누구나 | 로그인, JWT 발급 |
| GET | `/api/users/posts` | 본인 | 내가 쓴 글 |
| POST | `/api/posts` | 로그인 | 글 작성 |
| GET | `/api/posts` | 누구나 | 글 목록 |
| GET | `/api/posts/{id}` | 로그인 | 글 상세 |
| PUT | `/api/posts/{id}` | 작성자 | 글 수정 |
| DELETE | `/api/posts/{id}` | 작성자 | 글 삭제 (soft delete) |
| POST | `/api/posts/{postId}/comments` | 로그인 | 댓글 작성 |
| GET | `/api/posts/{postId}/comments` | 로그인 | 댓글 목록 |
| GET | `/api/posts/{postId}/comments/{commentId}` | 로그인 | 댓글 단건 |
| PUT | `/api/comments/{commentId}` | 작성자 | 댓글 수정 |
| DELETE | `/api/comments/{commentId}` | 작성자 | 댓글 삭제 |

> 🚧 JWT 필터 적용 전까지는 임시로 요청 JSON 또는 쿼리 파라미터의 `authorId`로 작성자를 식별합니다.

### 상태 코드

| 상황 | 코드 |
|---|---|
| 아이디 중복 | 409 Conflict |
| 아이디/비밀번호 불일치 | 401 Unauthorized |
| 작성자가 아님 | 403 Forbidden |
| 유저/글/댓글 없음 | 404 Not Found |
| 삭제된 글에 접근 | 410 Gone |

<br>

## ✨ 핵심 구현

### 1. 댓글 달린 글 삭제: soft delete
댓글이 달린 글을 삭제하면 외래키 제약조건 위반으로 500 에러가 발생했습니다.
Service에서 댓글 먼저 삭제, JPA cascade, DB `ON DELETE CASCADE`를 검토했지만 모두 댓글까지 지워집니다.
글이 삭제돼도 댓글은 사용자의 기록으로 남아야 한다고 판단해 `deleted` 필드를 둔 soft delete로 해결했습니다.
삭제된 글에 id로 접근하면 410 Gone을 반환합니다. [자세히 ↗️](docs/adr/001)

### 2. 작성자 권한 검사를 엔티티로
글 수정/삭제, 댓글 수정/삭제 4곳에 같은 권한 확인 코드가 반복됐습니다.
"이 글의 작성자가 맞는가?"는 작성자 정보를 가진 엔티티가 답할 수 있는 질문이라고 보고,
`Post`, `Comment`에 `validateAuthor(int authorId)`를 두어 검사 후 바로 예외를 던지도록 했습니다.
검사 순서는 권한 확인 → 삭제 여부 확인으로, 권한 없는 사용자는 글 상태와 관계없이 403을 받습니다.

### 3. JWT 토큰 발급
로그인 성공 시 사용자 PK id를 subject로 담은 토큰을 발급합니다.
로그인 아이디 대신 PK id를 넣어 요청마다 추가 DB 조회 없이 권한 검사에 바로 쓸 수 있게 했습니다.
토큰 생성과 검증은 `JwtProvider` 한 곳에서 담당해 비밀키를 한 곳에서만 관리합니다.

### 4. 테스트
| 대상 | 방식 |
|---|---|
| Service | Mockito. Repository는 mock, 엔티티는 실제 객체 (id가 필요한 User만 mock) |
| Controller | `@WebMvcTest` + MockMvc. 상태 코드와 응답 JSON 검증 |
| JwtProvider | Spring 없이 직접 생성. 발급 / 위조 / 만료 / 빈 값 검증 |

- 기능마다 성공 / 대상 없음 / 권한 없음 케이스를 기본으로 작성
- 실패 케이스는 예외뿐 아니라 막혔는지까지 검증 (데이터 미변경, `verify(..., never())`)
- 총 `<N>`개 테스트 통과

<br>

## 🚀 시작하기

### 요구 사항
- JDK 17 이상
- Gradle은 별도 설치 필요 없음 (Gradle Wrapper 포함)

### 설치
```bash
git clone https://github.com/Gahyun-313/board-and-user-practice.git
cd board-and-user-practice
```

비밀키 설정 파일을 만듭니다.
```bash
cp src/main/resources/application-secret.properties.example src/main/resources/application-secret.properties
```
`application-secret.properties`를 열어 `jwt.secret`에 32자 이상의 값을 입력합니다.
```properties
jwt.secret=32자-이상의-비밀키
```

> ⚠️ `application-secret.properties`는 Git에 포함되지 않습니다.
> 이 파일이 없으면 `Could not resolve placeholder 'jwt.secret'` 에러로 서버가 실행되지 않으며,
> 비밀키가 32바이트보다 짧으면 `WeakKeyException`이 발생합니다.

### 실행
```bash
# macOS / Linux / Git Bash
./gradlew bootRun

# Windows (PowerShell)
.\gradlew.bat bootRun
```
서버는 `http://localhost:8080`에서 실행됩니다. H2 인메모리 DB를 사용하므로 재시작하면 데이터가 초기화됩니다.

### 동작 확인
```bash
# 회원가입
curl -X POST http://localhost:8080/api/users/signup \
  -H "Content-Type: application/json" \
  -d '{"userId": "test01", "password": "1234", "nickname": "테스터"}'

# 로그인 -> 토큰 발급
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"userId": "test01", "password": "1234"}'
```

### 테스트
```bash
./gradlew test
```

<br>

## 📁 프로젝트 구조

```
src/main/java/com/study/board_practice
├── controller    API 엔드포인트
├── service       비즈니스 로직
├── repository    Spring Data JPA
├── domain        엔티티 (User, Post, Comment)
├── dto           요청/응답 객체
├── exception     커스텀 예외 + GlobalExceptionHandler
└── security      SecurityConfig, JwtProvider
```

<br>

## 🤝 개발 컨벤션

### 브랜치
| Prefix | 용도 |
|---|---|
| `feat/` | 기능 추가 |
| `fix/` | 버그 수정 |
| `docs/` | 문서 |
| `chore/` | 설정, 빌드 등 |

### 커밋 메시지
```
feat: 로그인 성공 시 JWT 토큰 발급
fix: 댓글 달린 글 삭제 시 500 에러 해결
test: PostService soft delete 테스트 추가
docs: ADR 001 댓글 달린 글 삭제 처리 기록
```

### PR
- 기능 단위로 브랜치를 만들고 PR로 main에 머지
- 머지 전 `./gradlew test` 전체 통과 확인

<br>

## 📚 문서

| 문서 | 내용 |
|---|---|
| [ADR 000](docs/adr/000) | ADR 템플릿 |
| [ADR 001](docs/adr/001) | 댓글 달린 글 삭제 처리 (soft delete) |
