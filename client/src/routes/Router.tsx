import { createBrowserRouter, Navigate } from "react-router-dom";
import Login from "../features/auth/Login";
import Dashboard from "../features/dashboard/Dashboard";
import PrivateRoute from "./PrivateRoute";
import AccountManagement from "../features/accounts/AccountManagement";

import { RouterProvider } from "react-router-dom";
export const router = createBrowserRouter([
  {
    path: "/",
    children: [
      // 直接定义子路由，不再使用 <App /> 作为根元素
      { index: true, element: <Navigate to="/login" replace /> },
      { path: "login", element: <Login /> },
      {
        path: "dashboard",
        element: <PrivateRoute roles={["USER", "ADMIN"]} />,
        children: [
          { index: true, element: <Dashboard /> },
          { path: "accounts", element: <AccountManagement /> },
        ],
      },
    ],
  },
]);

<RouterProvider router={router} />;
