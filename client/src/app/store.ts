import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import { useSelector, type TypedUseSelectorHook } from 'react-redux';
export const store = configureStore({
  reducer: {
    auth: authReducer,
    // 其他slice...
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector; 