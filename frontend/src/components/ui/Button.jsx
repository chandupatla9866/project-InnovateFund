import { motion } from 'framer-motion';
import { Loader2 } from 'lucide-react';
import { cn } from '../../lib/cn';
const variantClasses = {
    primary: 'gradient-brand text-white shadow-lg shadow-brand-500/25 hover:shadow-brand-500/40 hover:brightness-105',
    secondary: 'bg-white text-slate-900 border border-slate-200 hover:bg-slate-50 dark:bg-slate-900 dark:text-slate-100 dark:border-slate-700 dark:hover:bg-slate-800',
    ghost: 'bg-transparent text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800',
    danger: 'bg-red-500 text-white hover:bg-red-600',
};
const sizeClasses = {
    sm: 'px-3 py-1.5 text-sm rounded-lg gap-1.5',
    md: 'px-4 py-2.5 text-sm rounded-xl gap-2',
    lg: 'px-6 py-3 text-base rounded-xl gap-2',
};
export function Button({ variant = 'primary', size = 'md', loading = false, disabled, className, children, ...props }) {
    return (<motion.button whileTap={{ scale: 0.97 }} whileHover={{ scale: disabled || loading ? 1 : 1.02 }} disabled={disabled || loading} className={cn('inline-flex items-center justify-center font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed', variantClasses[variant], sizeClasses[size], className)} {...props}>
      {loading && <Loader2 className="size-4 animate-spin"/>}
      {children}
    </motion.button>);
}
