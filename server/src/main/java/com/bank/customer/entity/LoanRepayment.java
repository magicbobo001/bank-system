package com.bank.customer.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "loan_repayment")
@Data
public class LoanRepayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repaymentId;

    // 添加JSON序列化过滤注解
    @JsonIgnoreProperties({ "user", "account" })
    @ManyToOne
    @JoinColumn(name = "loan_id", referencedColumnName = "loan_id", nullable = false)
    private LoanApplication loan;

    @Column(nullable = false)
    private LocalDate repaymentDate; // 还款日期

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // 还款总额

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principal; // 本金部分

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interest; // 利息部分

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepaymentStatus status = RepaymentStatus.PENDING; // 还款状态

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO; // 滞纳金字段（与数据库late_fee列对应）

    @Column(nullable = true)
    private LocalDate actualRepaymentDate; // 实际还款日期

    // 还款状态枚举
    public enum RepaymentStatus {
        PENDING, // 待还款
        OVERDUE, // 逾期
        PAID // 已还款
    }
}