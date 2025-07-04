import { useState } from "react";
import { useDispatch } from "react-redux";
import { login } from "./authSlice";
import type { AppDispatch } from "../../app/store";
import {
  Box,
  Button,
  TextField,
  Typography,
  Container,
  Paper,
  Alert,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { styled } from "@mui/material/styles";

// 自定义渐变背景容器
const StyledContainer = styled(Container)(({ theme }) => ({
  minHeight: "100vh",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  background: "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)",
  padding: theme.spacing(4),
}));

// 自定义登录卡片
const LoginPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(6),
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  boxShadow: theme.shadows[10],
  borderRadius: Number(theme.shape.borderRadius) * 2,
  transition: "transform 0.3s ease, box-shadow 0.3s ease",
  "&:hover": {
    transform: "translateY(-5px)",
    boxShadow: theme.shadows[15],
  },
}));

// 自定义提交按钮
const StyledButton = styled(Button)(({ theme }) => ({
  mt: 4,
  mb: 2,
  padding: theme.spacing(1.5),
  fontSize: "1rem",
  background: "linear-gradient(45deg, #1976d2 0%, #42a5f5 100%)",
  "&:hover": {
    background: "linear-gradient(45deg, #1565c0 0%, #1e88e5 100%)",
  },
}));

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(""); // 重置错误信息

    if (!username || !password) {
      setError("请输入用户名和密码");
      return;
    }

    try {
      await dispatch(login(username, password));
      navigate("/dashboard");
    } catch {
      setError("登录失败：用户名或密码错误");
    }
  };

  return (
    <StyledContainer as="main" maxWidth="xs">
      <LoginPaper elevation={6}>
        {/* 银行Logo和标题 */}
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            mb: 4,
          }}
        >
          <Typography
            component="h1"
            variant="h4"
            sx={{ fontWeight: 700, color: "#1976d2", mb: 1 }}
          >
            银行客户信息管理系统
          </Typography>
        </Box>

        {/* 错误提示 */}
        {error && (
          <Alert severity="error" sx={{ width: "100%", mb: 3 }}>
            {error}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit} sx={{ width: "100%" }}>
          {/* 用户名输入框 */}
          <TextField
            required
            fullWidth
            id="username"
            label="用户名"
            name="username"
            autoComplete="username"
            autoFocus
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            sx={(theme) => ({
              mb: 2,
              "& .MuiInputLabel-root": {
                zIndex: 2,
                backgroundColor: theme.palette.background.paper,
                px: 1,
                mx: 0.5,
                transition: "all 0.2s ease-out",
              },
              "& .MuiOutlinedInput-root": {
                borderRadius: theme.shape.borderRadius,
                "&.Mui-focused fieldset": {
                  borderColor: "#1976d2",
                  boxShadow: "0 0 0 1px rgba(25, 118, 210, 0.2)",
                  borderWidth: 1.5,
                },
                "&:hover fieldset": {
                  borderColor: "#90caf9",
                },
              },
            })}
          />

          {/* 密码输入框 */}
          <TextField
            required
            fullWidth
            name="password"
            label="密码"
            type="password"
            id="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            sx={(theme) => ({
              mb: 3,
              "& .MuiInputLabel-root": {
                zIndex: 2,
                backgroundColor: theme.palette.background.paper,
                px: 1,
                mx: 0.5,
                transition: "all 0.2s ease-out",
              },
              "& .MuiOutlinedInput-root": {
                borderRadius: theme.shape.borderRadius,
                "&.Mui-focused fieldset": {
                  borderColor: "#1976d2",
                  boxShadow: "0 0 0 1px rgba(25, 118, 210, 0.2)",
                  borderWidth: 1.5,
                },
                "&:hover fieldset": {
                  borderColor: "#90caf9",
                },
              },
            })}
          />

          {/* 登录按钮 */}
          <StyledButton
            type="submit"
            fullWidth
            variant="contained"
            size="large"
          >
            安全登录
          </StyledButton>
        </Box>
      </LoginPaper>
    </StyledContainer>
  );
}
