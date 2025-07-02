package com.bank.customer.service;

import com.bank.customer.entity.Account;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.repository.LoanApplicationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduledLoanDisbursementServiceTest {

    @Mock
    private LoanApplicationRepository loanAppRepo;

    @Mock
    private TransactionService transactionService;

    @Mock
    private Logger log;

    @InjectMocks
    private ScheduledLoanDisbursementService disbursementService;

    @BeforeEach
    void setUp() {
        disbursementService.setLog(log);
    }

    // 测试放款成功
    @Test
    void disburseLoan_Success() {
        // Arrange
        LoanApplication loan = new LoanApplication();
        loan.setLoanId(1L);
        loan.setAmount(new BigDecimal(10000));
        Account account = new Account();
        account.setAccountId("ACC123");
        loan.setAccount(account);

        doNothing().when(transactionService).transfer(eq("LOAN_BANK_ACCOUNT"), eq("ACC123"), eq(10000.0));

        // Act
        disbursementService.disburseLoan(loan);

        // Assert
        assertEquals(LoanApplication.LoanStatus.DISBURSED, loan.getStatus());
        assertNotNull(loan.getDisbursementDate());
        verify(loanAppRepo).save(loan);
    }

    // 测试放款失败
    @Test
    void disburseLoan_Failure() {
        // Arrange
        LoanApplication loan = new LoanApplication();
        loan.setLoanId(1L);
        loan.setAmount(new BigDecimal(10000));
        Account account = new Account();
        account.setAccountId("ACC123");
        loan.setAccount(account);

        doThrow(new RuntimeException("转账失败")).when(transactionService).transfer(anyString(), anyString(), anyDouble());

        // Act
        disbursementService.disburseLoan(loan);

        // Assert
        assertNotEquals(LoanApplication.LoanStatus.DISBURSED, loan.getStatus());
        verify(log).error(eq("Loan disbursement failed for loan ID: {}"), eq(1L), any(Exception.class));
    }

    // 测试处理定时放款
    @Test
    void processScheduledDisbursements() {
        // Arrange
        LocalDate today = LocalDate.now();
        LoanApplication loan1 = new LoanApplication();
        loan1.setLoanId(1L);
        loan1.setAmount(new BigDecimal(10000));
        Account account1 = new Account();
        account1.setAccountId("ACC123");
        loan1.setAccount(account1);

        LoanApplication loan2 = new LoanApplication();
        loan2.setLoanId(2L);
        loan2.setAmount(new BigDecimal(20000));
        Account account2 = new Account();
        account2.setAccountId("ACC456");
        loan2.setAccount(account2);

        List<LoanApplication> loansToDisburse = Arrays.asList(loan1, loan2);

        when(loanAppRepo.findByStatusAndStartDate(eq(LoanApplication.LoanStatus.APPROVED), eq(today)))
                .thenReturn(loansToDisburse);
        doNothing().when(transactionService).transfer(anyString(), anyString(), anyDouble());

        // Act
        disbursementService.processScheduledDisbursements();

        // Assert
        verify(transactionService, times(2)).transfer(anyString(), anyString(), anyDouble());
        verify(loanAppRepo, times(2)).save(any(LoanApplication.class));
        assertEquals(LoanApplication.LoanStatus.DISBURSED, loan1.getStatus());
        assertEquals(LoanApplication.LoanStatus.DISBURSED, loan2.getStatus());
    }
}