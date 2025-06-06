import { Navigate, Outlet } from 'react-router-dom'; 
import { useAppSelector } from '../app/store';

export default function PrivateRoute({ roles }: { roles: string[] }) {
    const { user } = useAppSelector(state => state.auth);
    
    if (!user) return <Navigate to="/login" />;
    if (!roles.some(role => user.roles.includes(role))) return <Navigate to="/" />;
    
    return <Outlet />;
  }