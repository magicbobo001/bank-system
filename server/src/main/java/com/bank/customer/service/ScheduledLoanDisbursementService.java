package com.bank.customer.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.repository.LoanApplicationRepository;

@Service
public class ScheduledLoanDisbursementService {
    private Logger log;
    private final LoanApplicationRepository loanAppRepo;
    private final TransactionService transactionService;

    public ScheduledLoanDisbursementService(LoanApplicationRepository loanAppRepo,
            TransactionService transactionService) {
        this.loanAppRepo = loanAppRepo;
        this.transactionService = transactionService;
        this.log = LoggerFactory.getLogger(ScheduledLoanDisbursementService.class);
    }

    // 每天凌晨1点执行放款检查
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void processScheduledDisbursements() {
        LocalDate today = LocalDate.now();

        // 查询所有状态为APPROVED且start_date等于今天的贷款
        List<LoanApplication> loansToDisburse = loanAppRepo.findByStatusAndStartDate(
                LoanApplication.LoanStatus.APPROVED, today);

        for (LoanApplication loan : loansToDisburse) {
            disburseLoan(loan);
        }
    }

    // 执行放款操作
    void disburseLoan(LoanApplication loan) {
        try {
            // 银行专用贷款账户ID
            String bankLoanAccountId = "LOAN_BANK_ACCOUNT";
            String userAccountId = loan.getAccount().getAccountId();
            double loanAmount = loan.getAmount().doubleValue();

            // 使用TransactionService执行转账
            transactionService.transfer(bankLoanAccountId, userAccountId, loanAmount);

            // 更新贷款状态为已放款
            loan.setStatus(LoanApplication.LoanStatus.DISBURSED);
            loan.setDisbursementDate(LocalDate.now());
            loanAppRepo.save(loan);

        } catch (Exception e) {
            // 记录放款失败日志，可考虑添加重试机制
            log.error("Loan disbursement failed for loan ID: {}", loan.getLoanId(), e);
        }
    }

    // 添加日志设置方法，供测试注入mock
    public void setLog(Logger log) {
        this.log = log;
    }
}