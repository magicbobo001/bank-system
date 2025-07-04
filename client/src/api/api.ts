import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器处理401/403错误
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);
// 新增交易相关API
export const transactionApi = {
  // 获取账户交易历史
  getAccountTransactions: (
    accountId: string,
    startDate?: string,
    endDate?: string
  ) => {
    return api.get(`/transactions/${accountId}/history`, {
      params: { startDate, endDate },
    });
  },
  // 存款
  deposit: (accountId: string, amount: number, operatorId: number) => {
    return api.post(`/transactions/deposit`, null, {
      params: { accountId, amount, operatorId },
    });
  },
  // 取款
  withdraw: (accountId: string, amount: number, operatorId: number) => {
    return api.post(`/transactions/withdraw`, null, {
      params: { accountId, amount, operatorId },
    });
  },
  // 转账
  transfer: (
    fromAccountId: string,
    toAccountId: string,
    amount: number,
    operatorId: number
  ) => {
    return api.post(`/transactions/transfer`, null, {
      params: { fromAccountId, toAccountId, amount, operatorId },
    });
  },
};
// 新增用户相关API
export const userApi = {
  // 更新用户最后登录时间
  updateLastLogin: (userId: number) => {
    return api.put(`/users/${userId}/last-login`);
  },
};
export default api;
