import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import { LineChart, Rocket } from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { GoogleButton } from '../components/auth/GoogleButton';
import { useAuth } from '../hooks/useAuth';
import { useOAuthEnabled } from '../hooks/useOAuth';
export function RegisterPage() {
    const { register } = useAuth();
    const { data: oauthEnabled } = useOAuthEnabled();
    const navigate = useNavigate();
    const [fullName, setFullName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('FOUNDER');
    const [loading, setLoading] = useState(false);
    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const user = await register({ fullName, email, password, role });
            toast.success('Welcome to InnovateFund!');
            navigate(`/${user.role.toLowerCase()}/dashboard`);
        }
        catch {
            toast.error('Could not create account — email may already be in use');
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
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-100">Create your account</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">From idea to investment starts here</p>
        </div>
        <Card className="space-y-4 p-6">
          <div className="grid grid-cols-2 gap-2">
            <button type="button" onClick={() => setRole('FOUNDER')} className={`flex flex-col items-center gap-1.5 rounded-xl border px-3 py-3 text-sm font-medium transition-colors ${role === 'FOUNDER'
            ? 'border-brand-500 bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
            : 'border-slate-200 text-slate-500 hover:border-slate-300 dark:border-slate-700 dark:hover:border-slate-600'}`}>
              <Rocket className="size-5"/>
              I'm a Founder
            </button>
            <button type="button" onClick={() => setRole('INVESTOR')} className={`flex flex-col items-center gap-1.5 rounded-xl border px-3 py-3 text-sm font-medium transition-colors ${role === 'INVESTOR'
            ? 'border-brand-500 bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
            : 'border-slate-200 text-slate-500 hover:border-slate-300 dark:border-slate-700 dark:hover:border-slate-600'}`}>
              <LineChart className="size-5"/>
              I'm an Investor
            </button>
          </div>
          {oauthEnabled && (<>
              <GoogleButton role={role}/>
              <div className="flex items-center gap-3 text-xs text-slate-400">
                <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800"/>
                or continue with email
                <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800"/>
              </div>
            </>)}
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input label="Full name" required value={fullName} onChange={(e) => setFullName(e.target.value)}/>
            <Input label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)}/>
            <Input label="Password" type="password" required minLength={6} value={password} onChange={(e) => setPassword(e.target.value)}/>
            <Button type="submit" className="w-full" loading={loading}>
              Create account
            </Button>
          </form>
        </Card>
        <p className="mt-4 text-center text-sm text-slate-500 dark:text-slate-400">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-brand-600 hover:underline dark:text-brand-400">
            Log in
          </Link>
        </p>
      </motion.div>
    </div>);
}
