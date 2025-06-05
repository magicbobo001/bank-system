package com.bank.customer.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.customer.entity.User;
import com.bank.customer.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final PasswordEncoder passwordEncoder;
        private final UserRepository userRepository;

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest request) {
                User user = userRepository.findByUsername(request.username())
                                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

                return ResponseEntity.ok()
                                .body(Map.of(
                                                "userId", user.getUserId(),
                                                "username", user.getUsername(),
                                                "roles", user.getRoles().stream()
                                                                .map(role -> role.getRole().getRoleName()) // 获取角色名称
                                                                .collect(Collectors.toList())));
        }

        record LoginRequest(String username, String password) {
        }
}