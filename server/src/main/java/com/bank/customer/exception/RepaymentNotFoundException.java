package com.bank.customer.exception;

public class RepaymentNotFoundException extends RuntimeException {
    public RepaymentNotFoundException() {
        super("还款记录不存在");
    }
}