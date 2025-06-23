import {
  createAction,
  createSlice,
  type PayloadAction,
} from "@reduxjs/toolkit";
import api from "../../api/api";
import type { AppDispatch } from "../../app/store";

interface AuthState {
  user: {
    userId: number;
    username: string;
    roles: string[];
  } | null;
  token: string | null;
}

const initialState: AuthState = {
  user: null,
  token: localStorage.getItem("token") || null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setCredentials: (
      state,
      action: PayloadAction<{
        token: string;
        userId: number;
        username: string;
        roles: string[];
      }>
    ) => {
      state.token = action.payload.token;
      state.user = {
        userId: action.payload.userId,
        username: action.payload.username,
        roles: action.payload.roles,
      };
      localStorage.setItem("token", action.payload.token);
    },
    logout: (state) => {
      state.token = null;
      state.user = null;
      localStorage.removeItem("token");
    },
  },
});
type UserCredentials = {
  token: string;
  userId: number;
  username: string;
  roles: string[];
} | null;
export const setCredentials = createAction<UserCredentials>(
  "auth/setCredentials"
);

export const login =
  (username: string, password: string) => async (dispatch: AppDispatch) => {
    try {
      const response = await api.post("/auth/login", { username, password });
      console.log("登录接口返回数据:", response.data); // 添加日志，检查是否包含 token
      dispatch(
        setCredentials({
          token: response.data.token,
          userId: response.data.userId,
          username: response.data.username,
          roles: response.data.roles,
        })
      );
    } catch (err: unknown) {
      if (err instanceof Error) {
        alert(err.message);
      }
    }
  };

export default authSlice.reducer;
export const { logout } = authSlice.actions;
