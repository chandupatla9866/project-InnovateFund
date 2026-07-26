import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Spinner } from '../components/ui/Spinner';
export function ProtectedRoute({ children, allowedRoles }) {
    const { user, isLoading } = useAuth();
    if (isLoading) {
        return <Spinner />;
    }
    if (!user) {
        return <Navigate to="/login" replace/>;
    }
    if (allowedRoles && !allowedRoles.includes(user.role)) {
        return <Navigate to={`/${user.role.toLowerCase()}/dashboard`} replace/>;
    }
    return <>{children}</>;
}
