package com.bank.customer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.customer.entity.LoanApplication;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByUserUserId(Integer userId);
}