import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import { Bell, CheckCheck } from 'lucide-react';
import { cn } from '../../lib/cn';
import { useMarkAllNotificationsRead, useMarkNotificationRead, useNotifications, useUnreadCount, } from '../../hooks/useNotifications';
function timeAgo(iso) {
    const diffMs = Date.now() - new Date(iso).getTime();
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 1)
        return 'just now';
    if (minutes < 60)
        return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24)
        return `${hours}h ago`;
    return `${Math.floor(hours / 24)}d ago`;
}
export function NotificationBell() {
    const [open, setOpen] = useState(false);
    const { data: unreadCount } = useUnreadCount();
    const { data: notifications } = useNotifications();
    const markRead = useMarkNotificationRead();
    const markAllRead = useMarkAllNotificationsRead();
    const navigate = useNavigate();
    const handleClick = (n) => {
        if (!n.read)
            markRead.mutate(n.id);
        setOpen(false);
        if (n.link)
            navigate(n.link);
    };
    return (<div className="relative">
      <button onClick={() => setOpen((o) => !o)} aria-label="Notifications" className="relative rounded-lg p-2 text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800">
        <Bell className="size-4.5"/>
        {!!unreadCount && unreadCount > 0 && (<span className="absolute -right-0.5 -top-0.5 flex size-4 items-center justify-center rounded-full bg-pink-500 text-[10px] font-semibold text-white">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>)}
      </button>

      <AnimatePresence>
        {open && (<>
            <div className="fixed inset-0 z-40" onClick={() => setOpen(false)}/>
            <motion.div initial={{ opacity: 0, y: -8 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -8 }} className="absolute right-0 z-50 mt-2 w-80 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl dark:border-slate-800 dark:bg-slate-900">
              <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3 dark:border-slate-800">
                <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">Notifications</span>
                <button onClick={() => markAllRead.mutate()} className="flex items-center gap-1 text-xs text-brand-600 hover:underline dark:text-brand-400">
                  <CheckCheck className="size-3.5"/>
                  Mark all read
                </button>
              </div>
              <div className="max-h-96 overflow-y-auto">
                {!notifications || notifications.content.length === 0 ? (<p className="p-6 text-center text-sm text-slate-400">No notifications yet</p>) : (notifications.content.map((n) => (<button key={n.id} onClick={() => handleClick(n)} className={cn('block w-full border-b border-slate-50 px-4 py-3 text-left text-sm transition-colors last:border-0 hover:bg-slate-50 dark:border-slate-800/60 dark:hover:bg-slate-800/60', !n.read && 'bg-brand-50/50 dark:bg-brand-500/5')}>
                      <p className="text-slate-700 dark:text-slate-300">{n.message}</p>
                      <p className="mt-0.5 text-xs text-slate-400">{timeAgo(n.createdAt)}</p>
                    </button>)))}
              </div>
              <button onClick={() => {
                setOpen(false);
                navigate('/notifications');
            }} className="block w-full border-t border-slate-100 px-4 py-2.5 text-center text-xs font-medium text-brand-600 hover:bg-slate-50 dark:border-slate-800 dark:text-brand-400 dark:hover:bg-slate-800/60">
                View all notifications
              </button>
            </motion.div>
          </>)}
      </AnimatePresence>
    </div>);
}
