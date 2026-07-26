import { forwardRef } from 'react';
import { cn } from '../../lib/cn';
export const Select = forwardRef(({ label, className, id, children, ...props }, ref) => {
    return (<div className="flex flex-col gap-1.5">
      {label && (<label htmlFor={id} className="text-sm font-medium text-slate-700 dark:text-slate-300">
          {label}
        </label>)}
      <select ref={ref} id={id} className={cn('w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 outline-none transition-colors focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100', className)} {...props}>
        {children}
      </select>
    </div>);
});
Select.displayName = 'Select';
