import { useEffect, useState } from "react";
import api from "../../api/api";
import { transactionApi } from "../../api/api";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import { zhCN } from "date-fns/locale";
import { useAppSelector } from "../../app/store";
import {
  Button,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
  Box,
  TextField,
  Modal,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from "@mui/material";
import Navbar from "../../components/Navbar";

interface Account {
  accountId: string;
  accountType: string;
  balance: number;
  status: "ACTIVE" | "FROZEN" | "CLOSED";
  user: {
    // 用户信息嵌套在 user 对象中
    userId: number;
    // 可根据后端返回的实际字段补充其他用户属性（如 username）
  };
  createdAt: string;
}
interface Transaction {
  transactionId: number;
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  transactionTime: string;
  transactionType: "DEPOSIT" | "WITHDRAW" | "TRANSFER";
}
export default function AdminAccountManagement() {
  const { user } = useAppSelector((state) => state.auth);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [openCreateModal, setOpenCreateModal] = useState(false);
  const [newAccountType, setNewAccountType] = useState("SAVINGS"); // 默认储蓄账户
  const [targetUserId, setTargetUserId] = useState(""); // 开户目标用户ID
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
  const [openTransactionModal, setOpenTransactionModal] = useState(false);
  const [transactionType, setTransactionType] = useState<
    "DEPOSIT" | "WITHDRAW" | "TRANSFER"
  >("DEPOSIT");
  const [transactionAmount, setTransactionAmount] = useState("");
  const [targetAccountId, setTargetAccountId] = useState("");
  const [openHistoryModal, setOpenHistoryModal] = useState(false);
  const [accountTransactions, setAccountTransactions] = useState<Transaction[]>(
    []
  );
  const [startDate, setStartDate] = useState<Date | null>(
    new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
  );
  const [endDate, setEndDate] = useState<Date | null>(new Date());
  // 加载所有账户（调用AccountController的getAllAccounts接口）
  useEffect(() => {
    if (user?.userId) {
      setIsLoading(true);
      api
        .get("/accounts/admin", {
          params: { page: 0, size: 20 },
        })
        .then((res) => setAccounts(res.data.content))
        .catch((err) => alert("加载账户失败：" + err.message))
        .finally(() => setIsLoading(false));
    }
  }, [user?.userId]);
  interface ApiError {
    response?: {
      data?: {
        message?: string; // 与后端返回的错误信息字段一致
      };
    };
  }
  const handleViewTransactions = async (account: Account) => {
    setSelectedAccount(account);
    if (startDate && endDate) {
      try {
        const formattedStartDate = startDate.toISOString().split("T")[0];
        const formattedEndDate = endDate.toISOString().split("T")[0];
        const res = await transactionApi.getAccountTransactions(
          account.accountId,
          formattedStartDate,
          formattedEndDate
        );
        setAccountTransactions(res.data);
        setOpenHistoryModal(true);
      } catch (err) {
        alert("获取交易记录失败: " + (err as Error).message);
      }
    }
  };

  const handleTransactionSubmit = async () => {
    if (!selectedAccount || !transactionAmount) return;
    const amount = parseFloat(transactionAmount);
    if (isNaN(amount) || amount <= 0) {
      alert("请输入有效的交易金额");
      return;
    }
    // 添加用户存在性检查
    if (!user?.userId) {
      alert("当前用户未登录或会话已过期");
      return;
    }
    try {
      if (transactionType === "DEPOSIT") {
        // 移除 ! 非空断言
        await transactionApi.deposit(
          selectedAccount.accountId,
          amount,
          user.userId
        );
        alert("存款成功");
      } else if (transactionType === "WITHDRAW") {
        await transactionApi.withdraw(
          selectedAccount.accountId,
          amount,
          user.userId
        );
        alert("取款成功");
      } else if (transactionType === "TRANSFER") {
        if (!targetAccountId) {
          alert("请输入目标账户ID");
          return;
        }
        await transactionApi.transfer(
          selectedAccount.accountId,
          targetAccountId,
          amount,
          user.userId
        );
        alert("转账成功");
      }
      setOpenTransactionModal(false);
      // 刷新账户列表和余额
      api.get("/accounts/admin").then((res) => setAccounts(res.data.content));
    } catch (err) {
      const errorMessage =
        (err as ApiError)?.response?.data?.message || "交易失败";
      alert(`交易失败: ${errorMessage}`);
    }
  };
  // 开户操作（调用AccountController的createAccount接口）
  const handleCreateAccount = async () => {
    const parsedUserId = parseInt(targetUserId);
    if (isNaN(parsedUserId)) {
      alert("请输入有效的用户ID（数字）");
      return;
    }
    if (!newAccountType) {
      alert("请选择账户类型");
      return;
    }
    try {
      await api.post("/accounts/create", null, {
        params: {
          userId: parsedUserId,
          accountType: newAccountType,
        },
      });
      alert("开户成功");
      setOpenCreateModal(false);
      // 刷新账户列表
      api.get("/accounts/admin").then((res) => setAccounts(res.data.content));
    } catch (err) {
      const errorMessage =
        (err as ApiError)?.response?.data?.message || "开户失败";
      alert(`开户失败：${errorMessage}`);
    }
  };
  // 冻结账户
  const handleFreezeAccount = async (accountId: string) => {
    if (window.confirm("确认冻结该账户？")) {
      try {
        await api.put(`/accounts/${accountId}/freeze`, null, {
          params: { operatorId: user?.userId },
        });
        alert("冻结成功");
        // 刷新账户列表（重新获取数据）
        api.get("/accounts/admin").then((res) => setAccounts(res.data.content));
      } catch (err) {
        alert("冻结失败：" + (err as Error).message);
      }
    }
  };

  // 解冻账户
  const handleUnfreezeAccount = async (accountId: string) => {
    if (window.confirm("确认解冻该账户？")) {
      try {
        await api.put(`/accounts/${accountId}/unfreeze`, null, {
          params: { operatorId: user?.userId },
        });
        alert("解冻成功");
        api.get("/accounts/admin").then((res) => setAccounts(res.data.content));
      } catch (err) {
        alert("解冻失败：" + (err as Error).message);
      }
    }
  };

  // 恢复被删除账户（从CLOSED恢复为ACTIVE）
  const handleRestoreAccount = async (accountId: string) => {
    if (window.confirm("确认恢复该账户？")) {
      try {
        await api.put(`/accounts/${accountId}/restore`, null, {
          params: { operatorId: user?.userId },
        });
        alert("恢复成功");
        api.get("/accounts/admin").then((res) => setAccounts(res.data.content));
      } catch (err) {
        alert("恢复失败：" + (err as Error).message);
      }
    }
  };
  // 销户操作（调用AccountController的closeAccount接口）
  const handleCloseAccount = async (accountId: string) => {
    if (window.confirm("确认销户？")) {
      try {
        await api.delete(`/accounts/${accountId}`, {
          params: { operatorId: user?.userId },
        });
        alert("销户成功");
        api.get("/accounts/admin").then((res) => setAccounts(res.data.content));
      } catch (err) {
        alert("销户失败：" + (err as Error).message);
      }
    }
  };
  // 账户类型转换函数
  const getAccountTypeText = (type: string) => {
    switch (type) {
      case "savings":
        return "储蓄账户";
      case "checking":
        return "支票账户";
      default:
        return type;
    }
  };
  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        minHeight: "100vh",
        maxWidth: "100%",
        margin: "0 auto",
        padding: "20px",
      }}
    >
      <Navbar />
      <Card sx={{ margin: 3, width: "100%" }}>
        <CardContent>
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <Typography variant="h5" gutterBottom>
              账户管理
            </Typography>
            <Button
              variant="contained"
              onClick={() => setOpenCreateModal(true)}
            >
              新建账户
            </Button>
          </div>
          {isLoading ? (
            <Typography>加载中...</Typography>
          ) : (
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>账户ID</TableCell>
                  <TableCell>关联用户ID</TableCell>
                  <TableCell>账户类型</TableCell>
                  <TableCell>余额</TableCell>
                  <TableCell>状态</TableCell>
                  <TableCell>操作</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {accounts.map((acc) => (
                  <TableRow key={acc.accountId}>
                    <TableCell>{acc.accountId}</TableCell>
                    <TableCell>{acc.user?.userId}</TableCell>
                    {/* 从 user 对象中获取 userId */}
                    <TableCell>{getAccountTypeText(acc.accountType)}</TableCell>
                    <TableCell>{acc.balance.toFixed(2)}</TableCell>
                    <TableCell>{acc.status}</TableCell>
                    <TableCell>
                      <Box sx={{ display: "flex", gap: 1 }}>
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() => handleViewTransactions(acc)}
                          disabled={acc.status !== "ACTIVE"}
                        >
                          交易记录
                        </Button>
                        <Button
                          size="small"
                          variant="contained"
                          onClick={() => {
                            setSelectedAccount(acc);
                            setTransactionType("DEPOSIT");
                            setTransactionAmount("");
                            setTargetAccountId("");
                            setOpenTransactionModal(true);
                          }}
                          disabled={
                            acc.status !== "ACTIVE" ||
                            acc.accountId === "LOAN_BANK_ACCOUNT"
                          }
                        >
                          存款
                        </Button>
                        <Button
                          size="small"
                          variant="contained"
                          color="secondary"
                          onClick={() => {
                            setSelectedAccount(acc);
                            setTransactionType("WITHDRAW");
                            setTransactionAmount("");
                            setTargetAccountId("");
                            setOpenTransactionModal(true);
                          }}
                          disabled={
                            acc.status !== "ACTIVE" ||
                            acc.accountId === "LOAN_BANK_ACCOUNT"
                          }
                        >
                          取款
                        </Button>
                        <Button
                          size="small"
                          variant="contained"
                          color="primary"
                          onClick={() => {
                            setSelectedAccount(acc);
                            setTransactionType("TRANSFER");
                            setTransactionAmount("");
                            setTargetAccountId("");
                            setOpenTransactionModal(true);
                          }}
                          disabled={
                            acc.status !== "ACTIVE" ||
                            acc.accountId === "LOAN_BANK_ACCOUNT"
                          }
                        >
                          转账
                        </Button>
                        {acc.status === "ACTIVE" &&
                          acc.accountId !== "LOAN_BANK_ACCOUNT" && (
                            <Button
                              size="small"
                              variant="outlined"
                              color="error"
                              onClick={() => handleFreezeAccount(acc.accountId)}
                            >
                              冻结
                            </Button>
                          )}
                        {acc.status === "FROZEN" &&
                          acc.accountId !== "LOAN_BANK_ACCOUNT" && (
                            <Button
                              size="small"
                              variant="outlined"
                              color="success"
                              onClick={() =>
                                handleUnfreezeAccount(acc.accountId)
                              }
                            >
                              解冻
                            </Button>
                          )}
                        {acc.status !== "CLOSED" &&
                          acc.accountId !== "LOAN_BANK_ACCOUNT" && (
                            <Button
                              size="small"
                              variant="outlined"
                              color="error"
                              onClick={() => handleCloseAccount(acc.accountId)}
                            >
                              销户
                            </Button>
                          )}
                        {acc.status === "CLOSED" &&
                          acc.accountId !== "LOAN_BANK_ACCOUNT" && (
                            <Button
                              size="small"
                              variant="outlined"
                              color="success"
                              onClick={() =>
                                handleRestoreAccount(acc.accountId)
                              }
                            >
                              恢复
                            </Button>
                          )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
      {/* 交易操作模态框 */}
      <Modal
        open={openTransactionModal}
        onClose={() => setOpenTransactionModal(false)}
      >
        <Box
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            width: 400,
            bgcolor: "background.paper",
            border: "2px solid #000",
            boxShadow: 24,
            p: 4,
          }}
        >
          <Typography variant="h6" gutterBottom>
            {transactionType === "DEPOSIT"
              ? "存款"
              : transactionType === "WITHDRAW"
              ? "取款"
              : "转账"}
          </Typography>
          <Typography>账户ID: {selectedAccount?.accountId}</Typography>
          <Typography>
            当前余额: {selectedAccount?.balance.toFixed(2)} 元
          </Typography>
          <TextField
            fullWidth
            margin="normal"
            label="交易金额"
            type="number"
            value={transactionAmount}
            onChange={(e) => setTransactionAmount(e.target.value)}
            InputProps={{ inputProps: { min: 0.01, step: 0.01 } }}
          />
          {transactionType === "TRANSFER" && (
            <TextField
              fullWidth
              margin="normal"
              label="目标账户ID"
              value={targetAccountId}
              onChange={(e) => setTargetAccountId(e.target.value)}
            />
          )}
          <Box
            sx={{ display: "flex", justifyContent: "flex-end", gap: 2, mt: 3 }}
          >
            <Button onClick={() => setOpenTransactionModal(false)}>取消</Button>
            <Button variant="contained" onClick={handleTransactionSubmit}>
              确认
              {transactionType === "DEPOSIT"
                ? "存款"
                : transactionType === "WITHDRAW"
                ? "取款"
                : "转账"}
            </Button>
          </Box>
        </Box>
      </Modal>

      {/* 交易历史模态框 */}
      <Modal open={openHistoryModal} onClose={() => setOpenHistoryModal(false)}>
        <Box
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            width: "80%",
            maxHeight: "80vh",
            bgcolor: "background.paper",
            border: "2px solid #000",
            boxShadow: 24,
            p: 4,
            overflow: "auto",
          }}
        >
          <Typography variant="h6" gutterBottom>
            账户 {selectedAccount?.accountId} 交易记录
          </Typography>
          <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
            <LocalizationProvider
              dateAdapter={AdapterDateFns}
              adapterLocale={zhCN}
            >
              <DateTimePicker
                label="开始日期"
                value={startDate}
                onChange={(date) => setStartDate(date)}
                slotProps={{
                  textField: {
                    fullWidth: true,
                    margin: "normal",
                  },
                }}
              />
              <DateTimePicker
                label="结束日期"
                value={endDate}
                onChange={(date) => setEndDate(date)}
                slotProps={{
                  textField: {
                    fullWidth: true,
                    margin: "normal",
                  },
                }}
              />
              <Button
                variant="contained"
                onClick={() => handleViewTransactions(selectedAccount!)}
                sx={{ mt: 1.5 }}
              >
                查询
              </Button>
            </LocalizationProvider>
          </Box>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>交易时间</TableCell>
                <TableCell>类型</TableCell>
                <TableCell>金额（元）</TableCell>
                <TableCell>对方账户</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {accountTransactions.map((t) => (
                <TableRow key={t.transactionId}>
                  <TableCell>
                    {new Date(t.transactionTime).toLocaleString()}
                  </TableCell>
                  <TableCell>{t.transactionType}</TableCell>
                  <TableCell>
                    {(t.transactionType === "TRANSFER" &&
                      t.fromAccountId === selectedAccount?.accountId) ||
                    t.transactionType === "WITHDRAW"
                      ? `-${t.amount.toFixed(2)}`
                      : t.amount.toFixed(2)}
                  </TableCell>
                  <TableCell>
                    {t.transactionType === "TRANSFER"
                      ? t.fromAccountId === selectedAccount?.accountId
                        ? t.toAccountId
                        : t.fromAccountId
                      : "-"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Box>
      </Modal>
      {/* 开户模态框 */}
      <Modal open={openCreateModal} onClose={() => setOpenCreateModal(false)}>
        <Box
          sx={{
            position: "absolute",
            top: "30%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            bgcolor: "background.paper",
            p: 4,
            width: "400px",
          }}
        >
          <Typography variant="h6">新建账户</Typography>
          <FormControl fullWidth margin="normal" required>
            <TextField
              type="number"
              value={targetUserId}
              onChange={(e) => setTargetUserId(e.target.value)}
              label="目标用户ID"
              required
            />
          </FormControl>
          <FormControl fullWidth margin="normal" required>
            <InputLabel id="account-type-label">账户类型</InputLabel>
            <Select
              labelId="account-type-label" // 关联 InputLabel 的 id
              id="account-type-select"
              value={newAccountType}
              label="账户类型*" // 同步标签文字
              onChange={(e) => setNewAccountType(e.target.value as string)}
            >
              <MenuItem value="savings">储蓄账户</MenuItem>
              <MenuItem value="checking">支票账户</MenuItem>
            </Select>
          </FormControl>
          <Button
            fullWidth
            variant="contained"
            onClick={handleCreateAccount}
            sx={{ mt: 2 }}
          >
            提交开户
          </Button>
        </Box>
      </Modal>
    </Box>
  );
}
