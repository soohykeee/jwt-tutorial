package com.example.jwttutorial.jwt;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * JWT 를 위한 커스텀 필터를 만들기 위해 JwtFilter 클래스 생성
 * GenericFilterBeand 을 extends 해서 doFilter Override, 실제 필터링 로직은 doFilter 내부에 작성
 */
public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * 실제 필터링 로직 작성하는 메소드
     * Token 의 인증정보를 현재 실행중인 SecurityContext 에 저장하기 위한 역할
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        /**
         * resolveToken() 메소드를 통해 Token 을 받아와서 tokenProvider 에 있는 validateToken() 메소드로 유효성 검증을 하고,
         * 정상 Token 이면 SecurityContext에 저장
         */
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();

        /** Token 정보 및 유효성 검증 */
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
        } else {
            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        // chain 으로 연결 해주지 않으면 여기서 프로그램이 종료됨.
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Request Header 에서 Token 정보를 꺼내오기 위한 resolveToken 메소드 추가
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}