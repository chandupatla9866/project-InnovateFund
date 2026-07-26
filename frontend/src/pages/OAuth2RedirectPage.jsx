import { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { AlertCircle } from 'lucide-react';
import { Spinner } from '../components/ui/Spinner';
import { Button } from '../components/ui/Button';
import { useAuth } from '../hooks/useAuth';
export function OAuth2RedirectPage() {
    const [searchParams] = useSearchParams();
    const { loginWithToken } = useAuth();
    const navigate = useNavigate();
    const [error, setError] = useState(null);
    const started = useRef(false);
    useEffect(() => {
        if (started.current)
            return;
        started.current = true;
        const token = searchParams.get('token');
        const oauthError = searchParams.get('error');
        if (oauthError) {
            setError('Google sign-in failed. Please try again or use email/password.');
            return;
        }
        if (!token) {
            setError('No authentication token received.');
            return;
        }
        loginWithToken(token)
            .then((user) => navigate(`/${user.role.toLowerCase()}/dashboard`, { replace: true }))
            .catch(() => setError('Could not complete sign-in. Please try again.'));
    }, [searchParams, loginWithToken, navigate]);
    if (error) {
        return (<div className="flex min-h-screen flex-col items-center justify-center gap-4 p-6 text-center">
        <AlertCircle className="size-10 text-red-500"/>
        <p className="text-slate-600 dark:text-slate-400">{error}</p>
        <Button onClick={() => navigate('/login')}>Back to login</Button>
      </div>);
    }
    return (<div className="flex min-h-screen items-center justify-center">
      <Spinner />
    </div>);
}
