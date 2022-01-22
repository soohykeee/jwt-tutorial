package com.example.jwttutorial.jwt;


import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * TokenProvider, JwtFilter 를 SecurityConfig 에 적용할 때 사용할 JwtSecurityConfig 클래스 추가
 */
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    /**
     * SecurityConfigurerAdapter 를 extends 하고, TokenProvider 를 주입받아서
     * JwtFilter 를 통해 Security 로직에 Filter 를 등록한다.
     */
    private TokenProvider tokenProvider;

    public JwtSecurityConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void configure(HttpSecurity http) {
        JwtFilter customFilter = new JwtFilter(tokenProvider);
        /** UsernamePasswordAuthenticationFilter 해당 필터 전에 tokenProvider 를 넣은 customFilter 를 추가 */
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}