import { Loader2 } from 'lucide-react';
import { cn } from '../../lib/cn';
export function Spinner({ className }) {
    return (<div className="flex items-center justify-center py-10">
      <Loader2 className={cn('size-6 animate-spin text-brand-500', className)}/>
    </div>);
}
