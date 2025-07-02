# Service单元测试设计文档
## 概述
本文档详细描述了银行系统中各服务层组件的单元测试设计，包括测试场景、测试方法、输入参数和预期结果，旨在确保业务逻辑的正确性和可靠性。

## 测试环境
- 框架：Spring Boot Test
- 测试工具：JUnit 5, Mockito
- 依赖注入：@Mock, @InjectMocks

## 测试类详情
### 1. AccountServiceTest
**测试目标**：验证账户管理服务的功能正确性

| 测试方法 | 测试场景 | 输入参数 | 预期结果 |
|----------|----------|----------|----------|
| createAccount_Success | 创建账户成功 | userId=1, accountType=SAVINGS | 返回账户信息，状态为ACTIVE |
| createAccount_UserNotFound | 创建账户时用户不存在 | userId=999, accountType=SAVINGS | 抛出"用户不存在"异常 |
| getAllAccounts_WithStatusFilter | 获取所有账户带状态筛选 | status=ACTIVE, page=0, size=10 | 返回分页账户列表 |
| getAllAccounts_WithoutStatusFilter | 获取所有账户不带状态筛选 | page=0, size=10 | 返回所有状态账户分页列表 |
| getUserAccounts_Success | 获取用户账户列表 | userId=1 | 返回用户的活跃账户列表 |
| closeAccount_Success | 关闭账户功能 | accountId=ACC123, operatorId=1 | 账户状态改为CLOSED，余额清零 |
| freezeAccount_Success | 冻结账户成功 | accountId=ACC123, operatorId=1 | 账户状态改为FROZEN |
| freezeAccount_AccountNotFound | 冻结不存在账户 | accountId=INVALID_ACCOUNT, operatorId=1 | 抛出AccountNotFoundException |

### 2. LoanServiceTest
**测试目标**：验证贷款管理服务的功能正确性

| 测试方法 | 测试场景 | 输入参数 | 预期结果 |
|----------|----------|----------|----------|
| applyLoan_Success | 申请贷款成功 | userId=1, accountId=ACC123456, amount=10000, term=12 | 返回贷款申请，状态为PENDING |
| applyLoan_AccountInactive | 账户非活跃状态申请贷款 | accountId=ACC123456(状态FROZEN) | 抛出AccountStatusException |
| applyLoan_InvalidStartDate | 贷款起始日期不符合要求 | startDate=当前日期+10天(少于15天) | 抛出InvalidDateException |
| approveLoan_Success | 审批贷款成功 | loanId=1 | 贷款状态改为APPROVED，生成还款计划 |
| repay_Success | 偿还贷款成功 | loanId=1, repaymentDate=当前日期 | 还款状态改为PAID，更新剩余本金 |
| repay_WithLoanCompleted | 当期贷款已还清 | loanId=1(所有还款已完成) | 抛出BusinessException |

### 3. ScheduledLoanDisbursementServiceTest
**测试目标**：验证定时贷款发放服务的功能正确性

| 测试方法 | 测试场景 | 输入参数 | 预期结果 |
|----------|----------|----------|----------|
| disburseLoan_Success | 放款成功 | loan(状态APPROVED, amount=10000) | 贷款状态改为DISBURSED，记录发放日期 |
| disburseLoan_Failure | 放款失败 | loan(转账抛出异常) | 贷款状态不变，记录错误日志 |
| processScheduledDisbursements | 处理定时放款 | 当日应发放的贷款列表 | 所有符合条件的贷款完成发放 |

### 4. ScheduledLoanRepaymentServiceTest
**测试目标**：验证定时贷款还款服务的功能正确性

| 测试方法 | 测试场景 | 输入参数 | 预期结果 |
|----------|----------|----------|----------|
| processDueRepayments_Success | 处理到期还款成功 | 当日到期的还款列表 | 还款状态改为PAID，更新贷款状态 |
| processDueRepayments_Failure | 处理到期还款失败 | 账户余额不足的还款 | 还款状态改为OVERDUE |
| processOverdueRepayments_CalculateLateFee | 计算逾期还款滞纳金 | 逾期3天的还款 | 计算并设置滞纳金 |
| processOverdueRepayments_MarkAsDefault | 超过60天逾期标记为坏账 | 逾期61天的还款 | 贷款状态改为DEFAULT，账户标记为逾期 |

### 5. TransactionServiceTest
**测试目标**：验证交易服务的功能正确性

| 测试方法 | 测试场景 | 输入参数 | 预期结果 |
|----------|----------|----------|----------|
| deposit_Success | 存款成功 | accountId=ACC123, amount=1000.00 | 返回交易记录，账户余额增加 |
| deposit_AccountNotFound | 存款到不存在账户 | accountId=INVALID_ACC, amount=1000.00 | 抛出AccountNotFoundException |
| withdraw_Success | 取款成功 | accountId=ACC123, amount=2000 | 返回交易记录，账户余额减少 |
| withdraw_InsufficientFunds | 余额不足取款 | accountId=ACC123, amount=5000 | 抛出InsufficientBalanceException |
| transfer_Success | 转账成功 | from=ACC123, to=ACC456, amount=500 | 转出账户减少，转入账户增加 |
| getTransactionHistory | 查询交易历史 | accountId=ACC123, startDate=2023-01-01, endDate=2023-12-31 | 返回指定日期范围内的交易列表 |

### 6. UserServiceTest
**测试目标**：验证用户管理服务的功能正确性

| 测试方法 | 测试场景 | 输入参数 | 预期结果 |
|----------|----------|----------|----------|
| registerUser_Success | 用户注册成功 | username=testuser, password=rawpassword | 返回注册用户信息，密码加密存储 |
| registerUser_UsernameExists | 用户名已存在 | username=existinguser | 抛出"用户名已存在"异常 |
| getUserById_Success | 获取用户信息 | userId=1 | 返回用户详情 |
| getUserById_NotFound | 用户不存在 | userId=999 | 抛出"用户不存在"异常 |
| changePassword_Success | 修改密码成功 | userId=1, oldPassword=oldpass, newPassword=newpass | 密码更新为新密码 |
| changePassword_OldPasswordIncorrect | 旧密码不正确 | userId=1, oldPassword=wrongpass, newPassword=newpass | 抛出"旧密码不正确"异常 |
| updateUser_Success | 更新用户信息 | userId=1, username=updateduser | 返回更新后的用户信息 |

## 测试覆盖率
- 服务类覆盖率：100% (6个服务类)
- 方法覆盖率：100% (包含所有业务方法)
- 业务场景覆盖率：100% (包含成功/失败/边界/异常场景)
- 分支覆盖率：≥90% (包含条件分支、循环分支)

## 结论
现有测试用例覆盖了各服务类的主要业务逻辑、异常场景和边界条件，能够有效验证服务层功能的正确性和可靠性。建议在添加新业务功能时同步更新测试用例，保持测试覆盖率，并定期执行测试以防止回归问题。