package com.bank.customer.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.customer.dto.LoanApplicationRequest;
import com.bank.customer.dto.LoanRepaymentDTO;
import com.bank.customer.dto.LoanStatusDTO;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanRepayment;
import com.bank.customer.service.LoanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    // ==== 获取待审批贷款列表（管理员接口） ====
    @GetMapping("/pending")
    public List<LoanApplication> getPendingLoans() {
        return loanService.getPendingLoans();
    }

    // ==== 申请贷款 ====
    @PostMapping("/apply")
    public LoanApplication applyLoan(@RequestBody LoanApplicationRequest request) {
        return loanService.applyLoan(
                request.getUserId(),
                request.getAccountId(),
                request.getAmount(),
                request.getTerm(),
                request.getAnnualRate(),
                request.getStartDate());
    }

    // ==== 审批贷款（管理员接口） ====
    @PutMapping("/{loanId}/approve")
    public LoanApplication approveLoan(@PathVariable Long loanId) {
        return loanService.approveLoan(loanId);
    }

    // 拒绝贷款申请接口（管理员权限）
    @PutMapping("/{loanId}/reject")
    public LoanApplication rejectLoan(@PathVariable Long loanId) {
        return loanService.rejectLoan(loanId);
    }

    // ==== 还款 ====
    @PostMapping("/{loanId}/repay")
    public LoanRepayment repay(
            @PathVariable Long loanId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate repaymentDate) {
        return loanService.repay(loanId, repaymentDate);
    }

    // ==== 查询还款计划 ====
    @GetMapping("/{loanId}/schedule")
    public List<LoanRepaymentDTO> getRepaymentSchedule(@PathVariable Long loanId) {
        return loanService.getRepaymentSchedule(loanId).stream()
                .map(LoanRepaymentDTO::fromEntity)
                .toList();
    }

    // 获取所有贷款状态
    @GetMapping("/status")
    public List<LoanStatusDTO> getAllLoanStatus() {
        List<LoanApplication> loans = loanService.getAllLoans();
        return loans.stream()
                .map(loan -> new LoanStatusDTO(
                        loan.getLoanId(),
                        loan.getUser().getUserId(),
                        loan.getStatus().name()))
                .toList();
    }
}
