import { useEffect, useState } from "react";
import { useAppSelector } from "../../app/store";
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
} from "@mui/material";

// 定义账户类型接口（替换any类型）
interface Account {
  accountId: number;
  type: string;
  balance: number;
  createdAt: string;
}

export default function AccountManagement() {
  const { user } = useAppSelector((state) => state.auth);
  const [accounts, setAccounts] = useState<Account[]>([]);

  // 加载用户账户数据
  useEffect(() => {
    if (user?.userId) {
      api
        .get(`/accounts/my-accounts?userId=${user.userId}`)
        .then((res) => setAccounts(res.data))
        .catch(() => alert("加载账户失败"));
    }
  }, [user?.userId]);

  return (
    <Card sx={{ margin: 3 }}>
      <CardContent>
        <Typography variant="h5" gutterBottom>
          我的账户
        </Typography>
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
                <TableCell>{account.type}</TableCell>
                <TableCell>{account.balance}</TableCell>
                <TableCell>
                  {new Date(account.createdAt).toLocaleDateString()}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}
