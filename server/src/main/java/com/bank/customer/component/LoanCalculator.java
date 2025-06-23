package com.bank.customer.component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.bank.customer.entity.LoanApplication;
import com.bank.customer.entity.LoanRepayment;

@Component
public class LoanCalculator {
    /**
     * 计算等额本息每月还款额
     * 
     * @param principal  贷款本金
     * @param annualRate 年利率（如 0.05 表示 5%）
     * @param term       贷款期限（月）
     */
    public BigDecimal calculateMonthlyPayment(
            BigDecimal principal,
            BigDecimal annualRate,
            int term) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal factor = monthlyRate.add(BigDecimal.ONE).pow(term);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(factor);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
    }

    /**
     * 生成还款计划
     * 
     * @param loan 贷款信息
     * @return 还款计划列表
     */
    public List<LoanRepayment> generateRepaymentSchedule(LoanApplication loan) {
        List<LoanRepayment> schedule = new ArrayList<>();
        BigDecimal monthlyPayment = loan.getMonthlyPayment();
        BigDecimal remainingPrincipal = loan.getAmount();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        LocalDate repaymentDate = loan.getStartDate().plusMonths(1);

        for (int i = 1; i <= loan.getTerm(); i++) {
            BigDecimal interest = remainingPrincipal.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal principal = monthlyPayment.subtract(interest);

            LoanRepayment repayment = new LoanRepayment();
            repayment.setLoan(loan);
            repayment.setRepaymentDate(repaymentDate);
            if (i == loan.getTerm()) {
                // 最后一期利息 = 剩余本金 × 月利率
                interest = remainingPrincipal.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                // 最后一期本金 = 剩余本金
                principal = remainingPrincipal;
                // 最后一期还款额 = 本金 + 利息（动态调整，不再固定为月供）
                monthlyPayment = principal.add(interest);
            }

            // 同步更新还款记录的金额字段
            repayment.setAmount(monthlyPayment);
            repayment.setPrincipal(principal);
            repayment.setInterest(interest);
            repayment.setStatus(LoanRepayment.RepaymentStatus.PENDING);

            schedule.add(repayment);
            remainingPrincipal = remainingPrincipal.subtract(principal);
            repaymentDate = repaymentDate.plusMonths(1);
        }

        return schedule;
    }
}