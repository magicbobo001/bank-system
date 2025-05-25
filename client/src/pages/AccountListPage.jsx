// client/src/pages/AccountListPage.jsx
import { useEffect, useState } from 'react';
import PrimaryButton from '../components/PrimaryButton';
import LoadingSpinner from '../components/LoadingSpinner';
import axios from 'axios';

export default function AccountListPage() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 模拟 API 调用
    axios.get('/api/accounts/my-accounts?userId=1')
      .then((res) => {
        setAccounts(res.data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;

  return (
    <div style={{ padding: '2rem' }}>
      <h2>我的账户</h2>
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {accounts.map((account) => (
          <li
            key={account.accountId}
            style={{ padding: '1rem', border: '1px solid #ddd', marginBottom: '1rem' }}
          >
            <p>账户号: {account.accountId}</p>
            <p>类型: {account.accountType}</p>
            <p>余额: ${account.balance}</p>
          </li>
        ))}
      </ul>
      <PrimaryButton onClick={() => alert('功能待实现')}>申请新账户</PrimaryButton>
    </div>
  );
}