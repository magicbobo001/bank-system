import { useEffect, useState } from "react";
import {
  Card,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Box,
  Tabs,
  Tab,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  Typography,
} from "@mui/material";
import Grid from "@mui/material/Grid";
import { DatePicker } from "@mui/x-date-pickers";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import { format } from "date-fns";
import api from "../../api/api";
import Navbar from "../../components/Navbar";

interface LoanStatusDTO {
  loanId: number;
  userId: number;
  status: string;
}

interface LoanRepaymentDTO {
  id: number;
  amount: number;
  repaymentDate: string;
  actualRepaymentDate: string | null;
  status: string;
}

interface LoanApplication {
  loanId: number;
  account: { accountId: string };
  amount: number;
  term: number;
  interestRate: number;
  startDate: string;
  endDate: string;
  status: string;
}

interface ApiError {
  response?: {
    data?: {
      message?: string;
    };
  };
}

const LoanStatusManagement = () => {
  const [loans, setLoans] = useState<LoanStatusDTO[]>([]);
  const [filteredLoans, setFilteredLoans] = useState<LoanStatusDTO[]>([]);
  const [pendingLoans, setPendingLoans] = useState<LoanApplication[]>([]);
  const [status, setStatus] = useState("ALL");
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [openRepaymentDialog, setOpenRepaymentDialog] = useState(false);
  const [openScheduleDialog, setOpenScheduleDialog] = useState(false);
  const [openApprovalDialog, setOpenApprovalDialog] = useState(false);
  const [selectedLoanId, setSelectedLoanId] = useState<number | null>(null);
  const [selectedLoan, setSelectedLoan] = useState<LoanApplication | null>(
    null
  );
  const [repaymentDate, setRepaymentDate] = useState<Date | null>(null);
  const [repaymentPlans, setRepaymentPlans] = useState<LoanRepaymentDTO[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  // 贷款状态列表
  const loanStatuses = [
    { value: "ALL", label: "所有状态" },
    { value: "PENDING", label: "待审批" },
    { value: "APPROVED", label: "已审批" },
    { value: "REJECTED", label: "已拒绝" },
    { value: "DISBURSED", label: "已放款" },
    { value: "CLOSED", label: "已还清" },
  ];
  // 辅助函数：安全解析日期字符串，返回有效Date对象或null
  const safeParseDate = (dateString: string | null): Date | null => {
    if (!dateString) return null;
    const date = new Date(dateString);
    return isNaN(date.getTime()) ? null : date;
  };
  // 获取所有贷款状态
  useEffect(() => {
    const fetchLoanStatus = async () => {
      try {
        const res = await api.get("/loans/status");
        setLoans(res.data);
        setFilteredLoans(res.data);
      } catch (err) {
        setError("获取贷款状态失败：" + (err as Error).message);
      }
    };
    fetchLoanStatus();
  }, []);

  // 获取待审批贷款
  useEffect(() => {
    if (status === "PENDING") {
      const fetchPendingLoans = async () => {
        try {
          const res = await api.get("/loans/pending");
          setPendingLoans(res.data);
        } catch (err) {
          setError("获取待审批贷款失败：" + (err as Error).message);
        }
      };
      fetchPendingLoans();
    }
  }, [status]);

  // 根据选中的状态筛选贷款
  useEffect(() => {
    if (status === "ALL") {
      setFilteredLoans(loans);
    } else if (status !== "PENDING") {
      setFilteredLoans(loans.filter((loan) => loan.status === status));
    }
    setPage(0); // 重置页码
  }, [status, loans]);

  // 获取还款计划
  const fetchRepaymentPlans = async (loanId: number) => {
    try {
      const res = await api.get(`/loans/${loanId}/schedule`);
      setRepaymentPlans(res.data);
      setOpenScheduleDialog(true);
    } catch (err) {
      const errorMessage =
        (err as ApiError)?.response?.data?.message || "查询还款计划失败";
      setError(`查询还款计划失败：${errorMessage}`);
    }
  };

  // 提交还款
  const handleRepay = async () => {
    if (!selectedLoanId || !repaymentDate) return;

    try {
      const formattedDate = format(repaymentDate, "yyyy-MM-dd");
      await api.post(`/loans/${selectedLoanId}/repay`, null, {
        params: { repaymentDate: formattedDate },
      });
      setOpenRepaymentDialog(false);
      setSuccessMessage("还款成功");
      // 刷新贷款状态和还款计划
      if (selectedLoanId) {
        fetchRepaymentPlans(selectedLoanId);
      }
      // 重新获取所有贷款状态
      const res = await api.get("/loans/status");
      setLoans(res.data);
      setFilteredLoans(res.data);
    } catch (err) {
      const errorMessage =
        (err as ApiError)?.response?.data?.message || "还款失败";
      setError(`还款失败：${errorMessage}`);
    }
  };

  // 审批贷款
  const handleApprove = async () => {
    if (!selectedLoanId) return;

    try {
      await api.put(`/loans/${selectedLoanId}/approve`);
      setOpenApprovalDialog(false);
      setSuccessMessage("贷款审批成功");
      // 刷新待审批贷款列表
      const res = await api.get("/loans/pending");
      setPendingLoans(res.data);
      // 刷新所有贷款状态
      const statusRes = await api.get("/loans/status");
      setLoans(statusRes.data);
      setFilteredLoans(statusRes.data);
    } catch (err) {
      const errorMessage =
        (err as ApiError)?.response?.data?.message || "贷款审批失败";
      setError(`贷款审批失败：${errorMessage}`);
    }
  };

  // 拒绝贷款
  const handleReject = async () => {
    if (!selectedLoanId) return;

    try {
      await api.put(`/loans/${selectedLoanId}/reject`);
      setOpenApprovalDialog(false);
      setSuccessMessage("贷款已拒绝");
      // 刷新待审批贷款列表
      const res = await api.get("/loans/pending");
      setPendingLoans(res.data);
      // 刷新所有贷款状态
      const statusRes = await api.get("/loans/status");
      setLoans(statusRes.data);
      setFilteredLoans(statusRes.data);
    } catch (err) {
      const errorMessage =
        (err as ApiError)?.response?.data?.message || "拒绝贷款失败";
      setError(`拒绝贷款失败：${errorMessage}`);
    }
  };

  // 分页处理
  const handleChangePage = (_event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const indexOfLastRow = (page + 1) * rowsPerPage;
  const indexOfFirstRow = page * rowsPerPage;
  const currentLoans = filteredLoans.slice(indexOfFirstRow, indexOfLastRow);
  const totalPages = Math.ceil(filteredLoans.length / rowsPerPage);

  // 打开审批对话框
  const openApprovalModal = (loan: LoanApplication) => {
    setSelectedLoan(loan);
    setSelectedLoanId(loan.loanId);
    setOpenApprovalDialog(true);
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          minHeight: "100vh",
          maxWidth: "80%",
          margin: "0 auto",
          padding: "20px",
        }}
      >
        <Navbar />
        <Card sx={{ margin: 3, padding: 2, width: "100%" }}>
          {error && (
            <Alert severity="error" onClose={() => setError(null)}>
              {error}
            </Alert>
          )}
          {successMessage && (
            <Alert severity="success" onClose={() => setSuccessMessage(null)}>
              {successMessage}
            </Alert>
          )}

          <Box sx={{ mb: 3 }}>
            <Tabs
              value={status}
              onChange={(_, newValue) => setStatus(newValue as string)}
              sx={{ mb: 2 }}
            >
              {loanStatuses.map((statusOption) => (
                <Tab
                  key={statusOption.value}
                  value={statusOption.value}
                  label={statusOption.label}
                />
              ))}
            </Tabs>

            {/* 待审批贷款标签内容 - 包含审批功能 */}
            {status === "PENDING" ? (
              <Box sx={{ padding: 2 }}>
                <Typography variant="h6" gutterBottom>
                  待审批贷款列表
                </Typography>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>贷款ID</TableCell>
                      <TableCell>账户ID</TableCell>
                      <TableCell>贷款金额</TableCell>
                      <TableCell>期限(月)</TableCell>
                      <TableCell>年利率(%)</TableCell>
                      <TableCell>开始日期</TableCell>
                      <TableCell>结束日期</TableCell>
                      <TableCell>操作</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pendingLoans.map((loan) => (
                      <TableRow key={loan.loanId}>
                        <TableCell>{loan.loanId}</TableCell>
                        <TableCell>{loan.account.accountId}</TableCell>
                        <TableCell>{loan.amount}</TableCell>
                        <TableCell>{loan.term}</TableCell>
                        <TableCell>{loan.interestRate}</TableCell>
                        <TableCell>
                          {new Date(loan.startDate).toLocaleDateString()}
                        </TableCell>
                        <TableCell>
                          {new Date(loan.endDate).toLocaleDateString()}
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="contained"
                            color="primary"
                            size="small"
                            onClick={() => openApprovalModal(loan)}
                          >
                            审批
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </Box>
            ) : (
              // 其他状态标签内容 - 原有功能
              <Box sx={{ padding: 2 }}>
                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 2,
                  }}
                >
                  <Box>
                    共 {filteredLoans.length} 条记录，当前第 {page + 1} 页，共{" "}
                    {totalPages} 页
                  </Box>
                  <Box>
                    每页显示:
                    <select
                      value={rowsPerPage}
                      onChange={handleChangeRowsPerPage}
                      style={{ marginLeft: 10 }}
                    >
                      <option value={5}>5</option>
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </select>
                  </Box>
                </Box>

                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>贷款ID</TableCell>
                      <TableCell>用户ID</TableCell>
                      <TableCell>贷款状态</TableCell>
                      <TableCell>操作</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {currentLoans.map((loan) => (
                      <TableRow key={loan.loanId}>
                        <TableCell>{loan.loanId}</TableCell>
                        <TableCell>{loan.userId}</TableCell>
                        <TableCell>{loan.status}</TableCell>
                        <TableCell>
                          {loan.status === "APPROVED" && (
                            <Box sx={{ display: "flex", gap: 1 }}>
                              <Button
                                variant="outlined"
                                size="small"
                                onClick={() => {
                                  setSelectedLoanId(loan.loanId);
                                  fetchRepaymentPlans(loan.loanId);
                                }}
                              >
                                查看还款计划
                              </Button>
                              <Button
                                variant="contained"
                                size="small"
                                onClick={() => {
                                  setSelectedLoanId(loan.loanId);
                                  setRepaymentDate(null);
                                  setOpenRepaymentDialog(true);
                                }}
                              >
                                还款操作
                              </Button>
                            </Box>
                          )}
                          {loan.status !== "APPROVED" && <span>-</span>}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>

                {/* 分页控制 */}
                <Box sx={{ display: "flex", justifyContent: "center", mt: 2 }}>
                  <Button
                    disabled={page === 0}
                    onClick={() => handleChangePage(null, page - 1)}
                    sx={{ mr: 1 }}
                  >
                    上一页
                  </Button>
                  <Button
                    disabled={page >= totalPages - 1}
                    onClick={() => handleChangePage(null, page + 1)}
                  >
                    下一页
                  </Button>
                </Box>
              </Box>
            )}
          </Box>
        </Card>

        {/* 还款对话框 */}
        <Dialog
          open={openRepaymentDialog}
          onClose={() => setOpenRepaymentDialog(false)}
        >
          <DialogTitle>贷款还款</DialogTitle>
          <DialogContent>
            <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
              <TextField
                label="贷款ID"
                value={selectedLoanId || ""}
                disabled
                fullWidth
              />
              <DatePicker
                label="还款日期"
                value={repaymentDate}
                onChange={(newValue) => setRepaymentDate(newValue)}
                slotProps={{
                  textField: {
                    fullWidth: true,
                    required: true,
                  },
                }}
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenRepaymentDialog(false)}>取消</Button>
            <Button onClick={handleRepay} variant="contained">
              提交还款
            </Button>
          </DialogActions>
        </Dialog>

        {/* 还款计划对话框 */}
        <Dialog
          open={openScheduleDialog}
          onClose={() => setOpenScheduleDialog(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>还款计划</DialogTitle>
          <DialogContent>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>应还金额</TableCell>
                  <TableCell>应还日期</TableCell>
                  <TableCell>实际还款日期</TableCell>
                  <TableCell>状态</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {repaymentPlans.map((plan) => (
                  <TableRow key={plan.id}>
                    <TableCell>{plan.amount.toFixed(2)}</TableCell>
                    <TableCell>
                      {safeParseDate(plan.repaymentDate)
                        ? format(
                            safeParseDate(plan.repaymentDate)!,
                            "yyyy-MM-dd"
                          )
                        : "无效日期"}
                    </TableCell>
                    <TableCell>
                      {plan.actualRepaymentDate
                        ? safeParseDate(plan.actualRepaymentDate)
                          ? format(
                              safeParseDate(plan.actualRepaymentDate)!,
                              "yyyy-MM-dd"
                            )
                          : "无效日期"
                        : "未还款"}
                    </TableCell>
                    <TableCell>
                      {plan.status === "PAID" ? "已还款" : "未还款"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenScheduleDialog(false)}>关闭</Button>
          </DialogActions>
        </Dialog>

        {/* 贷款审批对话框 */}
        <Dialog
          open={openApprovalDialog}
          onClose={() => setOpenApprovalDialog(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>贷款审批</DialogTitle>
          <DialogContent>
            {selectedLoan && (
              <Grid container spacing={3}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="subtitle1">贷款ID:</Typography>
                  <Typography>{selectedLoan.loanId}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="subtitle1">账户ID:</Typography>
                  <Typography>{selectedLoan.account.accountId}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="subtitle1">贷款金额:</Typography>
                  <Typography>{selectedLoan.amount}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="subtitle1">贷款期限:</Typography>
                  <Typography>{selectedLoan.term} 个月</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="subtitle1">年利率:</Typography>
                  <Typography>{selectedLoan.interestRate}%</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="subtitle1">开始日期:</Typography>
                  <Typography>
                    {new Date(selectedLoan.startDate).toLocaleDateString()}
                  </Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="subtitle1">结束日期:</Typography>
                  <Typography>
                    {new Date(selectedLoan.endDate).toLocaleDateString()}
                  </Typography>
                </Grid>
                <Grid size={12}>
                  <Alert severity="info">
                    请确认贷款信息无误后进行审批操作
                  </Alert>
                </Grid>
              </Grid>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenApprovalDialog(false)}>取消</Button>
            <Button onClick={handleReject} color="error">
              拒绝
            </Button>
            <Button onClick={handleApprove} color="primary" variant="contained">
              批准
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
};

export default LoanStatusManagement;
