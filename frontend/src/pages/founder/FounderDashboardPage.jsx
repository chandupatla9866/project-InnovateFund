import { Link } from 'react-router-dom';
import { PlusCircle, Rocket } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { StartupCard } from '../../components/startup/StartupCard';
import { EmptyState } from '../../components/ui/EmptyState';
import { Button } from '../../components/ui/Button';
import { Skeleton } from '../../components/ui/Skeleton';
import { useMyStartups } from '../../hooks/useStartups';
import { useAuth } from '../../hooks/useAuth';
export function FounderDashboardPage() {
    const { user } = useAuth();
    const { data: startups, isLoading } = useMyStartups();
    return (<DashboardLayout>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
            Welcome back, {user?.fullName.split(' ')[0]}
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">Manage your startups and track their progress.</p>
        </div>
        <Link to="/founder/startups/new">
          <Button>
            <PlusCircle className="size-4"/>
            New Startup
          </Button>
        </Link>
      </div>

      {isLoading ? (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[...Array(3)].map((_, i) => (<Skeleton key={i} className="h-44"/>))}
        </div>) : !startups || startups.length === 0 ? (<EmptyState icon={Rocket} title="No startups yet" description="Create your first startup profile to start the journey from idea to investment." action={<Link to="/founder/startups/new">
              <Button size="sm">Create Startup</Button>
            </Link>}/>) : (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {startups.map((s) => (<StartupCard key={s.id} startup={s} to={`/founder/startups/${s.id}/edit`} viewTo={`/startups/${s.id}`}/>))}
        </div>)}
    </DashboardLayout>);
}
