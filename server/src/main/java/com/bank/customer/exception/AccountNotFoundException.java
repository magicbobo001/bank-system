package com.bank.customer.exception;

// 自定义账户未找到异常
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException() {
        super("账户不存在");
    }

    public AccountNotFoundException(String message) {
        super(message);
    }
}