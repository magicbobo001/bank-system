package com.bank.customer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Table(name = "[user]")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    private String address;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "userId")
    private List<UserRole> roles = new ArrayList<>(); // 初始化空列表避免null
    @Column(nullable = false)
    private boolean hasOverdue;

    // 导入 Collection 类
    // 导入 GrantedAuthority 类
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleName().toUpperCase()))
                .collect(Collectors.toList());
    }
}