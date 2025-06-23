import { createBrowserRouter, Navigate } from "react-router-dom";
import Login from "../features/auth/Login";
import Dashboard from "../features/dashboard/Dashboard";
import PrivateRoute from "./PrivateRoute";
import AccountManagement from "../features/accounts/AccountManagement";
import TransactionHistory from "../features/transactions/TransactionHistory";
import LoanApplication from "../features/loans/LoanApplication";
import { RouterProvider } from "react-router-dom";
import UserManagement from "../features/admin/UserManagement";
import AdminAccountManagement from "../features/admin/AdminAccountManagement";
import LoanStatusManagement from "../features/admin/LoanStatusManagement";
export const router = createBrowserRouter([
  {
    path: "/",
    children: [
      // 直接定义子路由，不使用 <App /> 作为根元素
      { index: true, element: <Navigate to="/login" replace /> },
      { path: "login", element: <Login /> },
      { path: "/transactions/:accountId", element: <TransactionHistory /> },
      { path: "/loans/apply", element: <LoanApplication /> },
      {
        path: "dashboard",
        element: <PrivateRoute roles={["USER", "ADMIN"]} />,
        children: [
          { index: true, element: <Dashboard /> },
          { path: "accounts", element: <AccountManagement /> },
          {
            path: "admin/users",
            element: <PrivateRoute roles={["ADMIN"]} />, // 仅管理员可访问
            children: [{ index: true, element: <UserManagement /> }],
          },
        ],
      },
      // 管理员账户管理路由
      {
        path: "/admin/accounts",
        element: <PrivateRoute roles={["ADMIN"]} />,
        children: [{ index: true, element: <AdminAccountManagement /> }],
      },
      {
        path: "/loans/status",
        element: <PrivateRoute roles={["ADMIN"]} />,
        children: [{ index: true, element: <LoanStatusManagement /> }],
      },
    ],
  },
]);

<RouterProvider router={router} />;
