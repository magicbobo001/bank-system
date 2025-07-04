import { useEffect, useState } from "react";
import {
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Box,
  Card,
  Typography,
  Alert,
} from "@mui/material";
import { useAppSelector } from "../../app/store";
import api from "../../api/api";
import Navbar from "../../components/Navbar";

interface LoanStatusDTO {
  loanId: number;
  userId: number;
  status: string;
  accountId: string;
  amount?: number;
  term?: number;
  interestRate?: number;
  startDate?: string;
  endDate?: string;
}

export default function UserLoanDetails() {
  const { user } = useAppSelector((state) => state.auth);
  const [loans, setLoans] = useState<LoanStatusDTO[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUserLoans = async () => {
      try {
        const res = await api.get(`/loans/status?userId=${user?.userId}`);
        setLoans(res.data);
      } catch (err) {
        setError("获取贷款信息失败，请稍后重试");
        console.error(err);
      }
    };

    if (user?.userId) {
      fetchUserLoans();
    }
  }, [user]);

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        minHeight: "100vh",
      }}
    >
      <Navbar />
      <Card sx={{ margin: 3, padding: 2, width: "90%" }}>
        <Typography variant="h5" gutterBottom>
          我的贷款详情
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

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
              <TableCell>贷款状态</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loans.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  暂无贷款记录
                </TableCell>
              </TableRow>
            ) : (
              loans.map((loan) => (
                <TableRow key={loan.loanId}>
                  <TableCell>{loan.loanId}</TableCell>
                  <TableCell>{loan.accountId}</TableCell>
                  <TableCell>{loan.amount?.toFixed(2)}</TableCell>
                  <TableCell>{loan.term}</TableCell>
                  <TableCell>{loan.interestRate?.toFixed(2)}</TableCell>
                  <TableCell>
                    {loan.startDate
                      ? new Date(loan.startDate).toLocaleDateString()
                      : "-"}
                  </TableCell>
                  <TableCell>
                    {loan.endDate
                      ? new Date(loan.endDate).toLocaleDateString()
                      : "-"}
                  </TableCell>
                  <TableCell>
                    {(() => {
                      switch (loan.status) {
                        case "PENDING":
                          return "待审批";
                        case "APPROVED":
                          return "已审批";
                        case "DISBURSED":
                          return "已放款";
                        case "CLOSED":
                          return "已还清";
                        case "REJECTED":
                          return "已拒绝";
                        default:
                          return loan.status;
                      }
                    })()}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </Card>
    </Box>
  );
}
