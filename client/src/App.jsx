// client/src/App.jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import AccountListPage from './pages/AccountListPage';
import TransactionPage from './pages/TransactionPage';
import Navbar from './components/Navbar';

function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/accounts" element={<AccountListPage />} />
        <Route path="/transactions" element={<TransactionPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;