import { cn } from '../../lib/cn';
export function Skeleton({ className }) {
    return <div className={cn('animate-pulse rounded-lg bg-slate-200 dark:bg-slate-800', className)}/>;
}
