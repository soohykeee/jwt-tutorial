package com.example.jwttutorial.config;


import com.example.jwttutorial.jwt.JwtAccessDeniedHandler;
import com.example.jwttutorial.jwt.JwtAuthenticationEntryPoint;
import com.example.jwttutorial.jwt.JwtSecurityConfig;
import com.example.jwttutorial.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * @EnableWebSecurity 는 기본적인 Web보안을 활성화 하겠다는 의미
 * 추가적인 설정을 위해서 WebSecurityConfigurer 을 implements 하거나
 * WebSecurityConfigurerAdapter 를 extends 하는 방법이 있다.
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)  /** @PreAuthorize 어노테이션을 메소드단위로 추가하기위해서 적용 */
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            TokenProvider tokenProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    /** 암호 복호화위해 존재하는 bean */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 해당 메소드는 무시해도 됌. 해당 도메인에는 무시되도록 설정 */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(
                        "/h2-console/**"
                        ,"/favicon.ico"
                        ,"/error"
                );
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                /** token을 사용하는 방식이기 때문에 csrf를 disable합니다. */
                .csrf().disable()

                /** Exception 핸들링할때 우리가 만들었던 클래스들을 추가해준다. */
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                /** h2-console을 사용하기 위해 추가 */
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()

                /** 세션을 사용하지 않기 때문에 STATELESS로 설정 */
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                /** 로그인 Api, 회원가입 Api는 Token 이 없는 상태에서 요청이 들어오므로, permitAll() 설정 */
                .and()
                .authorizeRequests()
                .antMatchers("/api/hello").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/signup").permitAll()

                /** 위에 permit 한 도메인 뺴고는 접근 가능한 token 을 가진 유저만 접근가능하도록 */
                .anyRequest().authenticated()

                /** JwtFilter 를 addFilterBefore 로 등록했던 JwtSecurityConfig 클래스도 적용 */
                .and()
                .apply(new JwtSecurityConfig(tokenProvider));
    }

}