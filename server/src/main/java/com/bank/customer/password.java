package com.bank.customer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class password {

    public static void main(String[] args) {
        String encodedPwd = new BCryptPasswordEncoder().encode("123456");
        System.out.println(encodedPwd); // 复制输出值到SQL
    }

}
