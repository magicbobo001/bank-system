package com.bank.customer.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.customer.entity.User;
import com.bank.customer.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import com.bank.customer.security.JwtTokenProvider;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final PasswordEncoder passwordEncoder;
        private final UserRepository userRepository;
        private final JwtTokenProvider jwtTokenProvider;

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest request) {
                User user = userRepository.findByUsername(request.username())
                                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
                // 防御性检查：确保角色数据非空
                if (user.getRoles() == null || user.getRoles().isEmpty()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未分配角色");
                }
                user.getRoles().forEach(ur -> {
                        if (ur.getRole() == null) {
                                throw new RuntimeException("用户角色关联数据异常");
                        }
                });
                String token = jwtTokenProvider.generateToken(user); // 生成Token
                return ResponseEntity.ok()
                                .body(Map.of(
                                                "userId", user.getUserId(),
                                                "username", user.getUsername(),
                                                "roles", user.getRoles().stream()
                                                                .map(role -> role.getRole().getRoleName()) // 获取角色名称
                                                                .collect(Collectors.toList()),
                                                "token", token));
        }

        @GetMapping("/me")
        public ResponseEntity<?> getCurrentUser() {
                // 从 SecurityContext 中获取当前用户（需结合 JWT 验证逻辑）
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
                String token = jwtTokenProvider.generateToken(user); // 生成Token
                return ResponseEntity.ok()
                                .body(Map.of(
                                                "userId", user.getUserId(),
                                                "username", user.getUsername(),
                                                "roles", user.getRoles().stream()
                                                                .map(role -> role.getRole().getRoleName())
                                                                .collect(Collectors.toList()),
                                                "token", token));
        }

        record LoginRequest(String username, String password) {
        }
}