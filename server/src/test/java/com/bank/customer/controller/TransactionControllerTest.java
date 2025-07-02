package com.bank.customer.controller;

import com.bank.customer.entity.Transaction;
import com.bank.customer.exception.AccountNotFoundException;
import com.bank.customer.exception.InsufficientBalanceException;
import com.bank.customer.service.TransactionService;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        // @Autowired
        // private ObjectMapper objectMapper;

        @SuppressWarnings("removal")
        @MockBean
        private TransactionService transactionService;

        @Test
        @WithMockUser(roles = "ADMIN")
        void testDeposit_Success() throws Exception {
                Transaction mockTransaction = new Transaction();
                mockTransaction.setTransactionId(123);
                mockTransaction.setAmount(BigDecimal.valueOf(1000));
                when(transactionService.deposit(eq("ACC123"), eq(1000.00))).thenReturn(mockTransaction);

                mockMvc.perform(post("/api/transactions/deposit")
                                .param("accountId", "ACC123")
                                .param("amount", "1000.0")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.transactionId").value(123));
        }

        // 存款到不存在账户
        @Test
        @WithMockUser(roles = "ADMIN")
        void testDeposit_AccountNotFound() throws Exception {
                doThrow(new AccountNotFoundException("账户不存在")).when(transactionService).deposit(eq("INVALID_ACC"),
                                eq(1000.00));

                mockMvc.perform(post("/api/transactions/deposit")
                                .param("accountId", "INVALID_ACC")
                                .param("amount", "1000.0")
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(content().string(containsString("账户不存在")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testWithdraw_Success() throws Exception {
                Transaction mockTransaction = new Transaction();
                mockTransaction.setTransactionId(456);
                mockTransaction.setAmount(new BigDecimal(2000));
                when(transactionService.withdraw(eq("ACC123"), eq(new BigDecimal(2000)))).thenReturn(mockTransaction);

                mockMvc.perform(post("/api/transactions/withdraw")
                                .param("accountId", "ACC123")
                                .param("amount", "2000")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.transactionId").value(456));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testWithdraw_InsufficientFunds() throws Exception {
                doThrow(new InsufficientBalanceException()).when(transactionService).withdraw(eq("ACC123"),
                                eq(new BigDecimal("5000.0")));

                mockMvc.perform(post("/api/transactions/withdraw")
                                .param("accountId", "ACC123")
                                .param("amount", "5000.0")
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(containsString("账户余额不足")));
        }

        // 取款账户不存在
        @Test
        @WithMockUser(roles = "ADMIN")
        void testWithdraw_AccountNotFound() throws Exception {
                doThrow(new AccountNotFoundException("账户不存在")).when(transactionService).withdraw(eq("INVALID_ACC"),
                                eq(new BigDecimal(1000)));

                mockMvc.perform(post("/api/transactions/withdraw")
                                .param("accountId", "INVALID_ACC")
                                .param("amount", "1000")
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(content().string(containsString("账户不存在")));
        }

        // 无效金额取款
        @Test
        @WithMockUser(roles = "ADMIN")
        void testWithdraw_InvalidAmount() throws Exception {
                mockMvc.perform(post("/api/transactions/withdraw")
                                .param("accountId", "ACC123")
                                .param("amount", "0")
                                .with(csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testTransfer_Success() throws Exception {
                mockMvc.perform(post("/api/transactions/transfer")
                                .param("fromAccountId", "ACC123")
                                .param("toAccountId", "ACC456")
                                .param("amount", "500")
                                .with(csrf()))
                                .andExpect(status().isOk());

                verify(transactionService).transfer(eq("ACC123"), eq("ACC456"), eq(500.0));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testTransfer_InsufficientFunds() throws Exception {
                // TransactionService的transfer方法参数是Double类型，匹配mock参数
                doThrow(new InsufficientBalanceException())
                                .when(transactionService).transfer(eq("ACC123"), eq("ACC456"), eq(10000.0));

                mockMvc.perform(post("/api/transactions/transfer")
                                .param("fromAccountId", "ACC123")
                                .param("toAccountId", "ACC456")
                                .param("amount", "10000")
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(containsString("账户余额不足")));
        }

        // 转账账户不存在
        @Test
        @WithMockUser(roles = "ADMIN")
        void testTransfer_AccountNotFound() throws Exception {
                doThrow(new AccountNotFoundException("账户不存在")).when(transactionService).transfer(eq("INVALID_ACC"),
                                eq("ACC456"), eq(500.0));

                mockMvc.perform(post("/api/transactions/transfer")
                                .param("fromAccountId", "INVALID_ACC")
                                .param("toAccountId", "ACC456")
                                .param("amount", "500")
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(content().string(containsString("账户不存在")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testGetTransactionHistory() throws Exception {
                LocalDate startDate = LocalDate.of(2023, 1, 1);
                LocalDate endDate = LocalDate.of(2023, 12, 31);

                // 创建测试交易数据（根据Transaction实体类调整字段）
                Transaction transaction1 = new Transaction();
                transaction1.setTransactionId(1);
                transaction1.setToAccountId("ACC123"); // 存款交易的目标账户
                transaction1.setAmount(new BigDecimal(1000));
                transaction1.setTransactionType("deposit");
                transaction1.setTransactionTime(LocalDateTime.of(2023, 6, 15, 10, 0)); // 改为LocalDateTime

                Transaction transaction2 = new Transaction();
                transaction2.setTransactionId(2);
                transaction2.setFromAccountId("ACC123"); // 取款交易的源账户
                transaction2.setAmount(new BigDecimal(500));
                transaction2.setTransactionType("withdraw");
                transaction2.setTransactionTime(LocalDateTime.of(2023, 6, 20, 14, 30)); // 改为LocalDateTime

                List<Transaction> mockTransactions = Arrays.asList(transaction1, transaction2);
                when(transactionService.getTransactionHistory(eq("ACC123"), eq(startDate), eq(endDate)))
                                .thenReturn(mockTransactions);

                mockMvc.perform(get("/api/transactions/ACC123/history")
                                .param("accountId", "ACC123")
                                .param("startDate", "2023-01-01")
                                .param("endDate", "2023-12-31")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.size()").value(2))
                                .andExpect(jsonPath("$[0].transactionId").value(1))
                                .andExpect(jsonPath("$[1].amount").value(500));
        }

        // 测试存款接口权限不足
        @Test
        @WithMockUser(roles = "USER")
        void testDeposit_AccessDenied() throws Exception {
                mockMvc.perform(post("/api/transactions/deposit")
                                .param("accountId", "ACC123")
                                .param("amount", "1000.0")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        // 测试取款接口权限不足
        @Test
        @WithMockUser(roles = "USER")
        void testWithdraw_AccessDenied() throws Exception {
                mockMvc.perform(post("/api/transactions/withdraw")
                                .param("accountId", "ACC123")
                                .param("amount", "1000.0")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        // 测试转账接口权限不足
        @Test
        @WithMockUser(roles = "USER")
        void testTransfer_AccessDenied() throws Exception {
                mockMvc.perform(post("/api/transactions/transfer")
                                .param("fromAccountId", "ACC123")
                                .param("toAccountId", "ACC456")
                                .param("amount", "500")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }
}