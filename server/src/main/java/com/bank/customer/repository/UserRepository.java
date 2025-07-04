package com.bank.customer.repository;

import com.bank.customer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // 根据用户名查找用户
    // Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roles ur " +
            "LEFT JOIN FETCH ur.role " +
            "WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    // 根据用户名检查用户是否存在
    boolean existsByUsername(String username);

    /**
     * 直接更新用户最后登录时间（更高效的实现）
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :now WHERE u.userId = :userId")
    void updateLastLogin(@Param("userId") Integer userId, @Param("now") LocalDateTime now);
}