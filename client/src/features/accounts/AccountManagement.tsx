import { useEffect, useState } from "react";
import { useAppSelector } from "../../app/store";
import api from "../../api/api";
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
} from "@mui/material";
import Navbar from "../../components/Navbar";
// 定义账户类型接口
interface Account {
  accountId: string; // 后端 accountId 是字符串类型（实体类中为 String），前端需匹配
  accountType: string; // 修正为 accountType，与后端字段一致
  balance: number;
  createdAt: string;
}

export default function AccountManagement() {
  const { user } = useAppSelector((state) => state.auth);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(false); // 新增加载状态

  // 加载用户账户数据
  useEffect(() => {
    if (user?.userId) {
      setIsLoading(true); // 开始加载时标记为加载中
      api
        .get(`/accounts/my-accounts?userId=${user.userId}`)
        .then((res) => {
          setAccounts(res.data);
          //setAccounts((prev) => [...prev]);
        })
        .catch((err) => {
          // 新增：打印具体错误信息
          console.error(
            "加载账户失败:",
            err.response?.status,
            err.response?.data
          );
          alert(`加载账户失败（状态码：${err.response?.status}）`);
        })
        .finally(() => setIsLoading(false)); // 无论成功/失败都结束加载状态;
    }
  }, [user?.userId]);

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center", // 水平居中
        justifyContent: "flex-start",
        minHeight: "100vh",
        maxWidth: "80%", // 修改：从 "1600px" 改为 "100%"
        margin: "0 auto",
        padding: "20px",
      }}
    >
      <Navbar /> {/* 新增导航栏 */}
      <Card sx={{ margin: 3 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>
            我的账户
          </Typography>
          {isLoading ? (
            <Typography>加载中...</Typography>
          ) : (
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>账户ID</TableCell>
                  <TableCell>账户类型</TableCell>
                  <TableCell>余额（元）</TableCell>
                  <TableCell>开户时间</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {accounts.map((account) => (
                  <TableRow key={account.accountId}>
                    <TableCell>{account.accountId}</TableCell>
                    <TableCell>{account.accountType}</TableCell>
                    <TableCell>{account.balance.toFixed(2)}</TableCell>
                    <TableCell>
                      {new Date(account.createdAt).toLocaleString()}
                    </TableCell>
                    <TableCell>
                      <Button
                        component="a"
                        href={`/transactions/${account.accountId}`}
                        variant="contained"
                        size="small"
                      >
                        查看交易记录
                      </Button>
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
