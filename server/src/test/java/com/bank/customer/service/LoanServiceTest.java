package com.bank.customer.service;

import com.bank.customer.component.LoanCalculator;
import com.bank.customer.entity.Account;
import com.bank.customer.entity.AccountStatus;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanRepayment;
import com.bank.customer.entity.User;
import com.bank.customer.exception.AccountStatusException;
import com.bank.customer.exception.BusinessException;
import com.bank.customer.exception.InvalidDateException;
import com.bank.customer.repository.AccountRepository;
import com.bank.customer.repository.LoanApplicationRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private LoanApplicationRepository loanAppRepo;

    @Mock
    private LoanRepaymentRepository repaymentRepo;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private LoanCalculator loanCalculator;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private LoanService loanService;

    // 测试申请贷款成功
    @Test
    void applyLoan_Success() {
        // Arrange
        Integer userId = 1;
        String accountId = "ACC123456";
        BigDecimal amount = new BigDecimal(10000);
        Integer term = 12;
        BigDecimal annualRate = new BigDecimal(0.05);
        LocalDate startDate = LocalDate.now().plusDays(15);
        BigDecimal expectedMonthlyPayment = new BigDecimal(856.07);

        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.ACTIVE);

        LoanApplication savedLoan = new LoanApplication();
        savedLoan.setLoanId(1L);
        savedLoan.setAmount(amount);
        savedLoan.setStatus(LoanApplication.LoanStatus.PENDING);
        savedLoan.setMonthlyPayment(expectedMonthlyPayment);

        when(accountRepo.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(loanCalculator.calculateMonthlyPayment(amount, annualRate, term))
                .thenReturn(expectedMonthlyPayment);
        when(loanAppRepo.save(any(LoanApplication.class))).thenReturn(savedLoan);

        // Act
        LoanApplication result = loanService.applyLoan(userId, accountId, amount, term, annualRate, startDate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getLoanId());
        assertEquals(LoanApplication.LoanStatus.PENDING, result.getStatus());
        assertEquals(expectedMonthlyPayment, result.getMonthlyPayment());
        verify(accountRepo).findById(accountId);
        verify(loanAppRepo).save(any(LoanApplication.class));
    }

    // 测试申请贷款-账户非活跃
    @Test
    void applyLoan_AccountInactive() {
        // Arrange
        String accountId = "ACC123456";
        BigDecimal amount = new BigDecimal(10000);
        Integer term = 12;
        BigDecimal annualRate = new BigDecimal(0.05);
        LocalDate startDate = LocalDate.now().plusDays(15);

        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.FROZEN);

        when(accountRepo.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act & Assert
        AccountStatusException exception = assertThrows(AccountStatusException.class, () -> {
            loanService.applyLoan(1, accountId, amount, term, annualRate, startDate);
        });
        assertEquals("账户非活跃状态", exception.getMessage());
    }

    // 测试申请贷款-日期不符合要求
    @Test
    void applyLoan_InvalidStartDate() {
        // Arrange
        String accountId = "ACC123456";
        BigDecimal amount = new BigDecimal(10000);
        Integer term = 12;
        BigDecimal annualRate = new BigDecimal(0.05);
        LocalDate startDate = LocalDate.now().plusDays(10); // 少于15天

        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.ACTIVE);

        when(accountRepo.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act & Assert
        InvalidDateException exception = assertThrows(InvalidDateException.class, () -> {
            loanService.applyLoan(1, accountId, amount, term, annualRate, startDate);
        });
        assertEquals("贷款起始日期必须至少为当前日期后15天", exception.getMessage());
    }

    // 测试审批贷款
    @Test
    void approveLoan_Success() {
        // Arrange
        Long loanId = 1L;
        LoanApplication mockLoan = new LoanApplication();
        mockLoan.setLoanId(loanId);
        mockLoan.setStatus(LoanApplication.LoanStatus.PENDING);

        LoanRepayment repayment1 = new LoanRepayment();
        LoanRepayment repayment2 = new LoanRepayment();
        List<LoanRepayment> schedule = Arrays.asList(repayment1, repayment2);

        when(loanAppRepo.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(loanCalculator.generateRepaymentSchedule(mockLoan)).thenReturn(schedule);

        // Act
        LoanApplication result = loanService.approveLoan(loanId);

        // Assert
        assertEquals(LoanApplication.LoanStatus.APPROVED, result.getStatus());
        assertNotNull(result.getApprovalDate());
        verify(repaymentRepo).saveAll(schedule);
    }

    // 测试还款功能
    @Test
    void repay_Success() {
        // Arrange
        Long loanId = 1L;
        LocalDate repaymentDate = LocalDate.now();
        LoanRepayment mockRepayment = new LoanRepayment();
        mockRepayment.setStatus(LoanRepayment.RepaymentStatus.PENDING);
        mockRepayment.setAmount(new BigDecimal(856.07));
        mockRepayment.setLateFee(BigDecimal.ZERO);
        mockRepayment.setPrincipal(new BigDecimal(800));

        LoanApplication mockLoan = new LoanApplication();
        mockLoan.setLoanId(loanId);
        mockLoan.setRemainingPrincipal(new BigDecimal(9200));
        Account mockAccount = new Account();
        mockAccount.setAccountId("ACC123456");
        mockLoan.setAccount(mockAccount);
        mockRepayment.setLoan(mockLoan);

        when(repaymentRepo.findByLoan_LoanIdAndRepaymentDate(loanId, repaymentDate))
                .thenReturn(Optional.of(mockRepayment));
        doNothing().when(transactionService).transfer(anyString(), anyString(), anyDouble());
        when(repaymentRepo.countByLoanAndStatus(mockLoan, LoanRepayment.RepaymentStatus.PENDING))
                .thenReturn(0L);

        // Act
        LoanRepayment result = loanService.repay(loanId, repaymentDate);

        // Assert
        assertEquals(LoanRepayment.RepaymentStatus.PAID, result.getStatus());
        assertEquals(new BigDecimal(8400), mockLoan.getRemainingPrincipal());
        assertEquals(LoanApplication.LoanStatus.CLOSED, mockLoan.getStatus());
    }

    // 测试当期贷款已还清时异常
    @Test
    void repay_WithLoanCompleted() {
        // Arrange
        Long loanId = 1L;
        LocalDate repaymentDate = LocalDate.now();
        LoanRepayment mockRepayment = new LoanRepayment();
        mockRepayment.setStatus(LoanRepayment.RepaymentStatus.PAID);
        mockRepayment.setAmount(new BigDecimal(856.07));
        mockRepayment.setLateFee(BigDecimal.ZERO);
        mockRepayment.setPrincipal(new BigDecimal(800));

        LoanApplication mockLoan = new LoanApplication();
        mockLoan.setLoanId(loanId);
        mockLoan.setRemainingPrincipal(new BigDecimal(0));
        Account mockAccount = new Account();
        mockAccount.setAccountId("ACC123456");
        mockLoan.setAccount(mockAccount);
        mockRepayment.setLoan(mockLoan);
        when(repaymentRepo.findByLoan_LoanIdAndRepaymentDate(loanId, repaymentDate))
                .thenReturn(Optional.of(mockRepayment));
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            loanService.repay(loanId, repaymentDate);
        });
        assertEquals("该期贷款已还清", exception.getMessage());
        verify(repaymentRepo, never()).save(any());
        verify(loanAppRepo, never()).save(any());
        verify(transactionService, never()).transfer(anyString(), anyString(), anyDouble());
    }

    // 测试逾期还款场景（包含逾期费用计算）
    @Test
    void repay_WithOverdueFee() {
        // Arrange
        Long loanId = 1L;
        LocalDate repaymentDate = LocalDate.now().minusDays(3); // 逾期3天
        BigDecimal principal = new BigDecimal(1000);
        BigDecimal amount = new BigDecimal(1050);
        BigDecimal expectedLateFee = amount.multiply(BigDecimal.valueOf(0.0005))
                .multiply(BigDecimal.valueOf(3))
                .setScale(2, RoundingMode.HALF_UP); // 1050 * 0.0005 * 3 = 1.58
        BigDecimal totalAmount = amount.add(expectedLateFee);

        LoanRepayment mockRepayment = new LoanRepayment();
        mockRepayment.setStatus(LoanRepayment.RepaymentStatus.PENDING);
        mockRepayment.setAmount(amount);
        mockRepayment.setLateFee(BigDecimal.ZERO);
        mockRepayment.setPrincipal(principal);

        LoanApplication mockLoan = new LoanApplication();
        mockLoan.setLoanId(loanId);
        mockLoan.setRemainingPrincipal(new BigDecimal(5000));
        Account mockAccount = new Account();
        mockAccount.setAccountId("ACC123456");
        mockLoan.setAccount(mockAccount);
        mockRepayment.setLoan(mockLoan);

        when(repaymentRepo.findByLoan_LoanIdAndRepaymentDate(loanId, repaymentDate))
                .thenReturn(Optional.of(mockRepayment));
        doNothing().when(transactionService).transfer(eq("ACC123456"), eq("LOAN_BANK_ACCOUNT"),
                eq(totalAmount.doubleValue()));
        when(repaymentRepo.countByLoanAndStatus(mockLoan, LoanRepayment.RepaymentStatus.PENDING))
                .thenReturn(1L); // 假设还有其他未还款项

        // Act
        LoanRepayment result = loanService.repay(loanId, repaymentDate);

        // Assert
        assertEquals(LoanRepayment.RepaymentStatus.PAID, result.getStatus());
        assertEquals(expectedLateFee, result.getLateFee());
        assertEquals(LocalDate.now(), result.getActualRepaymentDate());
        assertEquals(new BigDecimal(4000), mockLoan.getRemainingPrincipal()); // 5000 - 1000 = 4000
        verify(transactionService).transfer(eq("ACC123456"), eq("LOAN_BANK_ACCOUNT"), eq(totalAmount.doubleValue()));
    }

    // 测试获取待审批贷款
    @Test
    void getPendingLoans_Success() {
        // Arrange
        List<LoanApplication> mockLoans = Arrays.asList(new LoanApplication(), new LoanApplication());
        when(loanAppRepo.findByStatus(LoanApplication.LoanStatus.PENDING)).thenReturn(mockLoans);

        // Act
        List<LoanApplication> result = loanService.getPendingLoans();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(loanAppRepo).findByStatus(LoanApplication.LoanStatus.PENDING);
    }

    // 测试拒绝贷款申请
    @Test
    void rejectLoan_Success() {
        // Arrange
        Long loanId = 1L;
        LoanApplication mockLoan = new LoanApplication();
        mockLoan.setLoanId(loanId);
        mockLoan.setStatus(LoanApplication.LoanStatus.PENDING);

        when(loanAppRepo.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(loanAppRepo.save(mockLoan)).thenReturn(mockLoan);

        // Act
        LoanApplication result = loanService.rejectLoan(loanId);

        // Assert
        assertEquals(LoanApplication.LoanStatus.REJECTED, result.getStatus());
        verify(loanAppRepo).save(mockLoan);
    }

    // 测试查询还款计划
    @Test
    void getRepaymentSchedule_Success() {
        // Arrange
        Long loanId = 1L;
        LoanApplication mockLoan = new LoanApplication();
        mockLoan.setLoanId(loanId);
        List<LoanRepayment> mockRepayments = Arrays.asList(new LoanRepayment(), new LoanRepayment());

        when(loanAppRepo.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepo.findByLoan_LoanId(loanId)).thenReturn(mockRepayments);

        // Act
        List<LoanRepayment> result = loanService.getRepaymentSchedule(loanId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // 测试获取所有贷款
    @Test
    void getAllLoans_Success() {
        // Arrange
        List<LoanApplication> mockLoans = Arrays.asList(new LoanApplication(), new LoanApplication());
        when(loanAppRepo.findAll()).thenReturn(mockLoans);

        // Act
        List<LoanApplication> result = loanService.getAllLoans();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(loanAppRepo).findAll();
    }

    @Test
    void getLoansByUserId_Success() {
        // Arrange
        Integer userId = 1;
        User user = new User();
        user.setUserId(userId);
        LoanApplication loan1 = new LoanApplication();
        loan1.setLoanId(1L);
        loan1.setUser(user);
        loan1.setStatus(LoanApplication.LoanStatus.APPROVED);

        LoanApplication loan2 = new LoanApplication();
        loan2.setLoanId(2L);
        loan2.setUser(user);
        loan2.setStatus(LoanApplication.LoanStatus.PENDING);

        List<LoanApplication> expectedLoans = Arrays.asList(loan1, loan2);

        when(loanAppRepo.findByUserUserId(userId)).thenReturn(expectedLoans);

        // Act
        List<LoanApplication> result = loanService.getLoansByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(userId, result.get(0).getUser().getUserId());
        assertEquals(userId, result.get(1).getUser().getUserId());
        verify(loanAppRepo).findByUserUserId(userId);

    }
}