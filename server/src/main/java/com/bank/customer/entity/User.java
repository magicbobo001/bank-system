package com.bank.customer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import java.util.List;

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
    // 原代码中使用的 org.hibernate.mapping.List 不是泛型类型，这里替换为 java.util.List
    private List<UserRole> roles;
}