// server/src/main/java/com/bank/customer/service/TransactionService.java
package com.bank.customer.service;

import com.bank.customer.entity.Account;
import com.bank.customer.entity.AccountStatus;
import com.bank.customer.entity.Transaction;
import com.bank.customer.exception.AccountNotFoundException;
import com.bank.customer.exception.AccountStatusException;
import com.bank.customer.exception.InsufficientBalanceException;
import com.bank.customer.repository.AccountRepository;
import com.bank.customer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // 存款
    public Transaction deposit(String accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(AccountNotFoundException::new);
        checkAccountActive(account); // 状态校验    
        // 转换为 BigDecimal
        BigDecimal depositAmount = BigDecimal.valueOf(amount);
        account.setBalance(account.getBalance().add(depositAmount));
        accountRepository.save(account);
        
        return transactionRepository.save(
            new Transaction(
                null,
                null,
                accountId,
                depositAmount, // 使用 BigDecimal 类型
                "deposit",
                LocalDateTime.now(),
                null
            )
        );
    }
    // 取款方法
    public Transaction withdraw(String accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(AccountNotFoundException::new);
        checkAccountActive(account); // 状态校验
        // 检查余额是否足够
        BigDecimal currentBalance = account.getBalance();
        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        // 扣除余额
        account.setBalance(currentBalance.subtract(amount));
        accountRepository.save(account);

        // 记录交易
        return transactionRepository.save(
            new Transaction(
                null,            // transaction_id (自动生成)
                accountId,       // from_account_id (取款账户)
                null,            // to_account_id (无转入账户)
                amount,
                "withdraw",      // 交易类型
                LocalDateTime.now(),
                null             // description
            )
        );
    }
    
    // 转账（调用存储过程）
    public void transfer(String fromAccountId, String toAccountId, Double amount) {
        // 检查账户是否存在
        Account fromAccount = accountRepository.findById(fromAccountId)
            .orElseThrow(() -> new AccountNotFoundException("转出账户不存在"));
        Account toAccount = accountRepository.findById(toAccountId)
            .orElseThrow(() -> new AccountNotFoundException("转入账户不存在"));
        checkAccountActive(fromAccount);
        checkAccountActive(toAccount);
        // 检查余额
        BigDecimal currentBalance = accountRepository.getBalance(fromAccountId);
        BigDecimal transferAmount = BigDecimal.valueOf(amount);
        if (currentBalance.compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException();
        }
        // 调用存储过程
        transactionRepository.transferFunds(fromAccountId, toAccountId, transferAmount.doubleValue());
    }
    
    // ==== 状态校验工具方法 ====
    private void checkAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountStatusException("账户状态异常: " + account.getStatus());
        }
    }
}