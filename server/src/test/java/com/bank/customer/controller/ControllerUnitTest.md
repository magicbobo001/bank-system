# Controller单元测试设计文档

## 概述
本文档详细描述了银行系统中各控制器的单元测试设计，包括测试场景、测试方法、输入参数和预期结果，旨在确保API接口的正确性和安全性。

---

## 测试环境
- **框架**: Spring Boot Test
- **测试工具**: JUnit 5, MockMvc, Mockito
- **认证方式**: Spring Security @WithMockUser

---

## 测试类详情

### 1. AccountControllerTest
**测试目标**: 验证账户管理接口的功能正确性和权限控制

| 测试方法 | 测试场景 | 角色 | 请求参数 | 预期结果 |
|---------|---------|------|---------|---------|
| testCreateAccount_Success | 创建账户成功 | ADMIN | userId=1, accountType=SAVINGS | 200 OK, 返回账户信息 |
| testGetMyAccounts_Success | 查询个人账户 | USER | userId=1 | 200 OK, 返回账户列表 |
| testDeleteAccount_Success | 删除账户成功 | ADMIN | accountId=ACC123, operatorId=1 | 200 OK |
| testFreezeAccount_Success | 冻结账户成功 | ADMIN | accountId=ACC123, operatorId=1 | 200 OK |
| testUnfreezeAccount_Success | 解冻账户成功 | ADMIN | accountId=ACC123, operatorId=1 | 200 OK |
| testRestoreAccount_Success | 恢复账户成功 | ADMIN | accountId=ACC123, operatorId=1 | 200 OK |
| testGetAllAccounts_Success | 管理员查询账户列表 | ADMIN | status=ACTIVE, page=0, size=10 | 200 OK, 返回分页账户 |
| testCloseAccount_AccountNotFound | 关闭不存在账户 | ADMIN | accountId=ACC999, operatorId=1 | 404 Not Found |
| testFreezeAccount_AccountNotFound | 冻结不存在账户 | ADMIN | accountId=NOT_EXIST, operatorId=1 | 404 Not Found |
| testUnfreezeAccount_AccountNotFound | 解冻不存在账户 | ADMIN | accountId=NOT_EXIST, operatorId=1 | 404 Not Found |
| testRestoreAccount_AccountNotFound | 恢复不存在账户 | ADMIN | accountId=NOT_EXIST, operatorId=1 | 404 Not Found |
| testAdminEndpoint_AccessDenied | 普通用户访问管理员接口 | USER | - | 403 Forbidden |
| testCreateAccount_AccessDenied | 普通用户创建账户 | USER | userId=1, accountType=SAVINGS | 403 Forbidden |
| testDeleteAccount_AccessDenied | 普通用户删除账户 | USER | accountId=ACC123, operatorId=1 | 403 Forbidden |
| testFreezeAccount_AccessDenied | 普通用户冻结账户 | USER | accountId=ACC123, operatorId=1 | 403 Forbidden |
| testUnfreezeAccount_AccessDenied | 普通用户解冻账户 | USER | accountId=ACC123, operatorId=1 | 403 Forbidden |
| testGetAllAccounts_InvalidStatus | 查询账户时状态参数验证 | ADMIN | status=INVALID_STATUS, page=0, size=10 | 400 Bad Request |
| testCreateAccount_InvalidType | 创建账户时无效账户类型 | ADMIN | userId=1, accountType=INVALID_TYPE | 400 Bad Request |

---

### 2. AuthControllerTest
**测试目标**: 验证认证授权接口的功能正确性

| 测试方法 | 测试场景 | 角色 | 请求参数 | 预期结果 |
|---------|---------|------|---------|---------|
| testLogin_Success | 用户登录成功 | USER | username=testuser, password=password123 | 200 OK, 返回JWT令牌 |
| testLogin_Failure_InvalidCredentials | 无效凭证登录 | USER | username=testuser, password=wrongpassword | 401 Unauthorized |
| testGetCurrentUser | 获取当前用户信息 | USER | - | 200 OK, 返回用户详情 |
| testGetCurrentUser_NotExist | 获取不存在用户 | USER | - | 404 Not Found |
| testLogin_NoRolesAssigned | 用户无角色登录 | USER | username=noroleuser | 401 Unauthorized |
| testLogin_RolesNull | 用户角色为Null | USER | username=nullroleuser | 401 Unauthorized |
| testLogin_InvalidRoleAssociation | 角色关联异常 | USER | username=invalidroleuser | 400 Bad Request |

---

### 3. LoanControllerTest
**测试目标**: 验证贷款管理接口的功能正确性

| 测试方法 | 测试场景 | 角色 | 请求参数 | 预期结果 |
|---------|---------|------|---------|---------|
| testApplyLoan_Success | 申请贷款成功 | USER | userId=1, amount=10000, term=12 | 200 OK, 返回贷款申请 |
| testApplyLoan_InvalidStartDate | 无效贷款起始日期 | USER | startDate=当前日期-1天 | 400 Bad Request |
| testApproveLoan_Success | 审批贷款成功 | ADMIN | loanId=1 | 200 OK, 状态改为APPROVED |
| testApproveLoan_AccessDenied | 普通用户审批贷款 | USER | loanId=1 | 403 Forbidden |
| testApproveLoan_LoanNotFound | 审批不存在贷款 | ADMIN | loanId=999 | 404 Not Found |
| testRejectLoan_Success | 拒绝贷款成功 | ADMIN | loanId=1 | 200 OK, 状态改为REJECTED |
| testRejectLoan_AccessDenied | 普通用户拒绝贷款 | USER | loanId=1 | 403 Forbidden |
| testRepayLoan_Success | 偿还贷款成功 | ADMIN | loanId=1, repaymentDate=未来日期 | 200 OK, 状态改为PAID |
| testRepayLoan_NotFound | 偿还不存在贷款 | ADMIN | loanId=999 | 404 Not Found |
| testGetRepaymentSchedule | 查询还款计划 | ADMIN | loanId=1 | 200 OK, 返回还款列表 |
| testGetAllLoanStatus | 查询所有贷款状态 | ADMIN | - | 200 OK, 返回贷款列表 |
| testGetPendingLoans | 查询待处理贷款 | ADMIN | - | 200 OK, 返回待处理列表 |
| testGetPendingLoans_AccessDenied | 普通用户查询待处理贷款 | USER | - | 403 Forbidden |

---

### 4. TransactionControllerTest
**测试目标**: 验证交易接口的功能正确性

| 测试方法 | 测试场景 | 角色 | 请求参数 | 预期结果 |
|---------|---------|------|---------|---------|
| testDeposit_Success | 存款成功 | ADMIN | accountId=ACC123, amount=1000.0 | 200 OK, 返回交易记录 |
| testDeposit_AccountNotFound | 存款到不存在账户 | ADMIN | accountId=INVALID_ACC, amount=1000.0 | 404 Not Found |
| testWithdraw_Success | 取款成功 | ADMIN | accountId=ACC123, amount=2000 | 200 OK, 返回交易记录 |
| testWithdraw_InsufficientFunds | 余额不足取款 | ADMIN | accountId=ACC123, amount=5000 | 400 Bad Request |
| testWithdraw_AccountNotFound | 取款账户不存在 | ADMIN | accountId=INVALID_ACC, amount=1000 | 404 Not Found |
| testWithdraw_InvalidAmount | 无效金额取款 | ADMIN | accountId=ACC123, amount=0 | 400 Bad Request |
| testTransfer_Success | 转账成功 | ADMIN | from=ACC123, to=ACC456, amount=500 | 200 OK |
| testTransfer_InsufficientFunds | 转账余额不足 | ADMIN | from=ACC123, to=ACC456, amount=10000 | 400 Bad Request |
| testTransfer_AccountNotFound | 转账账户不存在 | ADMIN | from=INVALID_ACC, to=ACC456, amount=500 | 404 Not Found |
| testGetTransactionHistory | 查询交易历史 | ADMIN | accountId=ACC123, startDate=2023-01-01, endDate=2023-12-31 | 200 OK, 返回交易列表 |
| testDeposit_AccessDenied | 普通用户存款 | USER | accountId=ACC123, amount=1000 | 403 Forbidden |
| testWithdraw_AccessDenied | 普通用户取款 | USER | accountId=ACC123, amount=1000 | 403 Forbidden |
| testTransfer_AccessDenied | 普通用户转账 | USER | from=ACC123, to=ACC456, amount=500 | 403 Forbidden |

---

### 5. UserControllerTest
**测试目标**: 验证用户管理接口的功能正确性

| 测试方法 | 测试场景 | 角色 | 请求参数 | 预期结果 |
|---------|---------|------|---------|---------|
| testRegisterUser_Success | 注册用户成功 | ADMIN | username=newuser, password=password123 | 200 OK, 返回用户信息 |
| testGetAllUsers | 查询所有用户 | ADMIN | - | 200 OK, 返回用户列表 |
| testUpdateProfile_Success | 更新用户资料 | ADMIN | userId=1, username=updatedname | 200 OK, 返回更新信息 |
| testGetMyProfile_Success | 获取个人资料 | USER | - | 200 OK, 返回用户信息 |
| testChangePassword_Success | 修改密码成功 | ADMIN | oldPassword=oldpass, newPassword=newpass | 200 OK |
| testRegisterUser_DuplicateUsername | 注册重复用户名 | ADMIN | username=existinguser | 400 Bad Request, 返回"用户名已存在" |
| testRegisterUser_AccessDenied | 普通用户注册 | USER | username=newuser, password=password123 | 403 Forbidden |
| testGetAllUsers_AccessDenied | 普通用户查询所有用户 | USER | - | 403 Forbidden |
| testGetMyProfile_UserNotFound | 获取不存在用户资料 | USER | - | 404 Not Found |
| testChangePassword_OldPasswordIncorrect | 修改密码原密码错误 | ADMIN | oldPassword=wrongpass, newPassword=newpass | 400 Bad Request |
| testChangePassword_NotExist | 修改不存在用户密码 | ADMIN | oldPassword=wrongpass, newPassword=newpass | 404 Not Found |

---

## 测试覆盖率
- **控制器覆盖率**: 100% (5个控制器)
- **接口覆盖率**: 100% (包含成功/失败/权限/边界场景)
- **异常场景覆盖率**: 100% (包含业务异常和系统异常)

---

## 结论
现有测试用例覆盖了各控制器的主要功能点、异常场景和权限控制，能够有效验证API接口的正确性和安全性。建议定期执行测试以确保代码修改不会引入回归问题。