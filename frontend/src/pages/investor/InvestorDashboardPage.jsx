import { Link } from 'react-router-dom';
import { Compass, Heart, Sparkles } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { StartupCard } from '../../components/startup/StartupCard';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { EmptyState } from '../../components/ui/EmptyState';
import { Button } from '../../components/ui/Button';
import { Skeleton } from '../../components/ui/Skeleton';
import { useMyFollowing } from '../../hooks/useFollowing';
import { useInvestorMatches } from '../../hooks/useAiTools';
import { useAuth } from '../../hooks/useAuth';
export function InvestorDashboardPage() {
    const { user } = useAuth();
    const { data: following, isLoading } = useMyFollowing();
    const { data: matches, isLoading: loadingMatches } = useInvestorMatches(true);
    return (<DashboardLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
          Welcome back, {user?.fullName.split(' ')[0]}
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">Startups you follow, at a glance.</p>
      </div>

      <div className="mb-3 flex items-center gap-2">
        <Sparkles className="size-4 text-brand-500"/>
        <h2 className="font-semibold text-slate-900 dark:text-slate-100">Recommended for you</h2>
      </div>
      {loadingMatches ? (<Skeleton className="mb-8 h-24"/>) : !matches || matches.length === 0 ? (<p className="mb-8 text-sm text-slate-400">No recommendations yet — check back once more startups publish.</p>) : (<div className="mb-8 space-y-2">
          {matches.slice(0, 5).map((m) => (<Card key={m.id} className="flex items-center justify-between p-3">
              <Link to={`/startups/${m.id}`} className="text-sm font-medium text-slate-800 hover:underline dark:text-slate-200">
                {m.name}
              </Link>
              <Badge tone="brand">{m.matchPercent.toFixed(0)}% match</Badge>
            </Card>))}
        </div>)}

      <div id="following" className="mb-3 flex items-center gap-2">
        <Heart className="size-4 text-brand-500"/>
        <h2 className="font-semibold text-slate-900 dark:text-slate-100">Following</h2>
      </div>

      {isLoading ? (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[...Array(3)].map((_, i) => (<Skeleton key={i} className="h-44"/>))}
        </div>) : !following || following.length === 0 ? (<EmptyState icon={Compass} title="You're not following any startups yet" description="Browse published startups and follow the ones you're interested in." action={<Link to="/investor/browse">
              <Button size="sm">Discover Startups</Button>
            </Link>}/>) : (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {following.map((s) => (<StartupCard key={s.id} startup={s} to={`/startups/${s.id}`}/>))}
        </div>)}
    </DashboardLayout>);
}
