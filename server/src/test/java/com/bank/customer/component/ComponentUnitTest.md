# Component单元测试设计文档
## 概述
本文档详细描述了银行系统中组件层`LoanCalculator`的单元测试设计，包括测试场景、测试方法、输入参数和预期结果，旨在验证贷款计算核心逻辑的准确性和可靠性。

## 测试环境
- 框架：Spring Boot Test
- 测试工具：JUnit 5, Mockito
- 依赖注入：@Mock, @InjectMocks
- 精度处理：BigDecimal (RoundingMode.HALF_UP)

## 测试类详情
### 1. LoanCalculatorTest
**测试目标**：验证贷款计算器组件的月供计算和还款计划生成功能正确性

| 测试方法 | 测试场景 | 输入参数 | 预期结果 |
|----------|----------|----------|----------|
| calculateMonthlyPayment_StandardCase | 标准等额本息计算 | 本金=10000, 年利率=5%, 期限=12个月 | 月供=856.07元 |
| calculateMonthlyPayment_OneMonthTerm | 短期贷款(1个月) | 本金=10000, 年利率=5%, 期限=1个月 | 月供=10041.67元(本金+首月利息) |
| calculateMonthlyPayment_ZeroInterestRate | 零利率场景 | 本金=12000, 年利率=0%, 期限=12个月 | 月供=1000.00元(本金均分) |
| generateRepaymentSchedule_StandardCase | 生成完整还款计划 | 贷款(10000元,5%,12期,2023-01-01起) | 生成12期还款计划，首期待还款日为2023-02-01 |
| generateRepaymentSchedule_PrincipalInterestAllocation | 本金利息分摊验证 | 同标准还款计划 | 首期利息=41.67元，末期本金=剩余本金 |
| calculateMonthlyPayment_MinimumAmount | 最小金额贷款边界测试 | 本金=1元, 年利率=5%, 期限=1个月 | 计算结果非空 |

## 测试覆盖率
- 组件类覆盖率：100% (1个组件类)
- 方法覆盖率：100% (2个核心方法：calculateMonthlyPayment, generateRepaymentSchedule)
- 业务场景覆盖率：100% (包含标准/短期/零利率/边界场景)
- 分支覆盖率：100% (包含利率为零的特殊分支处理)

## 结论
现有测试用例全面覆盖了贷款计算器的核心功能和边界条件，验证了等额本息计算、还款计划生成及本金利息分摊的准确性。建议在修改利率计算逻辑或还款计划生成规则时，同步更新对应测试用例以维持测试有效性。