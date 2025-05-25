// server/src/main/java/com/bank/customer/entity/Transaction.java
package com.bank.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor // 添加 Lombok 全参构造函数注解
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @Column(name = "from_account_id")
    private String fromAccountId;

    @Column(name = "to_account_id")
    private String toAccountId;

    @Column(precision = 15, scale = 2) // 明确指定精度和小数位
    private BigDecimal amount; // 改为 BigDecimal

    @Column(nullable = false)
    private String transactionType; // deposit/withdraw/transfer

    @Column(nullable = false)
    private LocalDateTime transactionTime = LocalDateTime.now();

    private String description;
}