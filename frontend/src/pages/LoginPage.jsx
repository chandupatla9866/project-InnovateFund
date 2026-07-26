import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import { Rocket } from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { GoogleButton } from '../components/auth/GoogleButton';
import { useAuth } from '../hooks/useAuth';
import { useOAuthEnabled } from '../hooks/useOAuth';
export function LoginPage() {
    const { login } = useAuth();
    const { data: oauthEnabled } = useOAuthEnabled();
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const user = await login(email, password);
            navigate(`/${user.role.toLowerCase()}/dashboard`);
        }
        catch {
            toast.error('Invalid email or password');
        }
        finally {
            setLoading(false);
        }
    };
    return (<div className="flex min-h-screen items-center justify-center bg-slate-50 p-4 dark:bg-slate-950">
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center gap-2">
          <span className="gradient-brand flex size-10 items-center justify-center rounded-xl text-white">
            <Rocket className="size-5"/>
          </span>
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-100">Welcome back</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">Log in to InnovateFund</p>
        </div>
        <Card className="space-y-4 p-6">
          {oauthEnabled && (<>
              <GoogleButton role="FOUNDER"/>
              <div className="flex items-center gap-3 text-xs text-slate-400">
                <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800"/>
                or continue with email
                <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800"/>
              </div>
            </>)}
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)}/>
            <Input label="Password" type="password" required value={password} onChange={(e) => setPassword(e.target.value)}/>
            <div className="-mt-2 text-right">
              <Link to="/forgot-password" className="text-xs font-medium text-brand-600 hover:underline dark:text-brand-400">
                Forgot password?
              </Link>
            </div>
            <Button type="submit" className="w-full" loading={loading}>
              Log in
            </Button>
          </form>
        </Card>
        <p className="mt-4 text-center text-sm text-slate-500 dark:text-slate-400">
          Don't have an account?{' '}
          <Link to="/register" className="font-medium text-brand-600 hover:underline dark:text-brand-400">
            Sign up
          </Link>
        </p>
      </motion.div>
    </div>);
}
