package com.bank.customer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.customer.entity.UserRole;
import com.bank.customer.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUserId(Integer userId);
}
