// client/src/pages/TransactionPage.jsx
import { useState, useEffect } from 'react';
import axios from 'axios';
import LoadingSpinner from '../components/LoadingSpinner';

export default function TransactionPage() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get('/api/transactions?accountId=62258812345678')
      .then((res) => {
        setTransactions(res.data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;

  return (
    <div style={{ padding: '2rem' }}>
      <h2>交易记录</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ backgroundColor: '#f0f0f0' }}>
            <th style={{ padding: '0.5rem' }}>时间</th>
            <th style={{ padding: '0.5rem' }}>类型</th>
            <th style={{ padding: '0.5rem' }}>金额</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => (
            <tr key={transaction.transactionId} style={{ borderBottom: '1px solid #ddd' }}>
              <td style={{ padding: '0.5rem' }}>{new Date(transaction.transactionTime).toLocaleString()}</td>
              <td style={{ padding: '0.5rem' }}>{transaction.transactionType}</td>
              <td style={{ padding: '0.5rem' }}>${transaction.amount}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}