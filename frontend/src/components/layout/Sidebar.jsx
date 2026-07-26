import { NavLink } from 'react-router-dom';
import { Users } from 'lucide-react';
import { cn } from '../../lib/cn';
import { useAuth } from '../../hooks/useAuth';
import { getNavItems } from './navItems';
export function Sidebar() {
    const { user } = useAuth();
    if (!user)
        return null;
    const items = getNavItems(user.role);
    return (<aside className="hidden w-60 shrink-0 flex-col gap-1 border-r border-slate-200 p-4 md:flex dark:border-slate-800">
      {user.role === 'ADMIN' && (<div className="mb-2 flex items-center gap-2 rounded-xl bg-brand-50 px-3 py-2 text-xs font-medium text-brand-700 dark:bg-brand-500/10 dark:text-brand-300">
          <Users className="size-3.5"/>
          Admin console
        </div>)}
      {items.map(({ to, label, icon: Icon, end }) => (<NavLink key={to} to={to} end={end} className={({ isActive }) => cn('flex items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors', isActive
                ? 'bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
                : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800')}>
          <Icon className="size-4.5"/>
          {label}
        </NavLink>))}
    </aside>);
}
