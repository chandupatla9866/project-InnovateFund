import { useState } from 'react';
import { useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { History, Sparkles } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { AiReportView } from '../../components/ai/AiReportView';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { EmptyState } from '../../components/ui/EmptyState';
import { Spinner } from '../../components/ui/Spinner';
import { useAnalyzeStartup, useReportHistory } from '../../hooks/useAiReport';
import { useStartup } from '../../hooks/useStartups';
import { cn } from '../../lib/cn';
export function AiReportPage() {
    const { id } = useParams();
    const { data: startup } = useStartup(id);
    const { data: reports, isLoading } = useReportHistory(id);
    const analyze = useAnalyzeStartup(id ?? '');
    const [selectedId, setSelectedId] = useState(null);
    const sorted = reports ? [...reports].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()) : [];
    const latest = reports?.[0];
    const selected = (selectedId ? sorted.find((r) => r.id === selectedId) : null) ?? latest;
    const handleAnalyze = () => {
        analyze.mutate(undefined, {
            onSuccess: () => {
                toast.success('AI analysis complete');
                setSelectedId(null);
            },
            onError: () => toast.error('Could not run analysis'),
        });
    };
    return (<DashboardLayout>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">AI Startup Evaluation</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">{startup?.name}</p>
        </div>
        <Button onClick={handleAnalyze} loading={analyze.isPending}>
          <Sparkles className="size-4"/>
          {latest ? 'Re-analyze' : 'Analyze'}
        </Button>
      </div>

      {isLoading ? (<Spinner />) : !latest || !selected ? (<EmptyState icon={Sparkles} title="No analysis yet" description="Run the AI evaluation to get a structured, rubric-based assessment of your startup with actionable suggestions." action={<Button size="sm" onClick={handleAnalyze} loading={analyze.isPending}>
              Analyze now
            </Button>}/>) : (<div className="grid gap-6 lg:grid-cols-4">
          {sorted.length > 1 && (<Card className="h-fit space-y-1 p-3 lg:order-2">
              <div className="mb-2 flex items-center gap-1.5 px-2 text-xs font-semibold uppercase tracking-wide text-slate-400">
                <History className="size-3.5"/>
                Report History
              </div>
              {sorted.map((r, i) => (<button key={r.id} onClick={() => setSelectedId(r.id)} className={cn('flex w-full items-center justify-between rounded-lg px-2.5 py-2 text-left text-sm transition-colors', r.id === selected.id
                        ? 'bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
                        : 'text-slate-600 hover:bg-slate-50 dark:text-slate-400 dark:hover:bg-slate-800')}>
                  <span>Version {i + 1}</span>
                  <span className="font-medium">{r.overallScore.toFixed(0)}</span>
                </button>))}
            </Card>)}
          <div className={sorted.length > 1 ? 'lg:order-1 lg:col-span-3' : 'lg:col-span-4'}>
            <AiReportView report={selected} startupId={id ?? ''}/>
          </div>
        </div>)}
    </DashboardLayout>);
}
