package com.bank.customer.controller;

import com.bank.customer.dto.LoginRequest;
import com.bank.customer.entity.Role;
import com.bank.customer.entity.User;
import com.bank.customer.entity.UserRole;
import com.bank.customer.repository.UserRepository;
import com.bank.customer.repository.UserRoleRepository;
import com.bank.customer.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;
import java.util.Collections;
import java.util.Optional;

/**
 * 注意：虽然@MockBean已标记为弃用，但在Spring Boot 3.4.x中这是临时状态
 * 官方文档建议在找到更好的替代方案前继续使用
 * 参考：https://github.com/spring-projects/spring-boot/issues/37142
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 针对Spring管理的Bean必须使用@MockBean
    @SuppressWarnings("removal")
    @MockBean
    private UserRepository userRepository;

    @SuppressWarnings("removal")
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @SuppressWarnings("removal")
    @MockBean
    private PasswordEncoder passwordEncoder;

    @SuppressWarnings("removal")
    @MockBean
    private UserRoleRepository userRoleRepository;

    @Test
    @WithMockUser(roles = "USER")
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        Role userRole = new Role();
        userRole.setRoleName("ROLE_USER");
        UserRole userUserRole = mock(UserRole.class);
        when(userUserRole.getRole()).thenReturn(userRole);

        User testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("encodedPassword");
        testUser.setRoles(Collections.singletonList(userUserRole));

        when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("password123"), eq("encodedPassword"))).thenReturn(true);
        when(jwtTokenProvider.generateToken(eq(testUser))).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testLogin_Failure_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setPasswordHash("encodedPassword");

        when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(eq("wrongpassword"), eq("encodedPassword"))).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("用户名或密码错误")));
    }

    // 测试当前用户信息获取
    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetCurrentUser() throws Exception {
        // 创建测试角色
        Role userRole = new Role();
        userRole.setRoleId(1);
        userRole.setRoleName("ROLE_USER");

        // 创建用户角色关联
        UserRole userRoleRelation = new UserRole(1, 1); // 使用正确的构造函数
        userRoleRelation.setRole(userRole);

        // 创建测试用户
        User testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
        testUser.setRoles(Collections.singletonList(userRoleRelation));

        // Mock repository behavior
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // 执行测试
        mockMvc.perform(get("/api/auth/me").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    // 测试用户角色不存在
    @Test
    @WithMockUser(username = "nonexistentuser")
    void testGetCurrentUser_NotExist() throws Exception {
        // 模拟数据库查询返回空结果
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // 执行测试
        mockMvc.perform(get("/api/auth/me").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // 添加无角色分配测试
    @Test
    @WithMockUser(roles = { "USER" })
    void testLogin_NoRolesAssigned() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("noroleuser");
        request.setPassword("password123");
        // 创建无角色用户
        User user = new User();
        user.setUserId(1);
        user.setUsername("noroleuser");
        user.setPasswordHash("encodedPassword");
        user.setRoles(Collections.emptyList()); // 空角色列表

        when(userRepository.findByUsername("noroleuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("password123"), eq("encodedPassword"))).thenReturn(true);
        when(jwtTokenProvider.generateToken(eq(user))).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("用户未分配角色")));
    }

    // 验证用户角色为null
    @Test
    @WithMockUser(roles = "USER")
    void testLogin_RolesNull() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nullroleuser");
        request.setPassword("password123");

        // 创建一个roles为null的用户
        User userWithNullRoles = new User();
        userWithNullRoles.setUserId(1);
        userWithNullRoles.setUsername("nullroleuser");
        userWithNullRoles.setPasswordHash("encodedPassword");
        userWithNullRoles.setRoles(null); // 显式设置roles为null

        when(userRepository.findByUsername("nullroleuser")).thenReturn(Optional.of(userWithNullRoles));
        when(passwordEncoder.matches(eq("password123"), eq("encodedPassword"))).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("用户未分配角色")));
    }

    // 角色关联异常测试
    @Test
    @WithMockUser(roles = { "USER" })
    void testLogin_InvalidRoleAssociation() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("invalidroleuser");
        request.setPassword("password123");
        // 包含空角色的用户
        User user = new User();
        user.setUserId(1);
        user.setPasswordHash("encodedPassword");
        user.setUsername("invalidroleuser");

        UserRole invalidRole = new UserRole();
        invalidRole.setRole(null); // 设置空角色
        user.setRoles(Collections.singletonList(invalidRole));

        when(userRepository.findByUsername("invalidroleuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("password123"), eq("encodedPassword"))).thenReturn(true);
        when(jwtTokenProvider.generateToken(eq(user))).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("用户角色关联数据异常")));
    }
}