import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { isAxiosError } from 'axios';
import { motion } from 'framer-motion';
import { CheckCircle2, Rocket } from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { useResetPassword } from '../hooks/usePasswordReset';

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();
  const resetPassword = useResetPassword();
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }
    if (!token) {
      toast.error('This reset link is missing its token');
      return;
    }
    resetPassword.mutate(
      { token, newPassword },
      {
        onError: (error) => {
          const message =
            isAxiosError(error) && error.response?.data?.message ? error.response.data.message : 'Could not reset password';
          toast.error(message);
        },
      },
    );
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4 dark:bg-slate-950">
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center gap-2">
          <span className="gradient-brand flex size-10 items-center justify-center rounded-xl text-white">
            <Rocket className="size-5" />
          </span>
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-100">Set a new password</h1>
        </div>
        <Card className="space-y-4 p-6">
          {resetPassword.isSuccess ? (
            <div className="flex flex-col items-center gap-3 py-4 text-center">
              <CheckCircle2 className="size-8 text-emerald-500" />
              <p className="text-sm font-medium text-slate-800 dark:text-slate-200">Password updated</p>
              <Button onClick={() => navigate('/login')} className="w-full">
                Log in
              </Button>
            </div>
          ) : !token ? (
            <p className="text-sm text-slate-500 dark:text-slate-400">
              This link is missing a reset token. Request a new one from the{' '}
              <Link to="/forgot-password" className="font-medium text-brand-600 hover:underline dark:text-brand-400">
                forgot password
              </Link>{' '}
              page.
            </p>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <Input
                label="New password"
                type="password"
                required
                minLength={6}
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
              />
              <Input
                label="Confirm new password"
                type="password"
                required
                minLength={6}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              <Button type="submit" className="w-full" loading={resetPassword.isPending}>
                Update password
              </Button>
            </form>
          )}
        </Card>
      </motion.div>
    </div>
  );
}
