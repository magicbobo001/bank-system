package com.bank.customer.service;

import com.bank.customer.entity.User;
import com.bank.customer.entity.UserRole;
import com.bank.customer.repository.UserRepository;
import com.bank.customer.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private UserService userService;

    // 测试用户注册成功
    @Test
    void registerUser_Success() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("rawpassword");

        User savedUser = new User();
        savedUser.setUserId(1);
        savedUser.setUsername("testuser");
        savedUser.setPasswordHash("encodedpassword");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("rawpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("encodedpassword", result.getPasswordHash());
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("rawpassword");
        verify(userRepository).save(any(User.class));
        verify(userRoleRepository).save(any(UserRole.class));
    }

    // 测试用户名已存在场景
    @Test
    void registerUser_UsernameExists() {
        // Arrange
        User user = new User();
        user.setUsername("existinguser");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(user);
        });
        assertEquals("用户名已存在", exception.getMessage());
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    // 测试获取用户信息
    @Test
    void getUserById_Success() {
        // Arrange
        Integer userId = 1;
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("testuser", result.getUsername());
    }

    // 测试用户不存在场景
    @Test
    void getUserById_NotFound() {
        // Arrange
        Integer userId = 999;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(userId);
        });
        assertEquals("用户不存在", exception.getMessage());
    }

    // 测试修改密码成功
    @Test
    void changePassword_Success() {
        // Arrange
        Integer userId = 1;
        String oldPassword = "oldpass";
        String newPassword = "newpass";
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setPasswordHash("encodedoldpass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(oldPassword, "encodedoldpass")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodednewpass");

        // Act
        userService.changePassword(userId, oldPassword, newPassword);

        // Assert
        assertEquals("encodednewpass", mockUser.getPasswordHash());
        verify(userRepository).save(mockUser);
    }

    // 测试修改密码用户不存在
    @Test
    void changePassword_UserNotFound() {
        // Arrange
        Integer userId = 1;
        String oldPassword = "oldpass";
        String newPassword = "newpass";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword(userId, oldPassword, newPassword);
        });
        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // 测试旧密码不正确场景
    @Test
    void changePassword_OldPasswordIncorrect() {
        // Arrange
        Integer userId = 1;
        String oldPassword = "wrongoldpass";
        String newPassword = "newpass";
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setPasswordHash("encodedoldpass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(oldPassword, "encodedoldpass")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword(userId, oldPassword, newPassword);
        });
        assertEquals("旧密码不正确", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // 测试更新用户信息
    @Test
    void updateUser_Success() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setUserId(1);
        updatedUser.setUsername("updateduser");
        updatedUser.setFullName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        User existingUser = new User();
        existingUser.setUserId(1);
        existingUser.setUsername("oldname");
        existingUser.setFullName("Old Name");

        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUser(updatedUser);

        // Assert
        assertEquals("Updated Name", result.getFullName());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository).save(existingUser);
    }

    // 测试更新用户信息用户名已存在
    @Test
    void updateUser_UsernameExists() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setUserId(1);
        updatedUser.setUsername("existinguser");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(updatedUser);
        });
        assertEquals("用户名已存在", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // 测试获取所有用户
    @Test
    void findAll_Success() {
        // Arrange
        List<User> mockUsers = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(mockUsers);

        // Act
        List<User> result = userService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    // 测试更新用户最后登录时间成功
    @Test
    void updateLastLogin_Success() {
        // Arrange
        Integer userId = 1;

        // Act
        userService.updateLastLogin(userId);

        // Assert
        verify(userRepository).updateLastLogin(eq(userId), any(LocalDateTime.class));
    }
}