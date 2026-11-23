package com.nkduy.restaurant_application.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity
    ) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(req -> req
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return httpSecurity.build();
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(
//            HttpSecurity httpSecurity
//    ) throws Exception {
//        httpSecurity
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authorizeHttpRequests(req -> req
//                        .requestMatchers("/error").permitAll()
//
//                        .requestMatchers(
//                                HttpMethod.POST,
//                                "/api/v1/auth/register",
//                                "/api/v1/auth/login",
//                                "/api/v1/auth/refresh",
//                                "/api/v1/product/create-tag",
//                                "/api/v1/product/create-product",
//                                "/api/v1/branch/create",
//                                "/api/v1/ingredient/create"
//                        ).permitAll()
//
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                "/api/v1/ingredient",
//                                "/api/v1/product/tag",
//                                "/api/v1/branch",
//                                "/api/v1/product",
//                                "/api/v1/product/{id}"
//                        ).permitAll()
//                        .requestMatchers(
//                                HttpMethod.PATCH,
//                                "/api/v1/product/tag/{id}",
//                                "/api/v1/branch/{id}",
//                                "/api/v1/ingredient/{id}",
//                                "/api/v1/product/{id}"
//                        ).permitAll()
//
//                        .anyRequest().authenticated()
//                )
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
//                )
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//        ;
//        return httpSecurity.build();
//    }

}
