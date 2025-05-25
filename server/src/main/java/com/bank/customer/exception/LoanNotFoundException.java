package com.bank.customer.exception;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException() {
        super("贷款不存在");
    }
}