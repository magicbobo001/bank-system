package com.bank.customer.exception;

public class InvalidUsernameOrPasswordException extends RuntimeException {
    public InvalidUsernameOrPasswordException() {
        super("用户名或密码错误");
    }

    public InvalidUsernameOrPasswordException(String message) {
        super(message);
    }
}
