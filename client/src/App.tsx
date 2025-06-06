import { useEffect } from "react";
import { Provider } from "react-redux";
import { RouterProvider } from "react-router-dom";
import { store } from "./app/store";
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
        .get("/api/auth/login")
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
          console.log("接口调用失败:", err); // 添加日志
          localStorage.removeItem("token");
        });
    } else {
      console.log("localStorage 中无 token"); // 添加日志
    }
  }, []);

  return (
    <Provider store={store}>
      <RouterProvider router={router} />
    </Provider>
  );
}

export default App;
