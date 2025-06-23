import { useEffect } from "react";
import { Provider } from "react-redux";
import { RouterProvider } from "react-router-dom";
import { store, persistor } from "./app/store";
import { PersistGate } from "redux-persist/integration/react";
import { router } from "./routes/Router";
import { setCredentials } from "./features/auth/authSlice";
import api from "./api/api";

function App() {
  // 页面加载时自动恢复用户状态
  useEffect(() => {
    const token = localStorage.getItem("token");
    console.log("页面加载时获取到的 token:", token); // 添加日志
    if (token) {
      console.log("开始调用 /api/auth/me 接口..."); // 添加日志
      api
        .get("/auth/me")
        .then((res) => {
          console.log("接口返回数据:", res.data); // 添加日志
          store.dispatch(
            setCredentials({
              token: token,
              userId: res.data.userId,
              username: res.data.username,
              roles: res.data.roles,
            })
          );
        })
        .catch((err) => {
          console.error(
            "恢复登录状态失败:",
            err.response?.data?.message || "token无效或已过期"
          );
          localStorage.removeItem("token");
          alert("登录状态已过期，请重新登录"); // 添加明确提示
        });
    } else {
      console.log("localStorage 中无 token"); // 添加日志
    }
  }, []);

  return (
    <Provider store={store}>
      {/* 新增：使用 PersistGate 等待持久化数据加载 */}
      <PersistGate loading={null} persistor={persistor}>
        <RouterProvider router={router} />
      </PersistGate>
    </Provider>
  );
}

export default App;
