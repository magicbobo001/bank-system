package com.bank.customer.service;

import com.bank.customer.entity.User;
import com.bank.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // 用户注册
    public User registerUser(User user) {
        // 业务校验（如用户名唯一性）
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        return userRepository.save(user);
    }

    public List<User> findAll() {
    return userRepository.findAll();
    }

}