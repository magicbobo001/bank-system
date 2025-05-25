package com.bank.customer.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanRepayment;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {
    List<LoanRepayment> findByLoan_LoanId(Long loanId);
    
    Optional<LoanRepayment> findByLoan_LoanIdAndRepaymentDate(Long loanId, LocalDate repaymentDate);
    
    long countByLoanAndStatus(
        LoanApplication loan, LoanRepayment.RepaymentStatus status
    );
}