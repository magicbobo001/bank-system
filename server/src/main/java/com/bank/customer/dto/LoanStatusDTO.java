package com.bank.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanStatusDTO(
        Long loanId,
        Integer userId,
        String status,
        String accountId,
        BigDecimal amount,
        Integer term,
        BigDecimal interestRate,
        LocalDate startDate,
        LocalDate endDate) {
}