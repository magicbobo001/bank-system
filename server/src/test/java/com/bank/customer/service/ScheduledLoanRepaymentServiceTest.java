package com.bank.customer.service;

import com.bank.customer.entity.Account;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanRepayment;
import com.bank.customer.entity.LoanRepayment.RepaymentStatus;
import com.bank.customer.repository.LoanRepaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduledLoanRepaymentServiceTest {

    @Mock
    private LoanRepaymentRepository repaymentRepo;

    @Mock
    private TransactionService transactionService;

    @Mock
    private LoanService loanService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private ScheduledLoanRepaymentService repaymentService;

    // 测试处理到期还款-成功场景
    @Test
    void processDueRepayments_Success() {
        // Arrange
        LocalDate today = LocalDate.now();
        LoanRepayment repayment = new LoanRepayment();
        repayment.setRepaymentId(1L);
        repayment.setStatus(RepaymentStatus.PENDING);
        repayment.setAmount(new BigDecimal(1000));
        repayment.setLateFee(BigDecimal.ZERO);
        LoanApplication loan = new LoanApplication();
        Account account = new Account();
        account.setAccountId("ACC123");
        loan.setAccount(account);
        repayment.setLoan(loan);
        List<LoanRepayment> dueRepayments = Arrays.asList(repayment);

        when(repaymentRepo.findByStatusAndRepaymentDate(eq(RepaymentStatus.PENDING), eq(today)))
                .thenReturn(dueRepayments);
        doNothing().when(transactionService).transfer(anyString(), anyString(), anyDouble());

        // Act
        repaymentService.processDueRepayments(today);

        // Assert
        verify(transactionService).transfer(eq("ACC123"), eq("LOAN_BANK_ACCOUNT"), eq(1000.0));
        assertEquals(RepaymentStatus.PAID, repayment.getStatus());
        verify(repaymentRepo).save(repayment);
        verify(loanService).checkAndUpdateLoanCompletion(loan);
    }

    // 测试处理到期还款-失败场景
    @Test
    void processDueRepayments_Failure() {
        // Arrange
        LocalDate today = LocalDate.now();
        LoanRepayment repayment = new LoanRepayment();
        repayment.setRepaymentId(1L);
        repayment.setStatus(RepaymentStatus.PENDING);
        LoanApplication loan = new LoanApplication();
        Account account = new Account();
        account.setAccountId("ACC123");
        loan.setAccount(account);
        repayment.setLoan(loan);
        List<LoanRepayment> dueRepayments = Arrays.asList(repayment);

        when(repaymentRepo.findByStatusAndRepaymentDate(eq(RepaymentStatus.PENDING), eq(today)))
                .thenReturn(dueRepayments);

        // Act
        repaymentService.processDueRepayments(today);

        // Assert
        assertEquals(RepaymentStatus.OVERDUE, repayment.getStatus());
        verify(repaymentRepo).save(repayment);
    }

    // 测试处理逾期还款-计算滞纳金
    @Test
    void processOverdueRepayments_CalculateLateFee() {
        // Arrange
        LocalDate today = LocalDate.now();
        LoanRepayment repayment = new LoanRepayment();
        repayment.setRepaymentId(1L);
        repayment.setStatus(RepaymentStatus.OVERDUE);
        repayment.setRepaymentDate(today.minusDays(3)); // 逾期3天
        repayment.setAmount(new BigDecimal(1000));
        List<LoanRepayment> overdueRepayments = Arrays.asList(repayment);

        when(repaymentRepo.findByStatus(eq(RepaymentStatus.OVERDUE))).thenReturn(overdueRepayments);

        // Act
        repaymentService.processOverdueRepayments(today);

        // Assert
        BigDecimal expectedLateFee = new BigDecimal(1000).multiply(new BigDecimal(0.0005)).multiply(new BigDecimal(3))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedLateFee, repayment.getLateFee());
        verify(repaymentRepo).save(repayment);
    }

    // 测试处理逾期还款-超过60天标记为坏账
    @Test
    void processOverdueRepayments_MarkAsDefault() {
        // Arrange
        LocalDate today = LocalDate.now();
        LoanRepayment repayment = new LoanRepayment();
        repayment.setRepaymentId(1L);
        repayment.setStatus(RepaymentStatus.OVERDUE);
        repayment.setAmount(new BigDecimal(1000));
        repayment.setRepaymentDate(today.minusDays(61)); // 逾期61天
        LoanApplication loan = new LoanApplication();
        loan.setLoanId(1L);
        Account account = new Account();
        account.setAccountId("ACC123");
        loan.setAccount(account);
        repayment.setLoan(loan);
        List<LoanRepayment> overdueRepayments = Arrays.asList(repayment);

        when(repaymentRepo.findByStatus(eq(RepaymentStatus.OVERDUE))).thenReturn(overdueRepayments);

        // Act
        repaymentService.processOverdueRepayments(today);

        // Assert
        verify(loanService).updateLoanStatus(loan);
        assertEquals(LoanApplication.LoanStatus.DEFAULT, loan.getStatus());
        verify(accountService).markAccountWithOverdue("ACC123");
    }
}