import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, BadgeCheck, Calendar, CheckCheck, DollarSign, FolderLock, Heart, MessageCircle, Sparkles, Star, ThumbsUp, } from 'lucide-react';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { EmptyState } from '../components/ui/EmptyState';
import { Skeleton } from '../components/ui/Skeleton';
import { useMarkAllNotificationsRead, useMarkNotificationRead, useNotifications } from '../hooks/useNotifications';
import { cn } from '../lib/cn';
const typeIcons = {
    INVESTOR_FOLLOWED: Heart,
    NEW_COMMENT: MessageCircle,
    NEW_POST_LIKE: ThumbsUp,
    FOUNDER_VERIFIED: BadgeCheck,
    INVESTOR_VERIFIED: BadgeCheck,
    STARTUP_VERIFIED: BadgeCheck,
    MEETING_REQUESTED: Calendar,
    MEETING_ACCEPTED: Calendar,
    MEETING_REJECTED: Calendar,
    DUE_DILIGENCE_REQUESTED: FolderLock,
    DUE_DILIGENCE_APPROVED: FolderLock,
    NEW_MESSAGE: MessageCircle,
    AI_RECOMMENDATION: Sparkles,
    INVESTOR_INTERESTED: Star,
    INVESTMENT_RECEIVED: DollarSign,
    INTEREST_ACCEPTED: BadgeCheck,
    INTEREST_REJECTED: Bell,
};
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
export function NotificationsPage() {
    const navigate = useNavigate();
    const [page, setPage] = useState(0);
    const [items, setItems] = useState([]);
    const { data, isLoading, isFetching } = useNotifications(page);
    const markRead = useMarkNotificationRead();
    const markAllRead = useMarkAllNotificationsRead();
    useEffect(() => {
        if (!data)
            return;
        setItems((prev) => (page === 0 ? data.content : [...prev, ...data.content]));
    }, [data, page]);
    const handleClick = (n) => {
        if (!n.read)
            markRead.mutate(n.id);
        if (n.link)
            navigate(n.link);
    };
    const hasUnread = items.some((n) => !n.read);
    return (<DashboardLayout>
      <div className="mx-auto max-w-2xl space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="flex items-center gap-2 text-2xl font-bold text-slate-900 dark:text-slate-100">
              <Bell className="size-6 text-brand-500"/>
              Notifications
            </h1>
            <p className="text-sm text-slate-500 dark:text-slate-400">Everything that's happened across your account.</p>
          </div>
          {hasUnread && (<Button variant="ghost" size="sm" onClick={() => markAllRead.mutate()}>
              <CheckCheck className="size-4"/>
              Mark all read
            </Button>)}
        </div>

        {isLoading && items.length === 0 ? (<div className="space-y-3">
            {[...Array(5)].map((_, i) => (<Skeleton key={i} className="h-16"/>))}
          </div>) : items.length === 0 ? (<EmptyState icon={Bell} title="No notifications yet" description="Activity on your account will show up here."/>) : (<>
            <Card className="divide-y divide-slate-100 overflow-hidden p-0 dark:divide-slate-800">
              {items.map((n) => {
                const Icon = typeIcons[n.type] ?? Bell;
                return (<button key={n.id} onClick={() => handleClick(n)} className={cn('flex w-full items-start gap-3 px-4 py-3.5 text-left transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/60', !n.read && 'bg-brand-50/50 dark:bg-brand-500/5')}>
                    <div className="mt-0.5 flex size-9 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-300">
                      <Icon className="size-4"/>
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className={cn('text-sm', n.read ? 'text-slate-600 dark:text-slate-400' : 'font-medium text-slate-900 dark:text-slate-100')}>
                        {n.message}
                      </p>
                      <p className="mt-0.5 text-xs text-slate-400">{timeAgo(n.createdAt)}</p>
                    </div>
                    {!n.read && <span className="mt-1.5 size-2 shrink-0 rounded-full bg-brand-500"/>}
                  </button>);
            })}
            </Card>
            {data && !data.last && (<div className="flex justify-center pt-2">
                <Button variant="secondary" size="sm" loading={isFetching} onClick={() => setPage((p) => p + 1)}>
                  Load more
                </Button>
              </div>)}
          </>)}
      </div>
    </DashboardLayout>);
}
