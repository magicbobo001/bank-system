// client/src/components/Navbar.jsx
import { Link } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav style={{ padding: '1rem', backgroundColor: '#f0f0f0' }}>
      <ul style={{ listStyle: 'none', display: 'flex', gap: '2rem' }}>
        <li>
          <Link to="/">首页</Link>
        </li>
        <li>
          <Link to="/accounts">我的账户</Link>
        </li>
        <li>
          <Link to="/login">登录</Link>
        </li>
      </ul>
    </nav>
  );
}