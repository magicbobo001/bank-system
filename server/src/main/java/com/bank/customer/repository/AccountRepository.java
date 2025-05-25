package com.bank.customer.repository;

import com.bank.customer.entity.Account;
import com.bank.customer.entity.AccountStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    // 根据用户ID查找账户
    List<Account> findByUserUserId(Integer userId);

    // 调用存储过程（按名称绑定参数）
    @Procedure(procedureName = "sp_create_account")
    String createAccount(
        @Param("p_user_id") Integer userId,
        @Param("p_account_type") String accountType
    );
    // 根据状态查询账户
    List<Account> findByStatus(AccountStatus status);

    // 添加余额查询方法
    @Query("SELECT a.balance FROM Account a WHERE a.accountId = :accountId")
    BigDecimal getBalance(@Param("accountId") String accountId);
}