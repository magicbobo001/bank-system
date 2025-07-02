package com.bank.customer.controller;

import com.bank.customer.dto.LoanApplicationRequest;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanApplication.LoanStatus;
import com.bank.customer.exception.InvalidDateException;
import com.bank.customer.exception.LoanNotFoundException;
import com.bank.customer.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.bank.customer.entity.LoanRepayment;
import com.bank.customer.entity.User;
import com.bank.customer.exception.RepaymentNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import com.bank.customer.config.SecurityConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.bank.customer.service.UserService;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class LoanControllerTest {

        @SuppressWarnings("removal")
        @MockBean
        private LoanService loanService;

        @SuppressWarnings("removal")
        @MockBean
        private UserService userService;

        @SuppressWarnings("removal")
        @MockBean
        private EntityManager entityManager;

        @SuppressWarnings("removal")
        @MockBean
        private PlatformTransactionManager transactionManager;

        @Autowired
        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
        }

        private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        @Test
        @WithMockUser(roles = "USER")
        void testApplyLoan_Success() throws Exception {
                // 准备测试数据
                LoanApplicationRequest request = new LoanApplicationRequest();
                request.setUserId(1);
                request.setAccountId("ACC123456");
                request.setAmount(new BigDecimal(10000));
                request.setTerm(12);
                request.setAnnualRate(new BigDecimal(5.5));
                LocalDate expectedStartDate = LocalDate.now().plusDays(15);

                request.setStartDate(expectedStartDate);
                LoanApplication mockResponse = new LoanApplication();
                mockResponse.setLoanId(1L);
                mockResponse.setStatus(LoanStatus.PENDING);

                // Mock服务层方法
                when(loanService.applyLoan(eq(1), eq("ACC123456"), eq(new BigDecimal(10000)), eq(12),
                                eq(new BigDecimal(5.5)),
                                eq(expectedStartDate))).thenReturn(mockResponse);

                // 执行测试
                mockMvc.perform(post("/api/loans/apply")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.loanId").value(1))
                                .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @WithMockUser(roles = "USER")
        void testApplyLoan_InvalidStartDate() throws Exception {
                // 准备测试数据 - 开始日期设置为当前日期前
                LoanApplicationRequest request = new LoanApplicationRequest();
                request.setUserId(1);
                request.setAccountId("ACC123456");
                request.setAmount(new BigDecimal(10000));
                request.setTerm(12);
                request.setAnnualRate(new BigDecimal(5.5));
                request.setStartDate(LocalDate.now().minusDays(1));

                // Mock服务层抛出非法参数异常
                when(loanService.applyLoan(any(), any(), any(), any(), any(), any()))
                                .thenThrow(new InvalidDateException("贷款起始日期必须至少为当前日期后15天"));

                // 执行测试并验证错误响应
                mockMvc.perform(post("/api/loans/apply")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testApproveLoan_Success() throws Exception {
                LoanApplication mockResponse = new LoanApplication();
                mockResponse.setLoanId(1L);
                mockResponse.setStatus(LoanStatus.APPROVED);

                when(loanService.approveLoan(any(Long.class))).thenReturn(mockResponse);

                mockMvc.perform(put("/api/loans/1/approve").with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        // 审批不存在贷款
        @Test
        @WithMockUser(roles = "ADMIN")
        void testApproveLoan_LoanNotFound() throws Exception {

                when(loanService.approveLoan(eq(999L))).thenThrow(new LoanNotFoundException("贷款不存在"));

                mockMvc.perform(put("/api/loans/999/approve").with(csrf()))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER") // 普通用户无权审批贷款
        void testApproveLoan_AccessDenied() throws Exception {
                mockMvc.perform(put("/api/loans/1/approve").with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testRejectLoan_Success() throws Exception {
                LoanApplication mockResponse = new LoanApplication();
                mockResponse.setLoanId(1L);
                mockResponse.setStatus(LoanStatus.REJECTED);

                when(loanService.rejectLoan(eq(1L))).thenReturn(mockResponse);

                mockMvc.perform(put("/api/loans/1/reject").with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("REJECTED"));
        }

        @Test
        @WithMockUser(roles = "USER")
        void testRejectLoan_AccessDenied() throws Exception {
                mockMvc.perform(put("/api/loans/1/reject").with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testRepayLoan_Success() throws Exception {
                LocalDate repaymentDate = LocalDate.now().plusMonths(1);
                LoanRepayment mockRepayment = new LoanRepayment();
                mockRepayment.setRepaymentId(1L);
                mockRepayment.setStatus(LoanRepayment.RepaymentStatus.PAID);

                when(loanService.repay(eq(1L), eq(repaymentDate))).thenReturn(mockRepayment);

                mockMvc.perform(post("/api/loans/1/repay")
                                .param("repaymentDate", repaymentDate.toString())
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("PAID"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testRepayLoan_NotFound() throws Exception {
                LocalDate repaymentDate = LocalDate.now().plusMonths(1);
                when(loanService.repay(eq(999L), eq(repaymentDate)))
                                .thenThrow(new RepaymentNotFoundException("还款计划不存在"));

                mockMvc.perform(post("/api/loans/999/repay")
                                .param("repaymentDate", repaymentDate.toString())
                                .with(csrf()))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testGetRepaymentSchedule() throws Exception {
                LocalDate repaymentDate = LocalDate.now().plusMonths(1);
                LoanApplication loan = new LoanApplication();
                loan.setLoanId(1L);
                loan.setStatus(LoanApplication.LoanStatus.APPROVED);
                loan.setAmount(new BigDecimal(12000));
                loan.setTerm(12);
                loan.setStartDate(LocalDate.now().plusDays(15));
                loan.setEndDate(LocalDate.now().plusYears(1));

                LoanRepayment repayment1 = new LoanRepayment();
                repayment1.setRepaymentId(1L);
                repayment1.setStatus(LoanRepayment.RepaymentStatus.PENDING);
                repayment1.setAmount(new BigDecimal(1000));
                repayment1.setRepaymentDate(repaymentDate);
                repayment1.setLoan(loan);

                LoanRepayment repayment2 = new LoanRepayment();
                repayment2.setRepaymentId(2L);
                repayment2.setStatus(LoanRepayment.RepaymentStatus.PENDING);
                repayment2.setAmount(new BigDecimal(1000));
                repayment2.setRepaymentDate(repaymentDate.plusMonths(1));
                repayment2.setLoan(loan);
                when(loanService.getRepaymentSchedule(eq(1L)))
                                .thenReturn(List.of(repayment1, repayment2));

                mockMvc.perform(get("/api/loans/1/schedule").with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].amount").value(1000))
                                .andExpect(jsonPath("$[0].status").value("PENDING"));
                ;
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testGetAllLoanStatus() throws Exception {
                LoanApplication loan1 = new LoanApplication();
                loan1.setLoanId(1L);
                loan1.setStatus(LoanStatus.APPROVED);

                LoanApplication loan2 = new LoanApplication();
                loan2.setLoanId(2L);
                loan2.setStatus(LoanStatus.PENDING);

                User user = new User();
                user.setUserId(1);
                loan1.setUser(user);
                loan2.setUser(user);

                when(loanService.getAllLoans()).thenReturn(List.of(loan1, loan2));

                mockMvc.perform(get("/api/loans/status").with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].status").value("APPROVED"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testGetPendingLoans() throws Exception {
                LoanApplication loan1 = new LoanApplication();
                loan1.setLoanId(1L);
                loan1.setStatus(LoanStatus.PENDING);

                when(loanService.getPendingLoans()).thenReturn(List.of(loan1));

                mockMvc.perform(get("/api/loans/pending").with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].status").value("PENDING"));
        }

        @Test
        @WithMockUser(roles = "USER")
        void testGetPendingLoans_AccessDenied() throws Exception {
                mockMvc.perform(get("/api/loans/pending").with(csrf()))
                                .andExpect(status().isForbidden());
        }
}