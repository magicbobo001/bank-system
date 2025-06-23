import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../api/api";
import {
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
  Button,
  Box,
} from "@mui/material";
import Navbar from "../../components/Navbar";
interface Transaction {
  transactionId: number;
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  transactionTime: string;
  transactionType: "DEPOSIT" | "WITHDRAW" | "TRANSFER"; // 新增交易类型字段
}

export default function TransactionHistory() {
  const { accountId } = useParams<{ accountId: string }>();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (accountId) {
      setIsLoading(true);
      api
        .get(`/transactions/${accountId}/history`, {
          params: {
            startDate: "2023-01-01", // 可扩展为日期选择器
            endDate: new Date().toISOString().split("T")[0],
          },
        })
        .then((res) => setTransactions(res.data))
        .catch(() => alert("加载交易记录失败"))
        .finally(() => setIsLoading(false));
    }
  }, [accountId]);

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center", // 水平居中
        justifyContent: "flex-start",
        minHeight: "100vh",
        maxWidth: "40%", // 修改：从 "1600px" 改为 "100%"
        margin: "0 auto",
        padding: "20px",
      }}
    >
      <Navbar />
      <Card sx={{ margin: 3 }}>
        <CardContent>
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <Typography variant="h5" gutterBottom>
              交易记录（账户：{accountId}）
            </Typography>
            <Button
              onClick={() => navigate("/dashboard/accounts")}
              variant="outlined"
            >
              返回账户列表
            </Button>
          </div>
          {isLoading ? (
            <Typography>加载中...</Typography>
          ) : (
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
                {transactions.map((t) => (
                  <TableRow key={t.transactionId}>
                    <TableCell>
                      {new Date(t.transactionTime).toLocaleString()}
                    </TableCell>
                    <TableCell>{t.transactionType}</TableCell>
                    <TableCell>{t.amount.toFixed(2)}</TableCell>
                    <TableCell>
                      {t.transactionType === "TRANSFER"
                        ? t.fromAccountId === accountId
                          ? t.toAccountId
                          : t.fromAccountId
                        : "-"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
