package com.bank.customer.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@Table(name = "account")
@FilterDef(name = "activeAccountFilter", parameters = @ParamDef(name = "status", type = String.class))
@Filter(name = "activeAccountFilter", condition = "status = :status")
@Data
public class Account {
    @Id
    @Column(name = "account_id")
    private String accountId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String accountType; // 储蓄(savings)/支票(checking)

    @Column(precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE; // 默认状态为 ACTIVE

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt; // 确保字段存在

    @Column(nullable = false)
    private boolean hasOverdue;
}