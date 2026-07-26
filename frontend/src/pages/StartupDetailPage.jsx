import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Bookmark, Calendar, DollarSign, FolderLock, Globe, Heart, MessageCircle, Rocket, ShieldCheck, Sparkles, Star, TriangleAlert } from 'lucide-react';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { EmptyState } from '../components/ui/EmptyState';
import { PostCard } from '../components/feed/PostCard';
import { ScheduleMeetingModal } from '../components/meeting/ScheduleMeetingModal';
import { RecordInvestmentModal } from '../components/startup/RecordInvestmentModal';
import { MilestonesCard } from '../components/startup/MilestonesCard';
import { TeamCard } from '../components/startup/TeamCard';
import { useStartup, useFollowStartup, useSaveStartup } from '../hooks/useStartups';
import { useStartupPosts } from '../hooks/useFeed';
import { useMyFollowing } from '../hooks/useFollowing';
import { useMySavedStartups } from '../hooks/useSavedStartups';
import { useAcceptInterest, useExpressInterest, useInterestedInvestors, useMyInterests, useRejectInterest } from '../hooks/useInterest';
import { useAuth } from '../hooks/useAuth';
import { useReportSummary } from '../hooks/useAiReport';
function formatCurrency(value) {
    if (value == null)
        return '—';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);
}
const fields = [
    { key: 'problem', label: 'Problem' },
    { key: 'solution', label: 'Solution' },
    { key: 'businessModel', label: 'Business Model' },
    { key: 'revenueModel', label: 'Revenue Model' },
    { key: 'targetAudience', label: 'Target Audience' },
    { key: 'market', label: 'Market' },
    { key: 'competitors', label: 'Competitors' },
];
export function StartupDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();
    // Funding progress moves via a background job once a Razorpay payment actually clears — poll
    // gently here (not on every page that touches startup data) so the raised amount updates on
    // its own instead of needing a manual reload.
    const { data: startup, isLoading, isError } = useStartup(id, { refetchInterval: 20_000 });
    const { data: posts } = useStartupPosts(id);
    const { data: following } = useMyFollowing(user?.role === 'INVESTOR');
    const { data: myInterests } = useMyInterests(user?.role === 'INVESTOR');
    const { data: savedStartups } = useMySavedStartups(user?.role === 'INVESTOR');
    const followMutation = useFollowStartup();
    const expressInterestMutation = useExpressInterest();
    const saveStartupMutation = useSaveStartup();
    const [scheduleOpen, setScheduleOpen] = useState(false);
    const [investModalOpen, setInvestModalOpen] = useState(false);
    const isOwner = user?.id === startup?.founderId;
    const { data: interestedInvestors } = useInterestedInvestors(id, !!isOwner);
    const { data: aiSummary } = useReportSummary(id, !isOwner);
    const acceptInterestMutation = useAcceptInterest(id ?? '');
    const rejectInterestMutation = useRejectInterest(id ?? '');
    // Notifications deep-link here with a #hash (e.g. #interested-investors, #post-<id>) —
    // scroll to and briefly highlight that section once its data has actually rendered.
    useEffect(() => {
        if (!location.hash)
            return;
        const target = document.getElementById(location.hash.slice(1));
        if (!target)
            return;
        target.scrollIntoView({ behavior: 'smooth', block: 'center' });
        target.classList.add('ring-2', 'ring-brand-500', 'ring-offset-2', 'dark:ring-offset-slate-950');
        const timeout = setTimeout(() => {
            target.classList.remove('ring-2', 'ring-brand-500', 'ring-offset-2', 'dark:ring-offset-slate-950');
        }, 2500);
        return () => clearTimeout(timeout);
    }, [location.hash, interestedInvestors, posts]);
    if (isLoading) {
        return (<DashboardLayout>
        <Spinner />
      </DashboardLayout>);
    }
    if (isError || !startup) {
        return (<DashboardLayout>
        <EmptyState icon={TriangleAlert} title="This startup isn't available" description="It may have been unpublished or removed by its founder since you saved or followed it." action={<Button size="sm" variant="secondary" onClick={() => navigate(-1)}>Go back</Button>}/>
      </DashboardLayout>);
    }
    const isFollowing = !!following?.some((f) => f.id === startup.id);
    const isInterested = !!myInterests?.some((s) => s.id === startup.id);
    const isSaved = !!savedStartups?.some((s) => s.id === startup.id);
    const toggleFollow = () => {
        if (!id)
            return;
        followMutation.mutate({ id, follow: !isFollowing }, {
            onSuccess: () => toast.success(isFollowing ? 'Unfollowed' : 'Now following'),
            onError: () => toast.error('Something went wrong'),
        });
    };
    const toggleInterest = () => {
        if (!id)
            return;
        expressInterestMutation.mutate({ id, express: !isInterested }, {
            onSuccess: () => toast.success(isInterested ? 'Interest withdrawn' : 'Interest expressed — the founder has been notified'),
            onError: () => toast.error('Something went wrong'),
        });
    };
    const toggleSave = () => {
        if (!id)
            return;
        saveStartupMutation.mutate({ id, saved: isSaved }, {
            onSuccess: () => toast.success(isSaved ? 'Removed from saved startups' : 'Saved to your list'),
            onError: () => toast.error('Something went wrong'),
        });
    };
    const progressPct = startup.fundingGoal && startup.fundingGoal > 0
        ? Math.min(100, Math.round(((startup.fundingProgress ?? 0) / startup.fundingGoal) * 100))
        : 0;
    return (<DashboardLayout>
      <Card className="mb-6 space-y-4 p-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="flex size-14 items-center justify-center overflow-hidden rounded-2xl bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-300">
              {startup.logoUrl ? (<img src={startup.logoUrl} alt="" className="size-full object-cover"/>) : (<Rocket className="size-6"/>)}
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">{startup.name}</h1>
                {startup.websiteUrl && (<a href={startup.websiteUrl} target="_blank" rel="noreferrer" aria-label="Website" className="text-slate-400 hover:text-brand-600 dark:hover:text-brand-400">
                    <Globe className="size-4"/>
                  </a>)}
              </div>
              <p className="text-sm text-slate-500 dark:text-slate-400">
                by {startup.founderName} · {startup.industry ?? 'Uncategorized'}
              </p>
              <div className="mt-1.5 flex flex-wrap gap-1.5">
                {startup.stage && <Badge tone="brand">{startup.stage.replace('_', ' ')}</Badge>}
                {startup.verified && <Badge tone="green">Verified</Badge>}
                {startup.equityOffered != null && <Badge tone="slate">{startup.equityOffered}% equity offered</Badge>}
                {startup.interestedInvestorsCount > 0 && (<Badge tone="amber">
                    <Star className="size-3"/>
                    {startup.interestedInvestorsCount} interested
                  </Badge>)}
              </div>
              {startup.socialLinks && (<p className="mt-1 text-xs text-slate-400">{startup.socialLinks}</p>)}
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            {isOwner && (<Link to={`/founder/startups/${startup.id}/ai-report`}>
                <Button variant="secondary" size="sm">
                  <Sparkles className="size-4"/>
                  AI Report
                </Button>
              </Link>)}
            {!isOwner && user && (<Button variant="secondary" size="sm" onClick={() => navigate(`/chat/${startup.founderId}`, { state: { name: startup.founderName } })}>
                <MessageCircle className="size-4"/>
                Message
              </Button>)}
            {!isOwner && user?.role === 'INVESTOR' && (<Button variant="secondary" size="sm" onClick={() => setScheduleOpen(true)}>
                <Calendar className="size-4"/>
                Schedule Meeting
              </Button>)}
            {(isOwner || user?.role === 'INVESTOR') && (<Link to={`/startups/${startup.id}/due-diligence`}>
                <Button variant="secondary" size="sm">
                  <FolderLock className="size-4"/>
                  Due Diligence
                </Button>
              </Link>)}
            {user?.role === 'INVESTOR' && (<Button variant={isFollowing ? 'secondary' : 'primary'} size="sm" onClick={toggleFollow} loading={followMutation.isPending}>
                <Heart className={isFollowing ? 'size-4 fill-current' : 'size-4'}/>
                {isFollowing ? 'Following' : 'Follow'}
              </Button>)}
            {user?.role === 'INVESTOR' && (<Button variant={isInterested ? 'secondary' : 'primary'} size="sm" onClick={toggleInterest} loading={expressInterestMutation.isPending}>
                <Star className={isInterested ? 'size-4 fill-current' : 'size-4'}/>
                {isInterested ? 'Interested' : 'Express Interest'}
              </Button>)}
            {user?.role === 'INVESTOR' && (<Button variant="ghost" size="sm" onClick={toggleSave} loading={saveStartupMutation.isPending}>
                <Bookmark className={isSaved ? 'size-4 fill-brand-600 text-brand-600 dark:fill-brand-400 dark:text-brand-400' : 'size-4'}/>
                {isSaved ? 'Saved' : 'Save'}
              </Button>)}
          </div>
        </div>

        <div className="space-y-1.5">
          <div className="flex items-center justify-between text-sm text-slate-600 dark:text-slate-400">
            <span>{formatCurrency(startup.fundingProgress)} raised</span>
            <span>{formatCurrency(startup.fundingGoal)} goal</span>
          </div>
          <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
            <div className="gradient-brand h-full rounded-full" style={{ width: `${progressPct}%` }}/>
          </div>
        </div>
      </Card>

      {!isOwner && aiSummary && (<Card className="mb-6 flex flex-wrap items-center gap-x-6 gap-y-2 p-4">
          <div className="flex items-center gap-2 text-sm font-semibold text-slate-800 dark:text-slate-200">
            <ShieldCheck className="size-4 text-brand-500"/>
            AI Verified Insights
          </div>
          <span className="text-sm text-slate-600 dark:text-slate-400">
            Investment Readiness: <span className="font-medium text-slate-800 dark:text-slate-200">{aiSummary.overallScore.toFixed(0)}/100</span>
          </span>
          {aiSummary.investorReadinessStatus && <Badge tone="brand">{aiSummary.investorReadinessStatus}</Badge>}
          <span className="text-xs text-slate-400">
            Full report is a private founder tool — this is a signal, not the final word.
          </span>
        </Card>)}

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          {fields.map(({ key, label }) => startup[key] && (<Card key={key} className="p-5">
                  <h3 className="mb-1.5 text-sm font-semibold text-slate-900 dark:text-slate-100">{label}</h3>
                  <p className="whitespace-pre-wrap text-sm text-slate-600 dark:text-slate-400">
                    {String(startup[key])}
                  </p>
                </Card>))}
          <TeamCard startupId={startup.id} isOwner={isOwner}/>
          <MilestonesCard startupId={startup.id} isOwner={isOwner}/>
        </div>

        <div className="space-y-4">
          {isOwner && (<Card id="interested-investors" className="p-5 transition-shadow">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="flex items-center gap-1.5 font-semibold text-slate-900 dark:text-slate-100">
                  <Star className="size-4 text-amber-500"/>
                  Interested Investors
                </h3>
                {interestedInvestors?.some((i) => i.status === 'ACCEPTED') && (<Button size="sm" variant="secondary" onClick={() => setInvestModalOpen(true)}>
                    <DollarSign className="size-4"/>
                    Record Investment
                  </Button>)}
              </div>
              {!interestedInvestors || interestedInvestors.length === 0 ? (<p className="text-sm text-slate-400">No investors have expressed interest yet.</p>) : (<div className="space-y-2">
                  {interestedInvestors.map((i) => (<div key={i.investorId} className="rounded-xl border border-slate-100 p-3 text-sm dark:border-slate-800">
                      <div className="flex flex-wrap items-center gap-1.5">
                        <span className="font-medium text-slate-800 dark:text-slate-200">{i.investorName}</span>
                        {i.firmName && <span className="text-slate-400">· {i.firmName}</span>}
                        {i.verified && (<Badge tone="green">
                            <ShieldCheck className="size-3"/>
                            Verified
                          </Badge>)}
                        {i.aiMatchPercent != null && <Badge tone="brand">{i.aiMatchPercent.toFixed(0)}% match</Badge>}
                      </div>
                      {i.investmentInterests && (<p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{i.investmentInterests}</p>)}
                      <p className="mt-1 text-xs text-slate-400">{i.pastInvestmentsCount} past investment{i.pastInvestmentsCount === 1 ? '' : 's'}</p>

                      <div className="mt-2 flex items-center gap-2">
                        {i.status === 'PENDING' && (<>
                            <Button size="sm" variant="primary" loading={acceptInterestMutation.isPending} onClick={() => acceptInterestMutation.mutate(i.investorId, {
                            onSuccess: () => toast.success('Accepted — chat is now open'),
                            onError: () => toast.error('Something went wrong'),
                        })}>
                              Accept
                            </Button>
                            <Button size="sm" variant="ghost" loading={rejectInterestMutation.isPending} onClick={() => rejectInterestMutation.mutate(i.investorId, {
                            onSuccess: () => toast.success('Declined'),
                            onError: () => toast.error('Something went wrong'),
                        })}>
                              Decline
                            </Button>
                          </>)}
                        {i.status === 'ACCEPTED' && (<>
                            <Badge tone="green">Accepted</Badge>
                            <button onClick={() => navigate(`/chat/${i.investorId}`, { state: { name: i.investorName } })} className="text-xs text-brand-600 hover:underline dark:text-brand-400">
                              Message
                            </button>
                          </>)}
                        {i.status === 'REJECTED' && <Badge tone="slate">Declined</Badge>}
                      </div>
                    </div>))}
                </div>)}
            </Card>)}

          <h3 className="font-semibold text-slate-900 dark:text-slate-100">Updates from {startup.name}</h3>
          {!posts || posts.length === 0 ? (<p className="text-sm text-slate-500 dark:text-slate-400">No updates yet.</p>) : (posts.map((p) => (<PostCard key={p.id} id={`post-${p.id}`} post={p} defaultOpenComments={location.hash === `#post-${p.id}`}/>)))}
        </div>
      </div>

      <ScheduleMeetingModal open={scheduleOpen} onClose={() => setScheduleOpen(false)} recipientId={startup.founderId} startupId={startup.id}/>
      {isOwner && (<RecordInvestmentModal open={investModalOpen} onClose={() => setInvestModalOpen(false)} startupId={startup.id} interestedInvestors={(interestedInvestors ?? []).filter((i) => i.status === 'ACCEPTED')}/>)}
    </DashboardLayout>);
}
