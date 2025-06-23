import { useEffect, useState } from "react";
import { useAppSelector } from "../../app/store";
import api from "../../api/api";
import { useNavigate } from "react-router-dom";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import {
  Box,
  Button,
  TextField,
  Typography,
  Container,
  InputLabel,
  Select,
  MenuItem,
  FormControl,
} from "@mui/material";
import Navbar from "../../components/Navbar";
interface Account {
  accountId: string;
  accountType: string;
}
export default function LoanApplication() {
  const { user } = useAppSelector((state) => state.auth);
  const [amount, setAmount] = useState("");
  const [term, setTerm] = useState(12); // 默认12期
  const [annualRate, setAnnualRate] = useState("5.5"); // 默认年利率5.5%
  const [accountId, setAccountId] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const [startDate, setStartDate] = useState<Date | null>(null);
  // 加载用户账户列表（用于选择收款账户）
  const [accounts, setAccounts] = useState<Account[]>([]);
  useEffect(() => {
    if (user?.userId) {
      api.get(`/accounts/my-accounts?userId=${user.userId}`).then((res) => {
        setAccounts(
          res.data.map((acc: Account) => ({
            accountId: acc.accountId,
            accountType: acc.accountType,
          }))
        );
      });
    }
  }, [user?.userId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.userId || !accountId || !startDate) {
      alert("请填写所有必填字段并选择有效的贷款起始日期");
      return;
    }
    const minValidDate = new Date(Date.now() + 15 * 24 * 60 * 60 * 1000);
    if (startDate < minValidDate) {
      alert("贷款起始日期必须至少为当前日期后15天");
      return;
    }
    setIsLoading(true);
    try {
      await api.post("/loans/apply", {
        userId: user.userId,
        accountId: accountId,
        amount: parseFloat(amount),
        term: term,
        annualRate: parseFloat(annualRate),
        startDate: startDate?.toISOString().split("T")[0],
      });
      alert("贷款申请已提交");
      navigate("/dashboard");
    } catch (err) {
      alert("申请失败：" + (err as Error).message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center", // 水平居中
        justifyContent: "flex-start",
        minHeight: "100vh",
        maxWidth: "80%",
        margin: "0 auto",
        padding: "20px",
      }}
    >
      <Navbar />
      <Container component="main" maxWidth="sm" sx={{ mt: 8 }}>
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            p: 4,
            border: "1px solid #e0e0e0",
            borderRadius: 2,
          }}
        >
          <Typography component="h1" variant="h5">
            贷款申请
          </Typography>
          <Box
            component="form"
            onSubmit={handleSubmit}
            sx={{ mt: 1, width: "100%" }}
          >
            <FormControl fullWidth margin="normal" required>
              <InputLabel>收款账户</InputLabel>
              <Select
                label="收款账户"
                value={accountId}
                onChange={(e) => setAccountId(e.target.value as string)}
              >
                {accounts.map((acc) => (
                  <MenuItem key={acc.accountId} value={acc.accountId}>
                    {acc.accountType}（{acc.accountId}）
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              margin="normal"
              required
              fullWidth
              label="贷款金额（元）"
              type="number"
              inputProps={{ min: 1000, step: 100 }}
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              label="贷款期限（月）"
              type="number"
              inputProps={{ min: 6, max: 60 }}
              value={term}
              onChange={(e) => setTerm(Number(e.target.value))}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              label="年利率（%）"
              type="number"
              inputProps={{ min: 3, max: 24, step: 0.01 }}
              value={annualRate}
              onChange={(e) => setAnnualRate(e.target.value)}
            />
            <LocalizationProvider dateAdapter={AdapterDateFns}>
              <DatePicker
                label="贷款起始日期"
                value={startDate}
                onChange={(newValue) => setStartDate(newValue)}
                slotProps={{
                  textField: {
                    margin: "normal",
                    required: true,
                    fullWidth: true,
                    helperText: "请选择至少15天后的日期",
                  },
                }}
                minDate={new Date(Date.now() + 16 * 24 * 60 * 60 * 1000)} // 当前日期+15天
                disablePast
              />
            </LocalizationProvider>
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={isLoading}
            >
              {isLoading ? "提交中..." : "提交申请"}
            </Button>
          </Box>
        </Box>
      </Container>
    </Box>
  );
}
