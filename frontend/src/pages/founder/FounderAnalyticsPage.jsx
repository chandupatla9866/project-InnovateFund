import { Link } from 'react-router-dom';
import { BarChart3, Calendar, DollarSign, Eye, Heart, MessageCircle, Sparkles, Star, Users, } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { StatCard } from '../../components/ui/StatCard';
import { Skeleton } from '../../components/ui/Skeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { Button } from '../../components/ui/Button';
import { useFounderAnalytics } from '../../hooks/useAnalytics';
function formatCurrency(value) {
    if (value == null)
        return '—';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);
}
export function FounderAnalyticsPage() {
    const { data, isLoading, isError, refetch } = useFounderAnalytics();
    return (<DashboardLayout>
      <div className="mb-6">
        <h1 className="flex items-center gap-2 text-2xl font-bold text-slate-900 dark:text-slate-100">
          <BarChart3 className="size-6 text-brand-500"/>
          Analytics
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">Performance across all of your startups.</p>
      </div>

      {isLoading ? (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {[...Array(8)].map((_, i) => (<Skeleton key={i} className="h-20"/>))}
        </div>) : isError || !data ? (<div className="flex flex-col items-center gap-3 py-10 text-center">
          <p className="text-sm text-slate-500 dark:text-slate-400">Couldn't load analytics.</p>
          <Button size="sm" variant="secondary" onClick={() => refetch()}>Try again</Button>
        </div>) : (<>
          <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <StatCard icon={Eye} label="Total Views" value={data.totalViews} tone="brand"/>
            <StatCard icon={Heart} label="Followers" value={data.totalFollowers} tone="pink"/>
            <StatCard icon={Star} label="Post Likes" value={data.totalLikes} tone="amber"/>
            <StatCard icon={Users} label="Interested Investors" value={data.totalInterestedInvestors} tone="green"/>
            <StatCard icon={DollarSign} label="Funding Raised" value={formatCurrency(data.totalFundingProgress)} tone="green"/>
            <StatCard icon={DollarSign} label="Funding Goal" value={formatCurrency(data.totalFundingGoal)} tone="brand"/>
            <StatCard icon={Calendar} label="Upcoming Meetings" value={`${data.upcomingMeetings} / ${data.totalMeetings}`} tone="amber"/>
            <StatCard icon={MessageCircle} label="Unread Messages" value={data.unreadMessages} tone="pink"/>
          </div>

          <h2 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">Per-Startup Breakdown</h2>
          {data.startups.length === 0 ? (<EmptyState icon={BarChart3} title="No startups yet" description="Create a startup to start tracking analytics."/>) : (<div className="space-y-3">
              {data.startups.map((s) => {
                    const progressPct = s.fundingGoal && s.fundingGoal > 0
                        ? Math.min(100, Math.round(((s.fundingProgress ?? 0) / s.fundingGoal) * 100))
                        : 0;
                    return (<Card key={s.id} className="p-4">
                    <div className="mb-3 flex items-center justify-between">
                      <Link to={`/startups/${s.id}`} className="font-semibold text-slate-900 hover:underline dark:text-slate-100">
                        {s.name}
                      </Link>
                      {s.latestAiScore != null && (<Badge tone="brand">
                          <Sparkles className="size-3"/>
                          AI Score {Math.round(s.latestAiScore)}
                        </Badge>)}
                    </div>
                    <div className="grid grid-cols-2 gap-3 text-sm sm:grid-cols-4">
                      <div>
                        <p className="text-slate-400">Views</p>
                        <p className="font-medium text-slate-800 dark:text-slate-200">{s.viewCount}</p>
                      </div>
                      <div>
                        <p className="text-slate-400">Followers</p>
                        <p className="font-medium text-slate-800 dark:text-slate-200">{s.followerCount}</p>
                      </div>
                      <div>
                        <p className="text-slate-400">Likes</p>
                        <p className="font-medium text-slate-800 dark:text-slate-200">{s.likeCount}</p>
                      </div>
                      <div>
                        <p className="text-slate-400">Interested</p>
                        <p className="font-medium text-slate-800 dark:text-slate-200">{s.interestedInvestorsCount}</p>
                      </div>
                    </div>
                    {s.fundingGoal != null && (<div className="mt-3 space-y-1">
                        <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
                          <span>{formatCurrency(s.fundingProgress)} raised</span>
                          <span>{formatCurrency(s.fundingGoal)} goal</span>
                        </div>
                        <div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
                          <div className="gradient-brand h-full rounded-full" style={{ width: `${progressPct}%` }}/>
                        </div>
                      </div>)}
                  </Card>);
                })}
            </div>)}
        </>)}
    </DashboardLayout>);
}
