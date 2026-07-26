import { BarChart3, Briefcase, Calendar, DollarSign, Heart, MessageCircle, Star } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { StatCard } from '../../components/ui/StatCard';
import { Skeleton } from '../../components/ui/Skeleton';
import { Button } from '../../components/ui/Button';
import { useInvestorAnalytics } from '../../hooks/useAnalytics';
function formatCurrency(value) {
    if (value == null)
        return '—';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);
}
export function InvestorAnalyticsPage() {
    const { data, isLoading, isError, refetch } = useInvestorAnalytics();
    return (<DashboardLayout>
      <div className="mb-6">
        <h1 className="flex items-center gap-2 text-2xl font-bold text-slate-900 dark:text-slate-100">
          <BarChart3 className="size-6 text-brand-500"/>
          Analytics
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">Your activity and portfolio at a glance.</p>
      </div>

      {isLoading ? (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {[...Array(6)].map((_, i) => (<Skeleton key={i} className="h-20"/>))}
        </div>) : isError || !data ? (<div className="flex flex-col items-center gap-3 py-10 text-center">
          <p className="text-sm text-slate-500 dark:text-slate-400">Couldn't load analytics.</p>
          <Button size="sm" variant="secondary" onClick={() => refetch()}>Try again</Button>
        </div>) : (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard icon={Heart} label="Following" value={data.followingCount} tone="pink"/>
          <StatCard icon={Star} label="Expressed Interest" value={data.interestsCount} tone="amber"/>
          <StatCard icon={Briefcase} label="Portfolio Startups" value={data.portfolioStartupsCount} tone="green"/>
          <StatCard icon={DollarSign} label="Total Invested" value={formatCurrency(data.portfolioInvestedTotal)} tone="green"/>
          <StatCard icon={Calendar} label="Upcoming Meetings" value={`${data.upcomingMeetings} / ${data.totalMeetings}`} tone="brand"/>
          <StatCard icon={MessageCircle} label="Unread Messages" value={data.unreadMessages} tone="pink"/>
        </div>)}
    </DashboardLayout>);
}
