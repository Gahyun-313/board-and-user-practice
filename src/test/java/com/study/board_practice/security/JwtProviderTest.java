package com.study.board_practice.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {
    // 테스트용 비밀키
    private static final String SECRET = "RnfrhrnakaltntrkfnahdqffkdaltntrkfnrhrnakclwmxkfhfkEp";
    // 위조 테스트용 다른 비밀키
    private static final String OTHER_SECRET = "other-secret-key-for-forged-token-0123456789";
    // 테스트용 만료 시간
    private static final long EXPIRATION_MS = 3600000L; // 1시간

    // 테스트 대상
    private final JwtProvider jwtProvider = new JwtProvider(SECRET, EXPIRATION_MS);

    @Test
    void 토큰_생성_후_id_추출() {
        // given
        int id = 99;
        // when
        String token = jwtProvider.createToken(id); // 토큰 생성
        int extractedId = jwtProvider.getId(token); // id 꺼내기
        // then
        assertThat(extractedId).isEqualTo(id);
    }

    @Test
    void 정상_토큰은_isValid_true() {
        // given
        int id = 99;
        String token = jwtProvider.createToken(id); // 토큰 생성
        // when
        boolean result = jwtProvider.isValid(token);
        // then
        assertThat(result).isTrue();
    }

    @Test
    void 다른_비밀키로_만든_토큰은_isValid_false() {
        // given
        int id = 44;
        JwtProvider otherJwtProvider = new JwtProvider(OTHER_SECRET, EXPIRATION_MS);
        String forgedToken = otherJwtProvider.createToken(id);
        // when
        boolean result = jwtProvider.isValid(forgedToken);
        // then
        assertThat(result).isFalse();
    }

    @Test
    void null_또는_빈_문자열_토큰은_isValid_false() {
        // when & then
        // 토큰 자체가 없으면 IllegalArgumentException -> catch 에서 false
        assertThat(jwtProvider.isValid(null)).isFalse();
        assertThat(jwtProvider.isValid("")).isFalse();
    }

    @Test
    void 만료된_토큰은_isValid_false() {
        // given
        int id = 88;
        // 토큰 만료를 위해 expirationMs를 음수로 설정
        JwtProvider expiredJwtProvider = new JwtProvider(SECRET, -EXPIRATION_MS);
        String expiredToken = expiredJwtProvider.createToken(id);
        // when
        boolean result = jwtProvider.isValid(expiredToken);
        // then
        assertThat(result).isFalse();
    }
}
