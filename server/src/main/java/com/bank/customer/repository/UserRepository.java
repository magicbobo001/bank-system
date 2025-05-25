package com.bank.customer.repository;

import com.bank.customer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // 根据用户名查找用户
    Optional<User> findByUsername(String username);
    // 根据用户名检查用户是否存在
    boolean existsByUsername(String username);
}