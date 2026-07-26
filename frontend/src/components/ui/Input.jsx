import { forwardRef, useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { cn } from '../../lib/cn';
export const Input = forwardRef(({ label, error, className, id, type, ...props }, ref) => {
    const [showPassword, setShowPassword] = useState(false);
    const isPassword = type === 'password';
    return (<div className="flex flex-col gap-1.5">
      {label && (<label htmlFor={id} className="text-sm font-medium text-slate-700 dark:text-slate-300">
          {label}
        </label>)}
      <div className="relative">
        <input ref={ref} id={id} type={isPassword ? (showPassword ? 'text' : 'password') : type} className={cn('w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 outline-none transition-colors focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100', isPassword && 'pr-10', error && 'border-red-400 focus:border-red-500 focus:ring-red-500/20', className)} {...props}/>
        {isPassword && (<button type="button" onClick={() => setShowPassword((v) => !v)} tabIndex={-1} aria-label={showPassword ? 'Hide password' : 'Show password'} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 transition-colors hover:text-slate-600 dark:hover:text-slate-300">
            {showPassword ? <EyeOff className="size-4"/> : <Eye className="size-4"/>}
          </button>)}
      </div>
      {error && <span className="text-xs text-red-500">{error}</span>}
    </div>);
});
Input.displayName = 'Input';
