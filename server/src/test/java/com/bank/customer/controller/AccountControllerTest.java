package com.bank.customer.controller;

import com.bank.customer.entity.Account;
import com.bank.customer.entity.AccountStatus;
import com.bank.customer.exception.AccountNotFoundException;
import com.bank.customer.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import java.util.Collections;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private AccountService accountService;

    // ==== 正常场景测试 ====
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAccount_Success() throws Exception {
        Account mockAccount = new Account();
        mockAccount.setAccountId("ACC123");
        mockAccount.setAccountType("SAVINGS");
        when(accountService.createAccount(1, "SAVINGS")).thenReturn(mockAccount);

        mockMvc.perform(post("/api/accounts/create")
                .param("userId", "1")
                .param("accountType", "SAVINGS")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC123"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetMyAccounts_Success() throws Exception {
        Account account = new Account();
        account.setAccountId("ACC123");
        account.setAccountType("SAVINGS");
        when(accountService.getUserAccounts(1)).thenReturn(Collections.singletonList(account));

        mockMvc.perform(get("/api/accounts/my-accounts")
                .param("userId", "1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value("ACC123"));
    }

    // 测试删除账户成功场景
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteAccount_Success() throws Exception {
        Account mockAccount = new Account();
        mockAccount.setAccountId("ACC123");
        mockAccount.setStatus(AccountStatus.ACTIVE);
        doNothing().when(accountService).closeAccount("ACC123", 1);

        mockMvc.perform(delete("/api/accounts/ACC123")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // 测试冻结账户成功场景
    @Test
    @WithMockUser(roles = "ADMIN")
    void testFreezeAccount_Success() throws Exception {
        Account mockAccount = new Account();
        mockAccount.setAccountId("ACC123");
        mockAccount.setStatus(AccountStatus.ACTIVE);
        doNothing().when(accountService).freezeAccount("ACC123", 1);

        mockMvc.perform(put("/api/accounts/ACC123/freeze")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // 测试解冻账户成功场景
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUnfreezeAccount_Success() throws Exception {
        Account mockAccount = new Account();
        mockAccount.setAccountId("ACC123");
        mockAccount.setStatus(AccountStatus.FROZEN);
        doNothing().when(accountService).unfreezeAccount("ACC123", 1);

        mockMvc.perform(put("/api/accounts/ACC123/unfreeze")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // 测试恢复账户成功场景
    @Test
    @WithMockUser(roles = "ADMIN")
    void testRestoreAccount_Success() throws Exception {
        Account mockAccount = new Account();
        mockAccount.setAccountId("ACC123");
        mockAccount.setStatus(AccountStatus.CLOSED);
        when(accountService.restoreAccount("ACC123", 1)).thenReturn(mockAccount);

        mockMvc.perform(put("/api/accounts/ACC123/restore")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // 测试管理员查询账户列表
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllAccounts_Success() throws Exception {
        Page<Account> accountPage = new PageImpl<>(Collections.singletonList(new Account()));
        when(accountService.getAllAccounts(any(), anyInt(), anyInt())).thenReturn(accountPage);

        mockMvc.perform(get("/api/accounts/admin")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // ==== 异常场景测试 ====
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCloseAccount_AccountNotFound() throws Exception {
        doThrow(new AccountNotFoundException("账户不存在")).when(accountService).closeAccount("ACC999", 1);

        mockMvc.perform(delete("/api/accounts/ACC999")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("账户不存在")));
    }

    // 测试冻结不存在账户
    @Test
    @WithMockUser(roles = "ADMIN")
    void testFreezeAccount_AccountNotFound() throws Exception {
        doThrow(new AccountNotFoundException("账户不存在")).when(accountService).freezeAccount("NOT_EXIST", 1);

        mockMvc.perform(put("/api/accounts/NOT_EXIST/freeze")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("账户不存在")));
    }

    // 测试解冻不存在账户
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUnfreezeAccount_AccountNotFound() throws Exception {
        doThrow(new AccountNotFoundException("账户不存在")).when(accountService).unfreezeAccount("NOT_EXIST", 1);

        mockMvc.perform(put("/api/accounts/NOT_EXIST/unfreeze")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("账户不存在")));
    }

    // 测试恢复不存在账户
    @Test
    @WithMockUser(roles = "ADMIN")
    void testRestoreAccount_AccountNotFound() throws Exception {
        doThrow(new AccountNotFoundException("账户不存在")).when(accountService).restoreAccount("NOT_EXIST", 1);

        mockMvc.perform(put("/api/accounts/NOT_EXIST/restore")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ==== 权限测试 ====
    @Test
    @WithMockUser(roles = "USER")
    void testAdminEndpoint_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/accounts/admin")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // 测试普通用户访问管理员接口权限拒绝
    @Test
    @WithMockUser(roles = "USER")
    void testCreateAccount_AccessDenied() throws Exception {
        mockMvc.perform(post("/api/accounts/create")
                .param("userId", "1")
                .param("accountType", "SAVINGS")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // 测试删除账户权限不足
    @Test
    @WithMockUser(roles = "USER")
    void testDeleteAccount_AccessDenied() throws Exception {
        mockMvc.perform(delete("/api/accounts/ACC123")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // 测试冻结账户权限不足
    @Test
    @WithMockUser(roles = "USER")
    void testFreezeAccount_AccessDenied() throws Exception {
        mockMvc.perform(put("/api/accounts/ACC123/freeze")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // 测试解冻账户权限不足
    @Test
    @WithMockUser(roles = "USER")
    void testUnfreezeAccount_AccessDenied() throws Exception {
        mockMvc.perform(put("/api/accounts/ACC123/unfreeze")
                .param("operatorId", "1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // 测试查询账户时状态参数验证
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllAccounts_InvalidStatus() throws Exception {
        mockMvc.perform(get("/api/accounts/admin")
                .param("status", "INVALID_STATUS")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // 测试创建账户时无效账户类型
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAccount_InvalidType() throws Exception {
        doThrow(new IllegalArgumentException("无效的账户类型")).when(accountService).createAccount(1, "INVALID_TYPE");

        mockMvc.perform(post("/api/accounts/create")
                .param("userId", "1")
                .param("accountType", "INVALID_TYPE")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}