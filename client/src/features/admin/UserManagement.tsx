import { useEffect, useState } from "react";
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
  Box,
  Button,
  Modal,
  TextField,
  FormControl,
} from "@mui/material";
import Navbar from "../../components/Navbar";

interface UserFormData {
  username: string;
  passwordHash: string;
  fullName: string;
  email: string;
  phone: string;
}

interface RoleItem {
  role: {
    roleName: string;
  };
}

interface User {
  userId: number;
  username: string;
  fullName: string;
  email: string;
  phone: string;
  createdAt: string;
  status: string;
  roles: RoleItem[];
}

export default function UserManagement() {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // 注册相关状态
  const [openRegisterModal, setOpenRegisterModal] = useState(false);
  const [registerForm, setRegisterForm] = useState<UserFormData>({
    username: "",
    passwordHash: "",
    fullName: "",
    email: "",
    phone: "",
  });

  // 编辑相关状态
  const [openEditModal, setOpenEditModal] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [editForm, setEditForm] = useState<UserFormData>({
    username: "",
    passwordHash: "",
    fullName: "",
    email: "",
    phone: "",
  });

  // 密码修改相关状态
  const [openPasswordModal, setOpenPasswordModal] = useState(false);
  const [changingPasswordUser, setChangingPasswordUser] = useState<
    number | null
  >(null);
  const [newPassword, setNewPassword] = useState("");
  const [oldPassword, setOldPassword] = useState("");

  const getRoleText = (roleName: string) => {
    switch (roleName) {
      case "USER":
        return "普通用户";
      case "ADMIN":
        return "管理员";
      default:
        return roleName;
    }
  };

  useEffect(() => {
    setIsLoading(true);
    api
      .get("/users")
      .then((res) => setUsers(res.data))
      .catch((err) => alert("加载用户失败：" + err.message))
      .finally(() => setIsLoading(false));
  }, []);

  // 注册用户方法
  const handleRegister = async () => {
    try {
      await api.post("/users/register", registerForm);
      alert("用户注册成功");
      setOpenRegisterModal(false);
      // 刷新用户列表
      api.get("/users").then((res) => setUsers(res.data));
    } catch (err) {
      alert("注册失败：" + (err as Error).message);
    }
  };

  // 编辑用户方法
  const handleEdit = async () => {
    if (!editingUser) return;
    try {
      await api.put("/users/update", {
        ...editForm,
        userId: editingUser.userId,
      });
      alert("用户信息更新成功");
      setOpenEditModal(false);
      api.get("/users").then((res) => setUsers(res.data));
    } catch (err) {
      alert("更新失败：" + (err as Error).message);
    }
  };

  // 修改密码方法
  const handleChangePassword = async () => {
    if (!oldPassword || !newPassword) {
      alert("请输入旧密码和新密码");
      return;
    }
    try {
      await api.put("/users/change-password", null, {
        params: {
          userId: changingPasswordUser,
          oldPassword: oldPassword,
          newPassword: newPassword,
        },
      });
      alert("密码修改成功");
      setOpenPasswordModal(false);
      setOldPassword("");
      setNewPassword("");
    } catch (err) {
      alert("修改失败：" + (err as Error).message);
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
              用户管理
            </Typography>
            {/* 注册按钮 */}
            <Button
              variant="contained"
              onClick={() => setOpenRegisterModal(true)}
            >
              注册用户
            </Button>
          </div>
          {isLoading ? (
            <Typography>加载中...</Typography>
          ) : (
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>用户ID</TableCell>
                  <TableCell>用户名</TableCell>
                  <TableCell>全名</TableCell>
                  <TableCell>邮箱</TableCell>
                  <TableCell>电话</TableCell>
                  <TableCell>注册时间</TableCell>
                  <TableCell>用户状态</TableCell>
                  <TableCell>角色</TableCell>
                  <TableCell>操作</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map((user) => (
                  <TableRow key={user.userId}>
                    <TableCell>{user.userId}</TableCell>
                    <TableCell>{user.username}</TableCell>
                    <TableCell>{user.fullName}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.phone}</TableCell>
                    <TableCell>
                      {new Date(user.createdAt).toLocaleString()}
                    </TableCell>
                    <TableCell>{user.status}</TableCell>
                    <TableCell>
                      {user.roles.map((roleItem, index) => (
                        <span key={index}>
                          {getRoleText(roleItem.role.roleName)}{" "}
                          {index < user.roles.length - 1 ? "、" : ""}
                        </span>
                      ))}
                    </TableCell>
                    <TableCell>
                      {/* 编辑按钮 */}
                      <Button
                        variant="outlined"
                        size="small"
                        onClick={() => {
                          setEditingUser(user);
                          setEditForm({
                            username: user.username,
                            fullName: user.fullName,
                            email: user.email,
                            phone: user.phone,
                            passwordHash: "",
                          });
                          setOpenEditModal(true);
                        }}
                        sx={{ mr: 1 }}
                        disabled={user.username === "loan"}
                      >
                        编辑
                      </Button>
                      {/* 修改密码按钮 */}
                      <Button
                        variant="outlined"
                        size="small"
                        onClick={() => {
                          setChangingPasswordUser(user.userId);
                          setOpenPasswordModal(true);
                        }}
                        disabled={user.username === "loan"}
                      >
                        修改密码
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* 注册模态框 */}
      <Modal
        open={openRegisterModal}
        onClose={() => setOpenRegisterModal(false)}
      >
        <Box
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            bgcolor: "background.paper",
            p: 4,
            width: 400,
          }}
        >
          <Typography variant="h6">注册新用户</Typography>
          <FormControl fullWidth margin="normal">
            <TextField
              label="用户名"
              value={registerForm.username}
              onChange={(e) =>
                setRegisterForm((prev) => ({
                  ...prev,
                  username: e.target.value,
                }))
              }
              required
              sx={{ mb: 2 }}
            />
            <TextField
              label="密码"
              type="password"
              value={registerForm.passwordHash}
              onChange={(e) =>
                setRegisterForm((prev) => ({
                  ...prev,
                  passwordHash: e.target.value,
                }))
              }
              required
              sx={{ mb: 2 }}
            />
            <TextField
              label="全名"
              value={registerForm.fullName}
              onChange={(e) =>
                setRegisterForm((prev) => ({
                  ...prev,
                  fullName: e.target.value,
                }))
              }
              required
              sx={{ mb: 2 }}
            />
            <TextField
              label="邮箱"
              type="email"
              value={registerForm.email}
              onChange={(e) =>
                setRegisterForm((prev) => ({ ...prev, email: e.target.value }))
              }
              required
              sx={{ mb: 2 }}
            />
            <TextField
              label="电话"
              value={registerForm.phone}
              onChange={(e) =>
                setRegisterForm((prev) => ({ ...prev, phone: e.target.value }))
              }
              required
            />
            <Button variant="contained" onClick={handleRegister} sx={{ mt: 2 }}>
              提交注册
            </Button>
          </FormControl>
        </Box>
      </Modal>

      {/* 编辑模态框 */}
      <Modal open={openEditModal} onClose={() => setOpenEditModal(false)}>
        <Box
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            bgcolor: "background.paper",
            p: 4,
            width: 400,
          }}
        >
          <Typography variant="h6">编辑用户信息</Typography>
          <FormControl fullWidth margin="normal">
            <TextField
              label="用户名"
              value={editForm.username}
              onChange={(e) =>
                setEditForm((prev) => ({ ...prev, username: e.target.value }))
              }
              required
              sx={{ mb: 2 }}
            />
            <TextField
              label="全名"
              value={editForm.fullName}
              onChange={(e) =>
                setEditForm((prev) => ({ ...prev, fullName: e.target.value }))
              }
              required
              sx={{ mb: 2 }}
            />
            <TextField
              label="邮箱"
              type="email"
              value={editForm.email}
              onChange={(e) =>
                setEditForm((prev) => ({ ...prev, email: e.target.value }))
              }
              required
              sx={{ mb: 2 }}
            />
            <TextField
              label="电话"
              value={editForm.phone}
              onChange={(e) =>
                setEditForm((prev) => ({ ...prev, phone: e.target.value }))
              }
              required
            />
            <Button variant="contained" onClick={handleEdit} sx={{ mt: 2 }}>
              提交更新
            </Button>
          </FormControl>
        </Box>
      </Modal>

      {/* 密码修改模态框 */}
      <Modal
        open={openPasswordModal}
        onClose={() => setOpenPasswordModal(false)}
      >
        <Box
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            bgcolor: "background.paper",
            p: 4,
            width: 400,
          }}
        >
          <Typography variant="h6">修改用户密码</Typography>
          <FormControl fullWidth margin="normal">
            <TextField
              label="旧密码"
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              required // 强制输入旧密码
              sx={{ mb: 2 }}
            />
            <TextField
              label="新密码"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required // 强制输入新密码
              sx={{ mb: 2 }}
            />
            <Button
              variant="contained"
              onClick={handleChangePassword}
              sx={{ mt: 2 }}
            >
              提交修改
            </Button>
          </FormControl>
        </Box>
      </Modal>
    </Box>
  );
}
