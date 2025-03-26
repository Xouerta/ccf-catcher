package com.ccf.sercurity.jwt;

import com.ccf.sercurity.error.ErrorEnum;
import com.ccf.sercurity.error.PlatformException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig implements WebMvcConfigurer {
    private final JwtSecurityFilter jwtSecurityFilter;
    private final ObjectMapper objectMapper;


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .addFilterBefore(jwtSecurityFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                        HttpMethod.GET,
                        "/",
                        "/swagger-ui.html",
                        "/swagger-ui/",
                        "/favicon.ico",
                        "/doc.html",
                        "/webjars/js/**",
                        "/webjars/css/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**"
                ).permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/user/register", "/user/login", "/user/code").permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/public/**").permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS).permitAll())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
//                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .exceptionHandling(c ->
                        c.authenticationEntryPoint((request, response, authException) -> {
                            log.info("认证失败: {}, {}", request.getRequestURI(), authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.addHeader("Content-Type", "application/json;charset=UTF-8");
                            response.getWriter().write(objectMapper.writeValueAsString(Map.of("code", ErrorEnum.UNAUTHORIZED.getCode(), "msg", ErrorEnum.UNAUTHORIZED.getMsg())));
                        })
                )
                .build();
    }

}
