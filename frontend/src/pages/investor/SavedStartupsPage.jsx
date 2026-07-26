import { Bookmark } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { StartupCard } from '../../components/startup/StartupCard';
import { EmptyState } from '../../components/ui/EmptyState';
import { Skeleton } from '../../components/ui/Skeleton';
import { useMySavedStartups } from '../../hooks/useSavedStartups';
export function SavedStartupsPage() {
    const { data: startups, isLoading } = useMySavedStartups();
    return (<DashboardLayout>
      <div className="mb-6 flex items-center gap-2">
        <Bookmark className="size-5 text-brand-500"/>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Saved Startups</h1>
      </div>

      {isLoading ? (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[...Array(3)].map((_, i) => (<Skeleton key={i} className="h-44"/>))}
        </div>) : !startups || startups.length === 0 ? (<EmptyState icon={Bookmark} title="No saved startups yet" description="Tap Save on a startup to bookmark it here for later."/>) : (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {startups.map((s) => (<StartupCard key={s.id} startup={s} to={`/startups/${s.id}`}/>))}
        </div>)}
    </DashboardLayout>);
}
