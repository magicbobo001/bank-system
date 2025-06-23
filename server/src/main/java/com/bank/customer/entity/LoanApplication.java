package com.bank.customer.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "loan_application")
@Data
@JsonIgnoreProperties({ "approvedBy", "user" })
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long loanId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // 贷款总额

    @Column(nullable = false)
    private Integer term; // 贷款期限（月）

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal interestRate; // 年利率

    @Column(nullable = false)
    private LocalDate startDate; // 贷款开始日期

    @Column(nullable = false)
    private LocalDate endDate; // 贷款结束日期

    @Column
    private LocalDateTime approvalDate; // 贷款审批通过日期

    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyPayment; // 每月还款额

    @Column(precision = 15, scale = 2)
    private BigDecimal remainingPrincipal; // 剩余本金

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.PENDING; // 贷款状态
    @Column(nullable = true)
    private LocalDate disbursementDate; // 放款日期
    // 贷款状态枚举

    public enum LoanStatus {
        PENDING, // 与数据库定义中的'pending'不一致
        APPROVED, // 对应数据库中的'approved'
        REJECTED, // 对应数据库中的'rejected'
        DISBURSED, // 新增状态对应数据库中的'disbursed'
        CLOSED, // 需要数据库添加该状态到CHECK约束
        DEFAULT // 新增状态对应数据库中的'defaulted'
    }
}