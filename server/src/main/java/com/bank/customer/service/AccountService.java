package com.bank.customer.service;

import com.bank.customer.entity.Account;
import com.bank.customer.entity.AccountStatus;
import com.bank.customer.entity.AuditLog;
import com.bank.customer.entity.User;
import com.bank.customer.exception.AccountNotFoundException;
import com.bank.customer.repository.AccountRepository;
import com.bank.customer.repository.AuditLogRepository;
import com.bank.customer.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.hibernate.Session;
import lombok.RequiredArgsConstructor;

import org.hibernate.Filter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
     // ==== 开户（管理员权限） ====
    public Account createAccount(Integer userId, String accountType) {
        @SuppressWarnings("unused")
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        String accountId = accountRepository.createAccount(userId, accountType);
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("开户失败"));
    }

    // ==== 查看用户所有账户（普通用户） ====
    public List<Account> getUserAccounts(Integer userId) {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("activeAccountFilter");
        filter.setParameter("isDeleted", false);
        return accountRepository.findByUserUserId(userId);
    }

    // ==== 注销或删除账户（设为 CLOSED） ====
    public void closeAccount(String accountId, Integer operatorId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(AccountNotFoundException::new);
        account.setStatus(AccountStatus.CLOSED);
        account.setBalance(BigDecimal.ZERO);
        account.setClosedAt(LocalDateTime.now());
        accountRepository.save(account);
        auditLogRepository.save(
            new AuditLog(null, "CLOSE", accountId, operatorId, LocalDateTime.now())
        );
    }

    // ==== 管理员冻结账户 ====
    public void freezeAccount(String accountId, Integer operatorId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(AccountNotFoundException::new);
        
        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
        
        auditLogRepository.save(
            new AuditLog(null, "FREEZE", accountId, operatorId, LocalDateTime.now())
        );
    }

    // ==== 管理员解冻账户 ====
    public void unfreezeAccount(String accountId, Integer operatorId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(AccountNotFoundException::new);
        
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        
        auditLogRepository.save(
            new AuditLog(null, "UNFREEZE", accountId, operatorId, LocalDateTime.now())
        );
    }
    // ==== 恢复账户（设为 ACTIVE） ====
    public Account restoreAccount(String accountId, Integer operatorId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(AccountNotFoundException::new);
        account.setStatus(AccountStatus.ACTIVE);
        account.setClosedAt(null);
        accountRepository.save(account);
        auditLogRepository.save(
            new AuditLog(null, "RESTORE", accountId, operatorId, LocalDateTime.now())
        );
        return account;
    }

    // ==== 查看已删除账户 ====
    public List<Account> getDeletedAccounts() {
        return accountRepository.findByStatus(AccountStatus.CLOSED);
    }    
}