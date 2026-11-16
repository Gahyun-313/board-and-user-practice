package com.study.board_practice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// 토큰 생성/검증 담당. LoginService(생성)와 필터(검증)가 함께 사용
@Component
public class JwtProvider {

    // 서명/검증용 키. 생성 시 1회 변환
    private final SecretKey key;
    // 토큰 유효 시간 (ms)
    private final long expirationMs;

    // properties 값 주입받아 key 생성. secret이 32바이트 미만이면 WeakKeyException.
    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // 토큰 생성 메서드. (id -> 토큰)
    // 로그인 성공 시 LoginService가 호출.
    public String createToken(int id) {
        // 지금 시각
        Date now = new Date();
        // 만료 시각 = 지금 + expirationMs
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()   // 토큰 빌더 = 일종의 빈 토큰 틀
                .subject(String.valueOf(id))    // id 넣기
                .issuedAt(now)                  // 발급 시간 넣기
                .expiration(expiryDate)         // 만료 시간 넣기
                .signWith(key)                  // 비밀키로 서명
                .compact();                     // 완성된 토큰을 문자열 변환
    }

    // 서명/만료 검사 메서드. 토큰 확인 메서드. (토큰 -> true/false)
    // 실패 시 예외 대신 false.
    public boolean isValid(String token) {
        try {
            // 파싱 시도 (서명 틀림/만료면 여기서 예외)
            Jwts.parser()   // 토큰 읽는 도구
                    .verifyWith(key)    // 비밀키로 확인
                    .build()
                    .parseSignedClaims(token);  // 토큰 읽기 + 비밀키 확인 + 만료 여부 확인 -> 셋 중 하나라도 실패하면 예외 -> catch에서 false.
            return true;
            // JwtException: 서명 위조, 만료, 형식 오류
            // IllegalArgumentException: 토큰이 null 이거나 빈 문자열
            // Exception 전체를 잡지 않는 이유 : 토큰과 무관한 버그까지 false로 처리되어 원인이 숨겨질 수 있기 때문.
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 id 꺼내는 메서드. (토큰 -> id)
    // isValid 통과한 토큰만 넣을 것 (필터가 호출)
    public int getId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();  // 토큰의 내용을 꺼냄.

        return Integer.parseInt(claims.getSubject()); // 토큰의 subject를 int로 바꿔 id 자체를 리턴
    }
}
