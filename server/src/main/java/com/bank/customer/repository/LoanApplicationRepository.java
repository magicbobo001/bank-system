package com.bank.customer.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanApplication.LoanStatus;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByUserUserId(Integer userId);

    List<LoanApplication> findByStatusAndStartDate(LoanStatus status, LocalDate startDate);

    List<LoanApplication> findByStatus(LoanApplication.LoanStatus status);
}