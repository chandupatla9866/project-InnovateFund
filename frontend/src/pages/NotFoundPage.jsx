import { Link } from 'react-router-dom';
import { Compass } from 'lucide-react';
import { Button } from '../components/ui/Button';
export function NotFoundPage() {
    return (<div className="flex min-h-screen flex-col items-center justify-center gap-4 p-6 text-center">
      <div className="flex size-16 items-center justify-center rounded-full bg-brand-50 text-brand-500 dark:bg-brand-500/10">
        <Compass className="size-8"/>
      </div>
      <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Page not found</h1>
      <p className="text-slate-500 dark:text-slate-400">The page you're looking for doesn't exist.</p>
      <Link to="/">
        <Button>Back to home</Button>
      </Link>
    </div>);
}
