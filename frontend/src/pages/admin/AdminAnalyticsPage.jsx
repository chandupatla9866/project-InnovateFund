import { AlertTriangle, BarChart3, Building2, Calendar, DollarSign, FileText, Rocket, Users, } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { StatCard } from '../../components/ui/StatCard';
import { Skeleton } from '../../components/ui/Skeleton';
import { Button } from '../../components/ui/Button';
import { usePlatformAnalytics } from '../../hooks/useAnalytics';
function formatCurrency(value) {
    if (value == null)
        return '—';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);
}
export function AdminAnalyticsPage() {
    const { data, isLoading, isError, refetch } = usePlatformAnalytics();
    return (<DashboardLayout>
      <div className="mb-6">
        <h1 className="flex items-center gap-2 text-2xl font-bold text-slate-900 dark:text-slate-100">
          <BarChart3 className="size-6 text-brand-500"/>
          Platform Analytics
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">Platform-wide totals across all users and startups.</p>
      </div>

      {isLoading ? (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {[...Array(8)].map((_, i) => (<Skeleton key={i} className="h-20"/>))}
        </div>) : isError || !data ? (<div className="flex flex-col items-center gap-3 py-10 text-center">
          <p className="text-sm text-slate-500 dark:text-slate-400">Couldn't load analytics.</p>
          <Button size="sm" variant="secondary" onClick={() => refetch()}>Try again</Button>
        </div>) : (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard icon={Users} label="Total Users" value={data.totalUsers} tone="brand"/>
          <StatCard icon={Users} label="Founders" value={data.totalFounders} tone="brand"/>
          <StatCard icon={Users} label="Investors" value={data.totalInvestors} tone="brand"/>
          <StatCard icon={Rocket} label="Total Startups" value={data.totalStartups} tone="green"/>
          <StatCard icon={Building2} label="Published Startups" value={data.totalPublishedStartups} tone="green"/>
          <StatCard icon={AlertTriangle} label="Pending Verifications" value={data.pendingStartupVerifications + data.pendingFounderVerifications + data.pendingInvestorVerifications} tone="amber"/>
          <StatCard icon={FileText} label="Total Posts" value={data.totalPosts} tone="brand"/>
          <StatCard icon={DollarSign} label="Total Investments" value={data.totalInvestments} tone="green"/>
          <StatCard icon={DollarSign} label="Investment Volume" value={formatCurrency(data.totalInvestmentVolume)} tone="green"/>
          <StatCard icon={Calendar} label="Total Meetings" value={data.totalMeetings} tone="brand"/>
          <StatCard icon={AlertTriangle} label="Pending Reports" value={data.pendingReports} tone="amber"/>
        </div>)}
    </DashboardLayout>);
}
