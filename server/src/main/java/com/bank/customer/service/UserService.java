package com.bank.customer.service;

import com.bank.customer.entity.User;
import com.bank.customer.entity.UserRole;
import com.bank.customer.repository.UserRepository;
import com.bank.customer.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    // 用户注册
    public User registerUser(User user) {
        // 业务校验（如用户名唯一性）
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        User savedUser = userRepository.save(user);

        // 自动分配普通用户角色
        userRoleRepository.save(new UserRole(
                savedUser.getUserId(),
                1));// 关联普通用户角色
        return savedUser;
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User updateUser(User updatedUser) {
        if (userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User existing = getUserById(updatedUser.getUserId());
        existing.setFullName(updatedUser.getFullName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPhone(updatedUser.getPhone());
        return userRepository.save(existing);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("旧密码不正确");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 更新用户最后登录时间为当前时间
     */
    @Transactional
    public void updateLastLogin(Integer userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }
}