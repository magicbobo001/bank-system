// server/src/main/java/com/bank/customer/repository/TransactionRepository.java
package com.bank.customer.repository;

import com.bank.customer.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Procedure(procedureName = "sp_transfer")
    void transferFunds(
        @Param("p_from_account_id") String fromAccountId,
        @Param("p_to_account_id") String toAccountId,
        @Param("p_amount") Double amount
    );

    // 根据账户查询交易记录
    List<Transaction> findByFromAccountIdOrToAccountId(String accountId, String sameAccountId);
}