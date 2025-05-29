package com.bank.customer.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.bank.customer.repository.UserRepository;
import com.bank.customer.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                // 公共接口
                                                .requestMatchers("/api/auth/**").permitAll()

                                                // 普通用户权限
                                                .requestMatchers(
                                                                "/api/transactions/**",
                                                                "/api/loans/apply",
                                                                "/api/users/me/**")
                                                .hasRole("USER")

                                                // 管理员权限
                                                .requestMatchers(
                                                                "/api/accounts/admin/**",
                                                                "/api/loans/approve/**",
                                                                "/api/users")
                                                .hasRole("ADMIN")

                                                .anyRequest().authenticated())
                                .httpBasic(Customizer.withDefaults());
                return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService(UserRepository userRepository,
                        UserRoleRepository userRoleRepository) {
                return username -> userRepository.findByUsername(username)
                                .map(user -> {
                                        List<SimpleGrantedAuthority> authorities = userRoleRepository
                                                        .findByUserId(user.getUserId())
                                                        .stream()
                                                        .map(ur -> new SimpleGrantedAuthority(
                                                                        "ROLE_" + ur.getRole().getRoleName()))
                                                        .collect(Collectors.toList());

                                        return new org.springframework.security.core.userdetails.User(
                                                        user.getUsername(),
                                                        user.getPasswordHash(),
                                                        authorities);
                                })
                                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        }
}