package com.bank.customer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanRepayment;
import com.bank.customer.entity.LoanRepayment.RepaymentStatus;
import com.bank.customer.repository.LoanRepaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledLoanRepaymentService {
    private final LoanRepaymentRepository repaymentRepo;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final AccountService accountService;

    // 每天凌晨2点执行自动还款检查
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void processAutoRepayments() {
        LocalDate today = LocalDate.now();

        // 1. 处理当天应还款项
        processDueRepayments(today);

        // 2. 检查逾期项目并更新状态
        processOverdueRepayments(today);
    }

    // 处理当天应还款项
    void processDueRepayments(LocalDate today) {
        List<LoanRepayment> dueRepayments = repaymentRepo.findByStatusAndRepaymentDate(
                RepaymentStatus.PENDING, today);

        for (LoanRepayment repayment : dueRepayments) {
            try {
                autoRepay(repayment);
            } catch (Exception e) {
                log.error("自动还款失败 - 还款ID: {}", repayment.getRepaymentId(), e);
                // 首次扣款失败，标记为逾期
                repayment.setStatus(RepaymentStatus.OVERDUE);
                repaymentRepo.save(repayment);
            }
        }
    }

    // 处理逾期项目
    void processOverdueRepayments(LocalDate today) {
        List<LoanRepayment> overdueRepayments = repaymentRepo.findByStatus(
                RepaymentStatus.OVERDUE);

        for (LoanRepayment repayment : overdueRepayments) {
            long overdueDays = ChronoUnit.DAYS.between(
                    repayment.getRepaymentDate(), today);

            // 计算滞纳金 (假设按日利率0.05%计算)
            BigDecimal lateFee = repayment.getAmount()
                    .multiply(BigDecimal.valueOf(0.0005))
                    .multiply(BigDecimal.valueOf(overdueDays))
                    .setScale(2, RoundingMode.HALF_UP);

            repayment.setLateFee(lateFee);
            repaymentRepo.save(repayment);

            // 逾期超过60天，标记为坏账
            if (overdueDays >= 60) {
                LoanApplication loan = repayment.getLoan();
                loan.setStatus(LoanApplication.LoanStatus.DEFAULT);
                loanService.updateLoanStatus(loan);

                // 更新用户和账户的逾期标记
                accountService.markAccountWithOverdue(loan.getAccount().getAccountId());
            }
        }
    }

    // 执行自动还款
    void autoRepay(LoanRepayment repayment) {
        LoanApplication loan = repayment.getLoan();
        String fromAccountId = loan.getAccount().getAccountId();
        String toAccountId = "LOAN_BANK_ACCOUNT"; // 银行贷款账户
        double amount = repayment.getAmount().add(repayment.getLateFee()).doubleValue();

        // 调用TransactionService进行转账
        transactionService.transfer(fromAccountId, toAccountId, amount);

        // 更新还款状态
        repayment.setStatus(RepaymentStatus.PAID);
        repayment.setActualRepaymentDate(LocalDate.now());
        repaymentRepo.save(repayment);

        // 检查贷款是否全部还清
        loanService.checkAndUpdateLoanCompletion(loan);
    }
}