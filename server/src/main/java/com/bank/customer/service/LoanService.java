package com.bank.customer.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import com.bank.customer.exception.InvalidDateException;
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
    private final TransactionService transactionService;

    // ==== 查询待审批贷款（管理员接口） ====
    public List<LoanApplication> getPendingLoans() {
        return loanAppRepo.findByStatus(LoanApplication.LoanStatus.PENDING);
    }

    // ==== 申请贷款 ====
    public LoanApplication applyLoan(
            Integer userId,
            String accountId,
            BigDecimal amount,
            Integer term,
            BigDecimal annualRate,
            LocalDate startDate) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        // 校验账户状态
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountStatusException("账户非活跃状态");
        }
        // 验证startDate必须为当前日期后至少15天
        LocalDate minValidDate = LocalDate.now().plusDays(15);
        if (startDate.isBefore(minValidDate)) {
            throw new InvalidDateException("贷款起始日期必须至少为当前日期后15天");
        }
        // 创建贷款申请
        LoanApplication loan = new LoanApplication();
        loan.setUser(account.getUser());
        loan.setAccount(account);
        loan.setAmount(amount);
        loan.setTerm(term);
        loan.setInterestRate(annualRate.setScale(4, RoundingMode.HALF_UP));
        loan.setStartDate(startDate);
        loan.setEndDate(startDate.plusMonths(term));
        loan.setMonthlyPayment(
                loanCalculator.calculateMonthlyPayment(
                        amount,
                        annualRate,
                        term));
        loan.setRemainingPrincipal(amount);

        return loanAppRepo.save(loan);
    }

    // ==== 审批贷款 ====
    public LoanApplication approveLoan(Long loanId) {
        LoanApplication loan = loanAppRepo.findById(loanId)
                .orElseThrow(LoanNotFoundException::new);

        // 更新贷款状态
        loan.setStatus(LoanApplication.LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDateTime.now());
        loanAppRepo.save(loan);
        // 生成还款计划
        List<LoanRepayment> schedule = loanCalculator.generateRepaymentSchedule(loan);
        repaymentRepo.saveAll(schedule);

        return loan;
    }

    // 拒绝贷款申请
    public LoanApplication rejectLoan(Long loanId) {
        LoanApplication loan = loanAppRepo.findById(loanId)
                .orElseThrow(LoanNotFoundException::new);
        loan.setStatus(LoanApplication.LoanStatus.REJECTED);
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
        // 计算逾期费用（如果有）
        LocalDate today = LocalDate.now();
        if (repaymentDate.isBefore(today)) {
            long overdueDays = ChronoUnit.DAYS.between(repaymentDate, today);
            BigDecimal lateFee = repayment.getAmount()
                    .multiply(BigDecimal.valueOf(0.0005))
                    .multiply(BigDecimal.valueOf(overdueDays))
                    .setScale(2, RoundingMode.HALF_UP);
            repayment.setLateFee(lateFee);
        }

        // 执行还款转账
        String fromAccountId = repayment.getLoan().getAccount().getAccountId();
        String toAccountId = "LOAN_BANK_ACCOUNT"; // 银行贷款账户
        double amount = repayment.getAmount().add(repayment.getLateFee()).doubleValue();
        transactionService.transfer(fromAccountId, toAccountId, amount);
        // 更新还款状态
        repayment.setStatus(LoanRepayment.RepaymentStatus.PAID);
        repayment.setActualRepaymentDate(today);
        repaymentRepo.save(repayment);

        // 更新贷款剩余本金
        LoanApplication loan = repayment.getLoan();
        loan.setRemainingPrincipal(
                loan.getRemainingPrincipal().subtract(repayment.getPrincipal()));

        // 检查贷款是否全部还清
        checkAndUpdateLoanCompletion(loan);

        loanAppRepo.save(loan);
        return repayment;
    }

    // 检查并更新贷款完成状态
    public void checkAndUpdateLoanCompletion(LoanApplication loan) {
        if (isLoanCompleted(loan)) {
            loan.setStatus(LoanApplication.LoanStatus.CLOSED);
            loanAppRepo.save(loan);
        }
    }

    // 更新贷款状态
    public void updateLoanStatus(LoanApplication loan) {
        loanAppRepo.save(loan);
    }

    // 检查贷款是否全部还清
    private boolean isLoanCompleted(LoanApplication loan) {
        return repaymentRepo.countByLoanAndStatus(
                loan, LoanRepayment.RepaymentStatus.PENDING) == 0;
    }

    // ==== 查询还款计划 ====
    @SuppressWarnings("unused")
    public List<LoanRepayment> getRepaymentSchedule(Long loanId) {
        LoanApplication loan = loanAppRepo.findById(loanId)
                .orElseThrow(LoanNotFoundException::new);
        return repaymentRepo.findByLoan_LoanId(loanId);
    }

    public List<LoanApplication> getAllLoans() {
        return loanAppRepo.findAll();
    }
}