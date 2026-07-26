import { useEffect, useState } from 'react';
import { Flame, Search, Sparkles, X } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { StartupCard } from '../../components/startup/StartupCard';
import { EmptyState } from '../../components/ui/EmptyState';
import { Select } from '../../components/ui/Select';
import { Input } from '../../components/ui/Input';
import { Skeleton } from '../../components/ui/Skeleton';
import { Button } from '../../components/ui/Button';
import { usePublishedStartups, useStartupSearch, useTrendingStartups } from '../../hooks/useStartups';
import { cn } from '../../lib/cn';
const industries = ['AI', 'FinTech', 'AgriTech', 'HealthTech', 'EdTech', 'E-commerce', 'SaaS', 'Other'];
const stages = ['IDEA', 'MVP', 'EARLY_TRACTION', 'GROWTH', 'SCALING'];
export function BrowseStartupsPage() {
    const [industry, setIndustry] = useState('');
    const [stage, setStage] = useState('');
    const [country, setCountry] = useState('');
    const [minFunding, setMinFunding] = useState('');
    const [maxFunding, setMaxFunding] = useState('');
    const [minAiScore, setMinAiScore] = useState('');
    const [showTrending, setShowTrending] = useState(false);
    const [page, setPage] = useState(0);
    const [items, setItems] = useState([]);
    const [searchInput, setSearchInput] = useState('');
    const [searchQuery, setSearchQuery] = useState('');
    const [searchPage, setSearchPage] = useState(0);
    const [searchItems, setSearchItems] = useState([]);
    const isSearching = searchQuery.trim().length > 0;
    const { data, isLoading, isFetching } = usePublishedStartups({
        industry: industry || undefined,
        stage: stage || undefined,
        country: country || undefined,
        minFunding: minFunding ? Number(minFunding) : undefined,
        maxFunding: maxFunding ? Number(maxFunding) : undefined,
        minAiScore: minAiScore ? Number(minAiScore) : undefined,
        page,
    });
    const { data: searchData, isLoading: searchLoading, isFetching: searchFetching } = useStartupSearch(searchQuery, searchPage, isSearching);
    const { data: trendingData, isLoading: trendingLoading } = useTrendingStartups(20);
    useEffect(() => {
        setPage(0);
        setItems([]);
    }, [industry, stage, country, minFunding, maxFunding, minAiScore]);
    useEffect(() => {
        if (!data)
            return;
        setItems((prev) => (page === 0 ? data.content : [...prev, ...data.content]));
    }, [data, page]);
    useEffect(() => {
        if (!searchData)
            return;
        setSearchItems((prev) => (searchPage === 0 ? searchData.content : [...prev, ...searchData.content]));
    }, [searchData, searchPage]);
    const runSearch = () => {
        setShowTrending(false);
        setSearchPage(0);
        setSearchItems([]);
        setSearchQuery(searchInput.trim());
    };
    const clearSearch = () => {
        setSearchInput('');
        setSearchQuery('');
        setSearchItems([]);
        setSearchPage(0);
    };
    const hasAdvancedFilters = !!(country || minFunding || maxFunding || minAiScore);
    const showing = showTrending ? trendingData ?? [] : isSearching ? searchItems : items;
    const showingLoading = showTrending ? trendingLoading : isSearching ? searchLoading : isLoading;
    return (<DashboardLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Discover Startups</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">Browse published startups looking for investment.</p>
      </div>

      <div className="mb-4 flex flex-wrap items-center gap-2">
        <div className="relative min-w-64 flex-1">
          <Sparkles className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-brand-500"/>
          <Input value={searchInput} onChange={(e) => setSearchInput(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && runSearch()} placeholder='Try "early-stage fintech startups under $2m"' className="pl-9"/>
        </div>
        <Button variant="primary" size="sm" onClick={runSearch} loading={isSearching && searchFetching && searchPage === 0}>
          Search
        </Button>
        {isSearching && (<Button variant="ghost" size="sm" onClick={clearSearch}>
            <X className="size-4"/>
            Clear
          </Button>)}
        <Button variant={showTrending ? 'primary' : 'secondary'} size="sm" onClick={() => {
            clearSearch();
            setShowTrending((v) => !v);
        }}>
          <Flame className={cn('size-4', showTrending && 'fill-current')}/>
          Trending
        </Button>
      </div>

      {!isSearching && !showTrending && (<div className="mb-6 space-y-3">
          <div className="flex flex-wrap gap-3">
            <Select value={industry} onChange={(e) => setIndustry(e.target.value)} className="w-auto">
              <option value="">All industries</option>
              {industries.map((i) => (<option key={i} value={i}>
                  {i}
                </option>))}
            </Select>
            <Select value={stage} onChange={(e) => setStage(e.target.value)} className="w-auto">
              <option value="">All stages</option>
              {stages.map((s) => (<option key={s} value={s}>
                  {s.replace('_', ' ')}
                </option>))}
            </Select>
            <Input value={country} onChange={(e) => setCountry(e.target.value)} placeholder="Country" className="w-36"/>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <Input type="number" min="0" value={minFunding} onChange={(e) => setMinFunding(e.target.value)} placeholder="Min funding goal (₹)" className="w-48"/>
            <Input type="number" min="0" value={maxFunding} onChange={(e) => setMaxFunding(e.target.value)} placeholder="Max funding goal (₹)" className="w-48"/>
            <Input type="number" min="0" max="100" value={minAiScore} onChange={(e) => setMinAiScore(e.target.value)} placeholder="Min AI score" className="w-36"/>
            {hasAdvancedFilters && (<Button variant="ghost" size="sm" onClick={() => {
                    setCountry('');
                    setMinFunding('');
                    setMaxFunding('');
                    setMinAiScore('');
                }}>
                <X className="size-4"/>
                Clear filters
              </Button>)}
          </div>
        </div>)}

      {showingLoading && showing.length === 0 ? (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[...Array(6)].map((_, i) => (<Skeleton key={i} className="h-44"/>))}
        </div>) : showing.length === 0 ? (<EmptyState icon={showTrending ? Flame : Search} title={showTrending ? 'Nothing trending yet' : isSearching ? 'No startups match your search' : 'No startups match your filters'} description={showTrending ? 'Check back once startups start getting views and followers.' : isSearching ? 'Try a broader query.' : 'Try different filters.'}/>) : (<>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {showing.map((s) => (<StartupCard key={s.id} startup={s} to={`/startups/${s.id}`}/>))}
          </div>
          {!showTrending &&
                (isSearching
                    ? searchData && !searchData.last && (<div className="mt-6 flex justify-center">
                    <Button variant="secondary" size="sm" loading={searchFetching} onClick={() => setSearchPage((p) => p + 1)}>
                      Load more
                    </Button>
                  </div>)
                    : data && !data.last && (<div className="mt-6 flex justify-center">
                    <Button variant="secondary" size="sm" loading={isFetching} onClick={() => setPage((p) => p + 1)}>
                      Load more
                    </Button>
                  </div>))}
        </>)}
    </DashboardLayout>);
}
