package com.bank.customer.component;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanRepayment;

@ExtendWith(MockitoExtension.class)
class LoanCalculatorTest {
    private final LoanCalculator loanCalculator = new LoanCalculator();

    // 测试正常等额本息计算
    @Test
    void calculateMonthlyPayment_StandardCase() {
        // 本金10000元，年利率5%，期限12个月
        BigDecimal principal = new BigDecimal("10000");
        BigDecimal annualRate = new BigDecimal("5");
        int term = 12;

        BigDecimal result = loanCalculator.calculateMonthlyPayment(principal, annualRate, term);
        BigDecimal expected = new BigDecimal("856.07"); // 精确到分

        assertEquals(0, expected.compareTo(result.setScale(2, RoundingMode.HALF_UP)));
    }

    // 测试短期贷款（1个月）
    @Test
    void calculateMonthlyPayment_OneMonthTerm() {
        BigDecimal principal = new BigDecimal("10000");
        BigDecimal annualRate = new BigDecimal("5");
        int term = 1;

        BigDecimal result = loanCalculator.calculateMonthlyPayment(principal, annualRate, term);
        BigDecimal expected = new BigDecimal("10041.67"); // 本金+首月利息

        assertEquals(0, expected.compareTo(result.setScale(2, RoundingMode.HALF_UP)));
    }

    // 测试零利率场景
    @Test
    void calculateMonthlyPayment_ZeroInterestRate() {
        BigDecimal principal = new BigDecimal("12000");
        BigDecimal annualRate = new BigDecimal("0");
        int term = 12;

        BigDecimal result = loanCalculator.calculateMonthlyPayment(principal, annualRate, term);
        BigDecimal expected = new BigDecimal("1000.00"); // 本金均分

        assertEquals(0, expected.compareTo(result.setScale(2, RoundingMode.HALF_UP)));
    }

    // 测试生成完整还款计划
    @Test
    void generateRepaymentSchedule_StandardCase() {
        // 准备测试数据
        LoanApplication loan = createTestLoanApplication();
        List<LoanRepayment> schedule = loanCalculator.generateRepaymentSchedule(loan);

        // 验证基本属性
        assertEquals(12, schedule.size());
        assertEquals(LocalDate.of(2023, 2, 1), schedule.get(0).getRepaymentDate());
        assertEquals(LoanRepayment.RepaymentStatus.PENDING, schedule.get(0).getStatus());

        // 验证最后一期还款调整
        LoanRepayment lastRepayment = schedule.get(11);
        BigDecimal expectedLastPayment = new BigDecimal("856.07"); // 最后一期可能有微调
        assertEquals(0, expectedLastPayment.compareTo(lastRepayment.getAmount().setScale(2, RoundingMode.HALF_UP)));
    }

    // 测试还款计划中的本金利息分摊
    @Test
    void generateRepaymentSchedule_PrincipalInterestAllocation() {
        LoanApplication loan = createTestLoanApplication();
        List<LoanRepayment> schedule = loanCalculator.generateRepaymentSchedule(loan);

        // 验证首期利息计算
        BigDecimal firstInterest = schedule.get(0).getInterest();
        BigDecimal expectedFirstInterest = new BigDecimal("41.67"); // 10000 * 5% / 12
        assertEquals(0, expectedFirstInterest.compareTo(firstInterest));

        // 验证末期本金等于剩余本金
        LoanRepayment lastRepayment = schedule.get(11);
        assertEquals(lastRepayment.getPrincipal(), lastRepayment.getAmount().subtract(lastRepayment.getInterest()));
    }

    // 测试边界值：最小金额贷款
    @Test
    void calculateMonthlyPayment_MinimumAmount() {
        BigDecimal principal = new BigDecimal("1");
        BigDecimal annualRate = new BigDecimal("5");
        int term = 1;

        BigDecimal result = loanCalculator.calculateMonthlyPayment(principal, annualRate, term);
        assertNotNull(result);
    }

    // 辅助方法：创建测试用贷款申请
    private LoanApplication createTestLoanApplication() {
        LoanApplication loan = new LoanApplication();
        loan.setAmount(new BigDecimal("10000"));
        loan.setInterestRate(new BigDecimal("5"));
        loan.setTerm(12);
        loan.setStartDate(LocalDate.of(2023, 1, 1));
        loan.setMonthlyPayment(loanCalculator.calculateMonthlyPayment(
                loan.getAmount(), loan.getInterestRate(), loan.getTerm()));
        return loan;
    }
}