package com.bank.customer.service;

import com.bank.customer.entity.Account;
import com.bank.customer.entity.AccountStatus;
import com.bank.customer.entity.Transaction;
import com.bank.customer.exception.AccountNotFoundException;
import com.bank.customer.exception.AccountStatusException;
import com.bank.customer.exception.InsufficientBalanceException;
import com.bank.customer.repository.AccountRepository;
import com.bank.customer.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    // 测试存款成功
    @Test
    void deposit_Success() {
        // Arrange
        String accountId = "ACC123";
        Double amount = 1000.0;
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.ACTIVE);
        mockAccount.setBalance(BigDecimal.valueOf(5000.0));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Transaction result = transactionService.deposit(accountId, amount);

        // Assert
        assertNotNull(result);
        assertEquals("deposit", result.getTransactionType());
        assertEquals(BigDecimal.valueOf(amount), result.getAmount());
        assertEquals(accountId, result.getToAccountId());
        verify(accountRepository).save(mockAccount);
        assertEquals(BigDecimal.valueOf(6000.0), mockAccount.getBalance());
    }

    // 测试存款-账户不存在
    @Test
    void deposit_AccountNotFound() {
        // Arrange
        String accountId = "INVALID_ACC";
        Double amount = 1000.0;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            transactionService.deposit(accountId, amount);
        });
        verify(accountRepository, never()).save(any());
    }

    // 测试存款-账户非活跃
    @Test
    void deposit_AccountInactive() {
        // Arrange
        String accountId = "ACC123";
        Double amount = 1000.0;
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.FROZEN);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act & Assert
        assertThrows(AccountStatusException.class, () -> {
            transactionService.deposit(accountId, amount);
        });
        verify(accountRepository, never()).save(any());
    }

    // 测试取款成功
    @Test
    void withdraw_Success() {
        // Arrange
        String accountId = "ACC123";
        BigDecimal amount = new BigDecimal(2000);
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.ACTIVE);
        mockAccount.setBalance(BigDecimal.valueOf(5000));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Transaction result = transactionService.withdraw(accountId, amount);

        // Assert
        assertNotNull(result);
        assertEquals("withdraw", result.getTransactionType());
        assertEquals(amount, result.getAmount());
        assertEquals(accountId, result.getFromAccountId());
        verify(accountRepository).save(mockAccount);
        assertEquals(BigDecimal.valueOf(3000), mockAccount.getBalance());
    }

    // 测试取款-余额不足
    @Test
    void withdraw_InsufficientFunds() {
        // Arrange
        String accountId = "ACC123";
        BigDecimal amount = new BigDecimal(6000);
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.ACTIVE);
        mockAccount.setBalance(BigDecimal.valueOf(5000));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.withdraw(accountId, amount);
        });
        verify(accountRepository, never()).save(any());
    }

    // 测试转账成功
    @Test
    void transfer_Success() {
        // Arrange
        String fromAccountId = "ACC123";
        String toAccountId = "ACC456";
        Double amount = 1000.0;
        Account fromAccount = new Account();
        fromAccount.setAccountId(fromAccountId);
        fromAccount.setStatus(AccountStatus.ACTIVE);
        Account toAccount = new Account();
        toAccount.setAccountId(toAccountId);
        toAccount.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));
        when(accountRepository.getBalance(fromAccountId)).thenReturn(BigDecimal.valueOf(5000));
        doNothing().when(transactionRepository).transferFunds(eq(fromAccountId), eq(toAccountId), eq(amount));

        // Act
        transactionService.transfer(fromAccountId, toAccountId, amount);

        // Assert
        verify(transactionRepository).transferFunds(eq(fromAccountId), eq(toAccountId), eq(amount));
    }

    // 测试转账余额不足
    @Test
    void transfer_InsufficientFunds() {
        // Arrange
        String fromAccountId = "ACC123";
        String toAccountId = "ACC456";
        Double amount = 1000.0;
        Account fromAccount = new Account();
        fromAccount.setAccountId(fromAccountId);
        fromAccount.setStatus(AccountStatus.ACTIVE);
        Account toAccount = new Account();
        toAccount.setAccountId(toAccountId);
        toAccount.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));
        when(accountRepository.getBalance(fromAccountId)).thenReturn(BigDecimal.valueOf(500));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.transfer(fromAccountId, toAccountId, amount);
        });
    }

    // 测试转账转出账户不存在
    @Test
    void transfer_ToAccountNotFound() {
        // Arrange
        String fromAccountId = "ACC123";
        String toAccountId = "ACC456";
        Double amount = 1000.0;
        Account fromAccount = new Account();
        fromAccount.setAccountId(fromAccountId);
        fromAccount.setStatus(AccountStatus.ACTIVE);
        Account toAccount = new Account();
        toAccount.setAccountId(toAccountId);
        toAccount.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            transactionService.transfer(fromAccountId, toAccountId, amount);
        });
    }

    // 测试转账转入账户不存在
    @Test
    void transfer_FromAccountNotFound() {
        // Arrange
        String fromAccountId = "ACC123";
        String toAccountId = "ACC456";
        Double amount = 1000.0;
        Account fromAccount = new Account();
        fromAccount.setAccountId(fromAccountId);
        fromAccount.setStatus(AccountStatus.ACTIVE);
        Account toAccount = new Account();
        toAccount.setAccountId(toAccountId);
        toAccount.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            transactionService.transfer(fromAccountId, toAccountId, amount);
        });
    }

    // 测试获取交易历史
    @Test
    void getTransactionHistory_Success() {
        // Arrange
        String accountId = "ACC123";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        List<Transaction> mockTransactions = Arrays.asList(new Transaction(), new Transaction());

        when(transactionRepository.findByAccountAndDateRange(eq(accountId), any(), any())).thenReturn(mockTransactions);

        // Act
        List<Transaction> result = transactionService.getTransactionHistory(accountId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}