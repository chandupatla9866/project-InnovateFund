import toast from 'react-hot-toast';
import { AlertTriangle, BadgeCheck, Building2, Flag, ShieldAlert, ShieldCheck, Users } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { EmptyState } from '../../components/ui/EmptyState';
import { Skeleton } from '../../components/ui/Skeleton';
import { usePendingFounders, usePendingInvestors, usePendingStartups, useVerifyFounder, useVerifyInvestor, useVerifyStartup, } from '../../hooks/useAdmin';
import { useFraudFlags } from '../../hooks/useAiTools';
import { useDismissReport, usePendingReports, useResolveReport } from '../../hooks/useReports';
function Section({ title, icon: Icon, items, isLoading, renderLabel, onVerify, verifying, }) {
    return (<Card className="p-5">
      <div className="mb-4 flex items-center gap-2">
        <Icon className="size-4.5 text-brand-500"/>
        <h2 className="font-semibold text-slate-900 dark:text-slate-100">{title}</h2>
      </div>
      {isLoading ? (<div className="space-y-2">
          <Skeleton className="h-12"/>
          <Skeleton className="h-12"/>
        </div>) : !items || items.length === 0 ? (<EmptyState icon={BadgeCheck} title="All caught up" description="Nothing pending verification."/>) : (<div className="space-y-2">
          {items.map((item) => (<div key={item.id} className="flex items-center justify-between rounded-xl border border-slate-100 px-3 py-2.5 dark:border-slate-800">
              <span className="text-sm font-medium text-slate-700 dark:text-slate-300">{renderLabel(item)}</span>
              <Button size="sm" variant="secondary" loading={verifying} onClick={() => onVerify(item.id)}>
                Verify
              </Button>
            </div>))}
        </div>)}
    </Card>);
}
export function AdminDashboardPage() {
    const { data: founders, isLoading: loadingFounders } = usePendingFounders();
    const { data: investors, isLoading: loadingInvestors } = usePendingInvestors();
    const { data: startups, isLoading: loadingStartups } = usePendingStartups();
    const verifyFounder = useVerifyFounder();
    const verifyInvestor = useVerifyInvestor();
    const verifyStartup = useVerifyStartup();
    const { data: fraudFlags, isLoading: loadingFraud } = useFraudFlags();
    const { data: reports, isLoading: loadingReports } = usePendingReports();
    const resolveReport = useResolveReport();
    const dismissReport = useDismissReport();
    return (<DashboardLayout>
      <div className="mb-6">
        <h1 className="flex items-center gap-2 text-2xl font-bold text-slate-900 dark:text-slate-100">
          <ShieldCheck className="size-6 text-brand-500"/>
          Verification Queue
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Admins verify founders, investors, and startups — they don't decide investments.
        </p>
      </div>

      <Card className="mb-6 p-5">
        <div className="mb-4 flex items-center gap-2">
          <ShieldAlert className="size-4.5 text-amber-500"/>
          <h2 className="font-semibold text-slate-900 dark:text-slate-100">Fraud &amp; Spam Flags</h2>
          <span className="text-xs text-slate-400">(rule-based, not a trained model)</span>
        </div>
        {loadingFraud ? (<Skeleton className="h-16"/>) : !fraudFlags || fraudFlags.length === 0 ? (<EmptyState icon={BadgeCheck} title="No flags" description="Nothing suspicious detected right now."/>) : (<div className="space-y-2">
            {fraudFlags.map((f) => (<div key={f.startupId} className="rounded-xl border border-slate-100 p-3 dark:border-slate-800">
                <div className="flex items-center gap-2">
                  <AlertTriangle className="size-4 text-amber-500"/>
                  <span className="text-sm font-medium text-slate-800 dark:text-slate-200">
                    {f.startupName} — {f.founderName}
                  </span>
                  <Badge tone={f.severity === 'HIGH' ? 'red' : 'amber'}>{f.severity}</Badge>
                </div>
                <ul className="mt-1 list-disc pl-9 text-xs text-slate-500 dark:text-slate-400">
                  {f.reasons.map((r, i) => (<li key={i}>{r}</li>))}
                </ul>
              </div>))}
          </div>)}
      </Card>

      <Card className="mb-6 p-5">
        <div className="mb-4 flex items-center gap-2">
          <Flag className="size-4.5 text-red-500"/>
          <h2 className="font-semibold text-slate-900 dark:text-slate-100">User Reports</h2>
        </div>
        {loadingReports ? (<Skeleton className="h-16"/>) : !reports || reports.length === 0 ? (<EmptyState icon={BadgeCheck} title="No pending reports" description="Nothing reported by users right now."/>) : (<div className="space-y-2">
            {reports.map((r) => (<div key={r.id} className="flex items-center justify-between rounded-xl border border-slate-100 p-3 dark:border-slate-800">
                <div>
                  <div className="flex items-center gap-2">
                    <Badge tone="slate">{r.targetType}</Badge>
                    <span className="text-xs text-slate-400">reported by {r.reporterName}</span>
                  </div>
                  <p className="mt-1 text-sm text-slate-700 dark:text-slate-300">{r.reason}</p>
                </div>
                <div className="flex shrink-0 gap-2">
                  <Button size="sm" variant="ghost" onClick={() => dismissReport.mutate(r.id)}>
                    Dismiss
                  </Button>
                  <Button size="sm" variant="secondary" onClick={() => resolveReport.mutate(r.id)}>
                    Resolve
                  </Button>
                </div>
              </div>))}
          </div>)}
      </Card>

      <div className="grid gap-4 lg:grid-cols-3">
        <Section title="Founders" icon={Users} items={founders} isLoading={loadingFounders} renderLabel={(f) => `${f.fullName} · ${f.email}`} verifying={verifyFounder.isPending} onVerify={(id) => verifyFounder.mutate(id, {
            onSuccess: () => toast.success('Founder verified'),
            onError: () => toast.error('Could not verify founder'),
        })}/>
        <Section title="Investors" icon={Users} items={investors} isLoading={loadingInvestors} renderLabel={(i) => `${i.fullName} · ${i.email}`} verifying={verifyInvestor.isPending} onVerify={(id) => verifyInvestor.mutate(id, {
            onSuccess: () => toast.success('Investor verified'),
            onError: () => toast.error('Could not verify investor'),
        })}/>
        <Section title="Startups" icon={Building2} items={startups} isLoading={loadingStartups} renderLabel={(s) => s.name} verifying={verifyStartup.isPending} onVerify={(id) => verifyStartup.mutate(id, {
            onSuccess: () => toast.success('Startup verified'),
            onError: () => toast.error('Could not verify startup'),
        })}/>
      </div>
    </DashboardLayout>);
}
