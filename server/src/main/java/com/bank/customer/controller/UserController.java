package com.bank.customer.controller;

import com.bank.customer.entity.User;
import com.bank.customer.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/me")
    public User getMyProfile(@RequestParam Integer userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/me")
    public User updateProfile(@RequestBody User updatedUser) {
        return userService.updateUser(updatedUser);
    }
}