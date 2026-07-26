import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import { LogOut, Menu, Moon, Rocket, Sun, X } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useTheme } from '../../hooks/useTheme';
import { Button } from '../ui/Button';
import { cn } from '../../lib/cn';
import { getNavItems } from './navItems';
import { NotificationBell } from './NotificationBell';
export function Navbar() {
    const { user, logout } = useAuth();
    const { theme, toggleTheme } = useTheme();
    const navigate = useNavigate();
    const [mobileOpen, setMobileOpen] = useState(false);
    const handleLogout = () => {
        logout();
        setMobileOpen(false);
        navigate('/');
    };
    const homeLink = user ? `/${user.role.toLowerCase()}/dashboard` : '/';
    const navItems = user ? getNavItems(user.role) : [];
    return (<header className="sticky top-0 z-40 border-b border-slate-200 bg-white/80 backdrop-blur-md dark:border-slate-800 dark:bg-slate-950/80">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
        <div className="flex items-center gap-2">
          {user && (<button onClick={() => setMobileOpen((o) => !o)} aria-label="Toggle menu" className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 md:hidden dark:text-slate-400 dark:hover:bg-slate-800">
              {mobileOpen ? <X className="size-5"/> : <Menu className="size-5"/>}
            </button>)}
          <Link to={homeLink} className="flex items-center gap-2 font-semibold text-slate-900 dark:text-slate-100">
            <span className="gradient-brand flex size-8 items-center justify-center rounded-lg text-white">
              <Rocket className="size-4.5"/>
            </span>
            InnovateFund
          </Link>
        </div>

        <nav className="hidden items-center gap-1 sm:flex">
          {user && (<Link to="/feed" className="rounded-lg px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800">
              Feed
            </Link>)}
        </nav>

        <div className="flex items-center gap-2">
          <button onClick={toggleTheme} aria-label="Toggle theme" className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800">
            {theme === 'dark' ? <Sun className="size-4.5"/> : <Moon className="size-4.5"/>}
          </button>

          {user ? (<div className="flex items-center gap-2">
              <NotificationBell />
              <span className="hidden text-sm font-medium text-slate-700 sm:inline dark:text-slate-300">
                {user.fullName}
              </span>
              <Button variant="ghost" size="sm" onClick={handleLogout}>
                <LogOut className="size-4"/>
                <span className="hidden sm:inline">Log out</span>
              </Button>
            </div>) : (<div className="flex items-center gap-2">
              <Link to="/login">
                <Button variant="ghost" size="sm">
                  Log in
                </Button>
              </Link>
              <Link to="/register">
                <Button variant="primary" size="sm">
                  Get started
                </Button>
              </Link>
            </div>)}
        </div>
      </div>

      <AnimatePresence>
        {user && mobileOpen && (<motion.nav initial={{ height: 0, opacity: 0 }} animate={{ height: 'auto', opacity: 1 }} exit={{ height: 0, opacity: 0 }} className="overflow-hidden border-t border-slate-200 md:hidden dark:border-slate-800">
            <div className="flex flex-col gap-1 p-3">
              {navItems.map(({ to, label, icon: Icon, end }) => (<NavLink key={to} to={to} end={end} onClick={() => setMobileOpen(false)} className={({ isActive }) => cn('flex items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors', isActive
                    ? 'bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
                    : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800')}>
                  <Icon className="size-4.5"/>
                  {label}
                </NavLink>))}
            </div>
          </motion.nav>)}
      </AnimatePresence>
    </header>);
}
