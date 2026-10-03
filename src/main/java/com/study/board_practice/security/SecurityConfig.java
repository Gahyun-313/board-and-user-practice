package com.study.board_practice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Spring Security 설정 클래스
// @Configuration: 이 클래스 안의 @Bean 메서드들이 Spring 설정으로 등록된다는 표시
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // H2 콘솔은 폼 기반이라 CSRF 보호랑 충돌함 - 연습 단계라 꺼둠
                .csrf(csrf -> csrf.disable())
                // H2 콘솔이 iframe으로 렌더링되는데 Security 기본값이 iframe 차단함 - 풀어줌
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        // h2-console은 인증 없이 접근 가능
                        .requestMatchers("/h2-console/**").permitAll()
                        // 회원가입/로그인 -> 로그인 안 한 사람이 하는 거니까 인증 없이 접근 가능해야 함
                        .requestMatchers(
                                "/api/users/signup", "/api/users/login").permitAll()
                        // 나머지 요청은 전부 인증 필요
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}