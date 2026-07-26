import { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { MailCheck, Rocket } from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { useForgotPassword } from '../hooks/usePasswordReset';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const forgotPassword = useForgotPassword();

  const handleSubmit = (e) => {
    e.preventDefault();
    forgotPassword.mutate(email);
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4 dark:bg-slate-950">
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center gap-2">
          <span className="gradient-brand flex size-10 items-center justify-center rounded-xl text-white">
            <Rocket className="size-5" />
          </span>
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-100">Reset your password</h1>
          <p className="text-center text-sm text-slate-500 dark:text-slate-400">
            Enter the email on your account and we'll send you a reset link.
          </p>
        </div>
        <Card className="space-y-4 p-6">
          {forgotPassword.isSuccess ? (
            <div className="flex flex-col items-center gap-2 py-4 text-center">
              <MailCheck className="size-8 text-brand-500" />
              <p className="text-sm font-medium text-slate-800 dark:text-slate-200">Check your inbox</p>
              <p className="text-sm text-slate-500 dark:text-slate-400">
                If an account exists for <span className="font-medium">{email}</span>, a reset link is on its way. It expires in 30 minutes.
              </p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <Input label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
              <Button type="submit" className="w-full" loading={forgotPassword.isPending}>
                Send reset link
              </Button>
            </form>
          )}
        </Card>
        <p className="mt-4 text-center text-sm text-slate-500 dark:text-slate-400">
          <Link to="/login" className="font-medium text-brand-600 hover:underline dark:text-brand-400">
            Back to log in
          </Link>
        </p>
      </motion.div>
    </div>
  );
}
