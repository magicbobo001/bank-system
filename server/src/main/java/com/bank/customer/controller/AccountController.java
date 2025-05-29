package com.bank.customer.controller;

import com.bank.customer.entity.Account;
import com.bank.customer.entity.AccountStatus;
import com.bank.customer.service.AccountService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    // 管理员开户
    @PostMapping("/create")
    public Account createAccount(
            @RequestParam Integer userId,
            @RequestParam String accountType) {
        return accountService.createAccount(userId, accountType);
    }

    // 用户查看自己的账户
    @GetMapping("/my-accounts")
    public List<Account> getMyAccounts(@RequestParam Integer userId) {
        return accountService.getUserAccounts(userId);
    }

    // ==== 客户注销或管理员删除（合并接口） ====
    @DeleteMapping("/{accountId}")
    public void closeAccount(
            @PathVariable String accountId,
            @RequestParam Integer operatorId // 操作人 ID（从 Token 或 Session 获取）
    ) {
        accountService.closeAccount(accountId, operatorId);
    }

    // ==== 管理员冻结账户 ====
    @PutMapping("/{accountId}/freeze")
    public void freezeAccount(
            @PathVariable String accountId,
            @RequestParam Integer operatorId) {
        accountService.freezeAccount(accountId, operatorId);
    }

    // ==== 管理员解冻账户 ====
    @PutMapping("/{accountId}/unfreeze")
    public void unfreezeAccount(
            @PathVariable String accountId,
            @RequestParam Integer operatorId) {
        accountService.unfreezeAccount(accountId, operatorId);
    }

    @PutMapping("/{accountId}/restore")
    public Account restoreAccount(@PathVariable String accountId, @RequestParam Integer operatorId) {
        return accountService.restoreAccount(accountId, operatorId);
    }

    @GetMapping("/admin")
    public Page<Account> getAllAccounts(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return accountService.getAllAccounts(status, page, size);
    }
}