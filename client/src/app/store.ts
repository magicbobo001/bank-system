import { configureStore } from "@reduxjs/toolkit";
import { persistStore, persistReducer } from "redux-persist";
import storage from "redux-persist/lib/storage";
import authReducer from "../features/auth/authSlice";
import { useSelector, type TypedUseSelectorHook } from "react-redux";

// 配置持久化规则（仅持久化auth状态）
const persistConfig = {
  key: "auth",
  storage,
  whitelist: ["user", "token"], // 需要持久化的字段
};
// 包装authReducer为持久化reducer
const persistedAuthReducer = persistReducer(persistConfig, authReducer);
export const store = configureStore({
  reducer: {
    auth: persistedAuthReducer,
    // 其他slice...
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: false, // 关闭序列化检查（persist已处理）
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
export const persistor = persistStore(store); // 创建持久化存储
