// server/src/main/java/com/bank/customer/controller/TransactionController.java
package com.bank.customer.controller;

import com.bank.customer.entity.Transaction;
import com.bank.customer.service.TransactionService;

import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public Transaction deposit(
            @RequestParam String accountId,
            @RequestParam Double amount) {
        return transactionService.deposit(accountId, amount);
    }

    // 取款接口
    @PostMapping("/withdraw")
    public Transaction withdraw(
            @RequestParam String accountId,
            @RequestParam @DecimalMin("0.01") BigDecimal amount) {
        return transactionService.withdraw(accountId, amount);
    }

    @PostMapping("/transfer")
    public void transfer(
            @RequestParam String fromAccountId,
            @RequestParam String toAccountId,
            @RequestParam Double amount) {
        transactionService.transfer(fromAccountId, toAccountId, amount);
    }

    @GetMapping("/{accountId}/history")
    public List<Transaction> getTransactionHistory(
            @PathVariable String accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return transactionService.getTransactionHistory(accountId, startDate, endDate);
    }
}