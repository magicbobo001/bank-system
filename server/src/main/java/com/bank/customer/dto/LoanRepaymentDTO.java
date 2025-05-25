package com.bank.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.bank.customer.entity.LoanRepayment;
public record LoanRepaymentDTO(
    Long repaymentId,
    Long loanId,
    LocalDate repaymentDate,
    BigDecimal amount,
    BigDecimal principal,
    BigDecimal interest,
    String status  // 保持String类型
) {
    public static LoanRepaymentDTO fromEntity(LoanRepayment repayment) {
        return new LoanRepaymentDTO(
            repayment.getRepaymentId(),
            repayment.getLoan().getLoanId(),
            repayment.getRepaymentDate(),
            repayment.getAmount(),
            repayment.getPrincipal(),
            repayment.getInterest(),
            repayment.getStatus().name()  // 转换枚举值为字符串
        );
    }
}
