package com.bank.customer.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super("账户余额不足");
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
