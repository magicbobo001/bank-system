package com.bank.customer.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.customer.dto.LoanRepaymentDTO;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanRepayment;
import com.bank.customer.service.LoanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    // ==== 申请贷款 ====
    @PostMapping("/apply")
    public LoanApplication applyLoan(
        @RequestParam Integer userId,
        @RequestParam String accountId,
        @RequestParam BigDecimal amount,
        @RequestParam Integer term,
        @RequestParam BigDecimal annualRate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
    ) {
        return loanService.applyLoan(
            userId, accountId, amount, term, annualRate, startDate
        );
    }

    // ==== 审批贷款（管理员接口） ====
    @PutMapping("/{loanId}/approve")
    public LoanApplication approveLoan(@PathVariable Long loanId) {
        return loanService.approveLoan(loanId);
    }

    // ==== 还款 ====
    @PostMapping("/{loanId}/repay")
    public LoanRepayment repay(
        @PathVariable Long loanId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate repaymentDate
    ) {
        return loanService.repay(loanId, repaymentDate);
    }

    // ==== 查询还款计划 ====
    @GetMapping("/{loanId}/schedule")
    public List<LoanRepaymentDTO> getRepaymentSchedule(@PathVariable Long loanId) {
        return loanService.getRepaymentSchedule(loanId).stream()
        .map(LoanRepaymentDTO::fromEntity)
        .toList();
    }
}