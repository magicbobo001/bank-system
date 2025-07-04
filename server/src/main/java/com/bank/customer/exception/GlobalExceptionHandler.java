package com.bank.customer.exception;

import com.bank.customer.dto.ApiError;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleAccountNotFound(AccountNotFoundException e) {
        return ApiError.of(HttpStatus.NOT_FOUND, "账户不存在");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUsernameNotFound(UsernameNotFoundException e) {
        return ApiError.of(HttpStatus.NOT_FOUND, "用户不存在");
    }

    @ExceptionHandler(InvalidUsernameOrPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleInvalidUsernameOrPassword(InvalidUsernameOrPasswordException e) {
        return ApiError.of(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(InvalidDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidDate(InvalidDateException e) {
        return ApiError.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(LoanNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleLoanNotFound(LoanNotFoundException e) {
        return ApiError.of(HttpStatus.NOT_FOUND, "贷款不存在");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleRuntimeException(RuntimeException e) {
        return e.getMessage();
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInsufficientBalance(InsufficientBalanceException e) {
        return ApiError.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ConstraintViolationException e) {
        return "请求参数不合法: " + e.getMessage();
    }

    @ExceptionHandler(RepaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleRepaymentNotFound(RepaymentNotFoundException e) {
        return ApiError.of(HttpStatus.NOT_FOUND, "还款记录不存在");
    }
}