import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ShieldCheck, Star, Users } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Spinner } from '../../components/ui/Spinner';
import { EmptyState } from '../../components/ui/EmptyState';
import { useAcceptInterestAny, useFounderInterestedInvestors, useRejectInterestAny } from '../../hooks/useInterest';

export function InvestorsPage() {
  const { data: investors, isLoading } = useFounderInterestedInvestors();
  const acceptMutation = useAcceptInterestAny();
  const rejectMutation = useRejectInterestAny();

  return (
    <DashboardLayout>
      <div className="mb-6 flex items-center gap-2">
        <Users className="size-5 text-brand-500" />
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Investors</h1>
      </div>
      <p className="mb-6 text-sm text-slate-500 dark:text-slate-400">
        Everyone who has expressed interest across all of your startups, in one place.
      </p>

      {isLoading ? (
        <Spinner />
      ) : !investors || investors.length === 0 ? (
        <EmptyState
          icon={Star}
          title="No investor interest yet"
          description="When an investor expresses interest in one of your startups, they'll show up here."
        />
      ) : (
        <div className="space-y-3">
          {investors.map((i) => (
            <Card key={`${i.startupId}-${i.investorId}`} className="p-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <div className="flex flex-wrap items-center gap-1.5">
                    <span className="font-medium text-slate-800 dark:text-slate-200">{i.investorName}</span>
                    {i.firmName && <span className="text-slate-400">· {i.firmName}</span>}
                    {i.verified && (
                      <Badge tone="green">
                        <ShieldCheck className="size-3" />
                        Verified
                      </Badge>
                    )}
                    {i.aiMatchPercent != null && <Badge tone="brand">{i.aiMatchPercent.toFixed(0)}% match</Badge>}
                  </div>
                  <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                    Interested in{' '}
                    <Link to={`/startups/${i.startupId}#interested-investors`} className="font-medium text-brand-600 hover:underline dark:text-brand-400">
                      {i.startupName}
                    </Link>
                  </p>
                  {i.investmentInterests && (
                    <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{i.investmentInterests}</p>
                  )}
                  <p className="mt-1 text-xs text-slate-400">
                    {i.pastInvestmentsCount} past investment{i.pastInvestmentsCount === 1 ? '' : 's'}
                  </p>
                </div>

                <div className="flex shrink-0 items-center gap-2">
                  {i.status === 'PENDING' && (
                    <>
                      <Button
                        size="sm"
                        variant="primary"
                        loading={acceptMutation.isPending}
                        onClick={() =>
                          acceptMutation.mutate(
                            { startupId: i.startupId, investorId: i.investorId },
                            {
                              onSuccess: () => toast.success('Accepted — chat is now open'),
                              onError: () => toast.error('Something went wrong'),
                            },
                          )
                        }
                      >
                        Accept
                      </Button>
                      <Button
                        size="sm"
                        variant="ghost"
                        loading={rejectMutation.isPending}
                        onClick={() =>
                          rejectMutation.mutate(
                            { startupId: i.startupId, investorId: i.investorId },
                            {
                              onSuccess: () => toast.success('Declined'),
                              onError: () => toast.error('Something went wrong'),
                            },
                          )
                        }
                      >
                        Decline
                      </Button>
                    </>
                  )}
                  {i.status === 'ACCEPTED' && (
                    <>
                      <Badge tone="green">Accepted</Badge>
                      <Link to={`/chat/${i.investorId}`} state={{ name: i.investorName }} className="text-xs text-brand-600 hover:underline dark:text-brand-400">
                        Message
                      </Link>
                    </>
                  )}
                  {i.status === 'REJECTED' && <Badge tone="slate">Declined</Badge>}
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </DashboardLayout>
  );
}
