import { forwardRef } from 'react';
import { cn } from '../../lib/cn';
export const TextArea = forwardRef(({ label, error, className, id, rows = 4, ...props }, ref) => {
    return (<div className="flex flex-col gap-1.5">
        {label && (<label htmlFor={id} className="text-sm font-medium text-slate-700 dark:text-slate-300">
            {label}
          </label>)}
        <textarea ref={ref} id={id} rows={rows} className={cn('w-full resize-y rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 outline-none transition-colors focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100', error && 'border-red-400 focus:border-red-500 focus:ring-red-500/20', className)} {...props}/>
        {error && <span className="text-xs text-red-500">{error}</span>}
      </div>);
});
TextArea.displayName = 'TextArea';
