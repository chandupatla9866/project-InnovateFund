import { useEffect, useState } from 'react';
import { Rss, TrendingUp } from 'lucide-react';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { PostComposer } from '../components/feed/PostComposer';
import { PostCard } from '../components/feed/PostCard';
import { EmptyState } from '../components/ui/EmptyState';
import { Skeleton } from '../components/ui/Skeleton';
import { Button } from '../components/ui/Button';
import { useFeed, useTrending } from '../hooks/useFeed';
import { cn } from '../lib/cn';
export function FeedPage() {
    const [tab, setTab] = useState('recent');
    const [page, setPage] = useState(0);
    const [items, setItems] = useState([]);
    const { data, isLoading, isFetching } = useFeed(page);
    const { data: trending, isLoading: loadingTrending } = useTrending();
    useEffect(() => {
        if (!data)
            return;
        setItems((prev) => (page === 0 ? data.content : [...prev, ...data.content]));
    }, [data, page]);
    const showing = tab === 'recent' ? items : trending ?? [];
    const showingLoading = tab === 'recent' ? isLoading : loadingTrending;
    return (<DashboardLayout>
      <div className="mx-auto max-w-2xl space-y-5">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Startup Feed</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">Updates from founders, investors, and admins.</p>
        </div>

        <PostComposer />

        <div className="flex gap-2">
          <button onClick={() => setTab('recent')} className={cn('flex items-center gap-1.5 rounded-xl border px-3 py-1.5 text-sm font-medium transition-colors', tab === 'recent'
            ? 'border-brand-500 bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
            : 'border-slate-200 text-slate-500 dark:border-slate-700')}>
            <Rss className="size-3.5"/>
            Recent
          </button>
          <button onClick={() => setTab('trending')} className={cn('flex items-center gap-1.5 rounded-xl border px-3 py-1.5 text-sm font-medium transition-colors', tab === 'trending'
            ? 'border-brand-500 bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
            : 'border-slate-200 text-slate-500 dark:border-slate-700')}>
            <TrendingUp className="size-3.5"/>
            Trending
          </button>
        </div>

        {showingLoading ? (<div className="space-y-4">
            {[...Array(3)].map((_, i) => (<Skeleton key={i} className="h-40"/>))}
          </div>) : showing.length === 0 ? (<EmptyState icon={Rss} title="No posts yet" description="Be the first to share an update."/>) : (<>
            <div className="space-y-4">
              {showing.map((post) => (<PostCard key={post.id} post={post}/>))}
            </div>
            {tab === 'recent' && data && !data.last && (<div className="flex justify-center pt-2">
                <Button variant="secondary" size="sm" loading={isFetching} onClick={() => setPage((p) => p + 1)}>
                  Load more
                </Button>
              </div>)}
          </>)}
      </div>
    </DashboardLayout>);
}
