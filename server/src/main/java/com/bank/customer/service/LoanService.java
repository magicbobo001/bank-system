package com.bank.customer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.customer.component.LoanCalculator;
import com.bank.customer.entity.Account;
import com.bank.customer.entity.AccountStatus;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanRepayment;
import com.bank.customer.exception.AccountNotFoundException;
import com.bank.customer.exception.AccountStatusException;
import com.bank.customer.exception.BusinessException;
import com.bank.customer.exception.LoanNotFoundException;
import com.bank.customer.exception.RepaymentNotFoundException;
import com.bank.customer.repository.AccountRepository;
import com.bank.customer.repository.LoanApplicationRepository;
import com.bank.customer.repository.LoanRepaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanApplicationRepository loanAppRepo;
    private final LoanRepaymentRepository repaymentRepo;
    private final AccountRepository accountRepo;
    private final LoanCalculator loanCalculator;

    // ==== 申请贷款 ====
    public LoanApplication applyLoan(
        Integer userId,
        String accountId,
        BigDecimal amount,
        Integer term,
        BigDecimal annualRate,
        LocalDate startDate
    ) {
        Account account = accountRepo.findById(accountId)
            .orElseThrow(AccountNotFoundException::new);
        
        // 校验账户状态
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountStatusException("账户非活跃状态");
        }

        // 创建贷款申请
        LoanApplication loan = new LoanApplication();
        loan.setUser(account.getUser());
        loan.setAccount(account);
        loan.setAmount(amount);
        loan.setTerm(term);
        loan.setInterestRate(annualRate);
        loan.setStartDate(startDate);
        loan.setEndDate(startDate.plusMonths(term));
        loan.setMonthlyPayment(
            loanCalculator.calculateMonthlyPayment(amount, annualRate, term)
        );
        loan.setRemainingPrincipal(amount);
        
        return loanAppRepo.save(loan);
    }

    // ==== 审批贷款 ====
    public LoanApplication approveLoan(Long loanId) {
        LoanApplication loan = loanAppRepo.findById(loanId)
            .orElseThrow(LoanNotFoundException::new);
        
        // 更新贷款状态
        loan.setStatus(LoanApplication.LoanStatus.APPROVED);
        
        // 生成还款计划
        List<LoanRepayment> schedule = 
            loanCalculator.generateRepaymentSchedule(loan);
        repaymentRepo.saveAll(schedule);
        
        return loanAppRepo.save(loan);
    }

    // ==== 还款 ====
    public LoanRepayment repay(Long loanId, LocalDate repaymentDate) {
        LoanRepayment repayment = repaymentRepo
            .findByLoan_LoanIdAndRepaymentDate(loanId, repaymentDate)
            .orElseThrow(RepaymentNotFoundException::new);
        
        // 校验状态
        if (repayment.getStatus() == LoanRepayment.RepaymentStatus.PAID) {
            throw new BusinessException("该期贷款已还清");
        }

        // 更新还款状态
        repayment.setStatus(LoanRepayment.RepaymentStatus.PAID);
        repaymentRepo.save(repayment);

        // 更新贷款剩余本金
        LoanApplication loan = repayment.getLoan();
        loan.setRemainingPrincipal(
            loan.getRemainingPrincipal().subtract(repayment.getPrincipal())
        );
        
        // 如果所有期数已还清，标记贷款完成
        if (isLoanCompleted(loan)) {
            loan.setStatus(LoanApplication.LoanStatus.CLOSED);
        }
        
        loanAppRepo.save(loan);
        return repayment;
    }

    // 检查贷款是否全部还清
    private boolean isLoanCompleted(LoanApplication loan) {
        return repaymentRepo.countByLoanAndStatus(
            loan, LoanRepayment.RepaymentStatus.PENDING
        ) == 0;
    }

    // ==== 查询还款计划 ====
    @SuppressWarnings("unused")
    public List<LoanRepayment> getRepaymentSchedule(Long loanId) {
        LoanApplication loan = loanAppRepo.findById(loanId)
            .orElseThrow(LoanNotFoundException::new);
        return repaymentRepo.findByLoan_LoanId(loanId);
    }
}