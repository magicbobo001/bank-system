package com.bank.customer.controller;

import com.bank.customer.entity.User;
import com.bank.customer.repository.UserRepository;
import com.bank.customer.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SuppressWarnings("removal")
    @MockBean
    private UserService userService;
    @SuppressWarnings("removal")
    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegisterUser_Success() throws Exception {
        User newUser = new User();
        newUser.setUserId(1);
        newUser.setUsername("newuser");
        when(userService.registerUser(any(User.class))).thenReturn(newUser);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"password123\"}")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers() throws Exception {
        User user1 = new User();
        user1.setUserId(1);
        user1.setUsername("admin");
        user1.setRoles(new ArrayList<>());
        List<User> users = Arrays.asList(user1);
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].username").value("admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateProfile_Success() throws Exception {
        User updatedUser = new User();
        updatedUser.setUserId(1);
        updatedUser.setUsername("updatedname");
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"username\":\"updatedname\"}")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedname"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetMyProfile_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/me").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "nonexistent")
    void testGetMyProfile_UserNotFound() throws Exception {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    void testChangePassword_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        doNothing().when(userService).changePassword(eq(1), eq("oldpass123"), eq("newpass123"));

        mockMvc.perform(put("/api/users/change-password")
                .param("oldPassword", "oldpass123")
                .param("newPassword", "newpass123")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    void testChangePassword_OldPasswordIncorrect() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        doThrow(new IllegalArgumentException("密码错误")).when(userService).changePassword(eq(1), eq("wrongpass"),
                eq("newpass123"));

        mockMvc.perform(put("/api/users/change-password")
                .param("oldPassword", "wrongpass")
                .param("newPassword", "newpass123")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // 测试用户角色不存在
    @Test
    @WithMockUser(username = "nonexistentuser", roles = "ADMIN")
    void testChangePassword_NotExist() throws Exception {
        // 模拟数据库查询返回空结果
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // 执行测试
        mockMvc.perform(put("/api/users/change-password")
                .param("oldPassword", "wrongpass")
                .param("newPassword", "newpass123")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegisterUser_DuplicateUsername() throws Exception {
        when(userService.registerUser(any(User.class))).thenThrow(new IllegalArgumentException("用户名已存在"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"existinguser\",\"password\":\"password123\"}")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名已存在"));
        ;
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testRegisterUser_AccessDenied() throws Exception {
        // 创建并初始化测试用户对象
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("password123");
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPhone("1234567890");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllUsers_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/users").with(csrf()))
                .andExpect(status().isForbidden());
    }
}