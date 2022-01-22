package com.example.jwttutorial.controller;

import com.example.jwttutorial.dto.LoginDto;
import com.example.jwttutorial.dto.TokenDto;
import com.example.jwttutorial.jwt.JwtFilter;
import com.example.jwttutorial.jwt.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {

        /** LoginDto 의 username, password 를 파라미터로 받고, 이를 이용해서 UsernamePasswordAuthenticationToken을 생성 */
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        /**
         * authenticationToken 을 이용해서 Authentication 객체를 생성하려고 authenticate() 메소드가 실행될 때,
         * loadUserByUsername 메소드가 실행된다.
         *  -> 이 결과값을 authentication 에 생성하고, 이를 SecurityContext 에 저장하고,
         *  authentication 을 createToken() 메소드를 통해서 jwt 에 Token 을 생성한다.
         */
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication);

        /** jwt Token 을 Response Header 에도 넣어주고, TokenDto 를 이용해서 Response Body 에도 넣어서 리턴한다. */
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);
    }
}