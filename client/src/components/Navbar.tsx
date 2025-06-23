import { Link, useNavigate } from "react-router-dom";
import { Button, AppBar, Toolbar, Typography } from "@mui/material";
import { useDispatch } from "react-redux";
import { logout } from "../features/auth/authSlice";
import { useAppSelector } from "../app/store";

export default function Navbar() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user } = useAppSelector((state) => state.auth); // 获取用户角色信息

  const handleLogout = async () => {
    localStorage.removeItem("token"); // 清除本地存储的token
    dispatch(logout()); // 清除Redux中的用户状态
    navigate("/login"); // 跳转登录页
    window.location.reload();
  };
  return (
    <AppBar position="static" sx={{ mb: 4 }}>
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          银行系统
        </Typography>
        <Button component={Link} to="/dashboard" color="inherit">
          首页
        </Button>
        {/* 管理员显示"管理账户信息"，普通用户显示"账户信息" */}
        {user?.roles.includes("ADMIN") ? (
          <Button component={Link} to="/admin/accounts" color="inherit">
            管理账户信息
          </Button>
        ) : (
          <Button component={Link} to="/dashboard/accounts" color="inherit">
            账户信息
          </Button>
        )}
        {user?.roles.includes("ADMIN") && ( // 管理员专属导航项
          <>
            <Button
              component={Link}
              to="/dashboard/admin/users"
              color="inherit"
            >
              用户管理
            </Button>
            <Button component={Link} to="/loans/status" color="inherit">
              贷款管理
            </Button>
          </>
        )}
        <Button component={Link} to="/loans/apply" color="inherit">
          申请贷款
        </Button>
        <Button onClick={handleLogout} color="inherit">
          注销登录
        </Button>
      </Toolbar>
    </AppBar>
  );
}
