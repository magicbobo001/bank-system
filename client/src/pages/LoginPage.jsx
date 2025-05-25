// client/src/pages/LoginPage.jsx
import { useState } from 'react';
import { useDispatch } from 'react-redux';
import { login } from '../store/authSlice';
import PrimaryButton from '../components/PrimaryButton';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const dispatch = useDispatch();

  const handleSubmit = (e) => {
    e.preventDefault();
    dispatch(login({ username })); // 触发 Redux action
    alert(`模拟登录成功，用户名: ${username}`); // 临时反馈
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '400px', margin: '0 auto' }}>
      <h2>用户登录</h2>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label>用户名:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={{ width: '100%', padding: '0.5rem' }}
          />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>密码:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ width: '100%', padding: '0.5rem' }}
          />
        </div>
        <PrimaryButton type="submit">登录</PrimaryButton>
      </form>
    </div>
  );
}