import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { FileSearch, MessageSquareText, Sparkles, TrendingUp, Users, Wand2 } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Spinner } from '../../components/ui/Spinner';
import { useStartup } from '../../hooks/useStartups';
import { useAskMentor, useMarketResearch, usePitchImprovement, usePitchReview, useStartupMatches } from '../../hooks/useAiTools';
import { cn } from '../../lib/cn';
const tabs = [
    { id: 'pitch', label: 'Pitch Review', icon: FileSearch },
    { id: 'mentor', label: 'AI Mentor', icon: MessageSquareText },
    { id: 'research', label: 'Market Research', icon: TrendingUp },
    { id: 'matches', label: 'Investor Matches', icon: Users },
];
const statusTone = { Strong: 'green', Okay: 'amber', Weak: 'red' };
export function AiToolsPage() {
    const { id: startupId } = useParams();
    const { data: startup } = useStartup(startupId);
    const [tab, setTab] = useState('pitch');
    const pitchReview = usePitchReview(startupId ?? '');
    const pitchImprovement = usePitchImprovement(startupId ?? '');
    const askMentor = useAskMentor(startupId ?? '');
    const marketResearch = useMarketResearch(startupId ?? '');
    const { data: matches, isLoading: loadingMatches } = useStartupMatches(startupId ?? '');
    const [question, setQuestion] = useState('');
    const [researchQuery, setResearchQuery] = useState(startup?.industry ?? '');
    // Restore the question/query that produced the persisted result, so a returning visitor
    // sees the answer in context instead of a blank input above a leftover answer.
    useEffect(() => {
        if (askMentor.cachedInput) setQuestion(askMentor.cachedInput);
    }, [askMentor.cachedInput]);
    useEffect(() => {
        if (marketResearch.cachedInput) setResearchQuery(marketResearch.cachedInput);
    }, [marketResearch.cachedInput]);
    const handleAsk = (e) => {
        e.preventDefault();
        if (!question.trim())
            return;
        askMentor.mutate(question.trim());
    };
    const handleResearch = (e) => {
        e.preventDefault();
        if (!researchQuery.trim())
            return;
        marketResearch.mutate(researchQuery.trim());
    };
    return (<DashboardLayout>
      <div className="mb-6 flex items-center gap-2">
        <Sparkles className="size-5 text-brand-500"/>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">AI Tools — {startup?.name}</h1>
      </div>

      <div className="mb-6 flex flex-wrap gap-2">
        {tabs.map((t) => (<button key={t.id} onClick={() => setTab(t.id)} className={cn('flex items-center gap-1.5 rounded-xl border px-3 py-2 text-sm font-medium transition-colors', tab === t.id
                ? 'border-brand-500 bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
                : 'border-slate-200 text-slate-500 dark:border-slate-700')}>
            <t.icon className="size-4"/>
            {t.label}
          </button>))}
      </div>

      {tab === 'pitch' && (<Card className="space-y-4 p-5">
          <div className="flex items-center justify-between">
            <p className="text-sm text-slate-500 dark:text-slate-400">
              Reviews your startup profile's narrative fields slide-by-slide (no PDF upload yet).
            </p>
            <Button size="sm" onClick={() => pitchReview.mutate()} loading={pitchReview.isPending}>
              Review my pitch
            </Button>
          </div>
          {pitchReview.data && (<div className="space-y-3">
              <p className="text-sm font-medium text-slate-800 dark:text-slate-200">{pitchReview.data.overallImpression}</p>
              {pitchReview.data.slides.map((s) => (<div key={s.slide} className="rounded-xl border border-slate-100 p-3 dark:border-slate-800">
                  <div className="mb-1 flex items-center gap-2">
                    <span className="text-sm font-semibold text-slate-800 dark:text-slate-200">{s.slide}</span>
                    <Badge tone={statusTone[s.status]}>{s.status}</Badge>
                  </div>
                  <p className="text-sm text-slate-600 dark:text-slate-400">{s.feedback}</p>
                  {s.suggestions.length > 0 && (<ul className="mt-1 list-disc pl-5 text-xs text-slate-500 dark:text-slate-400">
                      {s.suggestions.map((sg, i) => (<li key={i}>{sg}</li>))}
                    </ul>)}
                </div>))}
            </div>)}
        </Card>)}

      {tab === 'pitch' && (<Card className="mt-4 space-y-4 p-5">
          <div className="flex items-center justify-between">
            <div>
              <p className="flex items-center gap-1.5 text-sm font-semibold text-slate-800 dark:text-slate-200">
                <Wand2 className="size-4 text-brand-500"/>
                AI Pitch Improvement
              </p>
              <p className="text-sm text-slate-500 dark:text-slate-400">
                Rewrites your problem, solution, and business model into investor-friendly language.
              </p>
            </div>
            <Button size="sm" onClick={() => pitchImprovement.mutate()} loading={pitchImprovement.isPending}>
              Improve my pitch
            </Button>
          </div>
          {pitchImprovement.data && (<div className="space-y-3">
              {[
                    { label: 'Problem statement', text: pitchImprovement.data.improvedProblemStatement },
                    { label: 'Solution', text: pitchImprovement.data.improvedSolution },
                    { label: 'Business model', text: pitchImprovement.data.improvedBusinessModel },
                ].map((f) => (<div key={f.label} className="rounded-xl border border-slate-100 p-3 dark:border-slate-800">
                  <p className="mb-1 text-sm font-semibold text-slate-800 dark:text-slate-200">{f.label}</p>
                  <p className="whitespace-pre-wrap text-sm text-slate-600 dark:text-slate-400">{f.text}</p>
                </div>))}
              {pitchImprovement.data.languageTips.length > 0 && (<div className="rounded-xl bg-slate-50 p-3 dark:bg-slate-800/60">
                  <p className="mb-1 text-sm font-semibold text-slate-800 dark:text-slate-200">Language tips</p>
                  <ul className="list-disc space-y-1 pl-5 text-xs text-slate-500 dark:text-slate-400">
                    {pitchImprovement.data.languageTips.map((t, i) => (<li key={i}>{t}</li>))}
                  </ul>
                </div>)}
            </div>)}
        </Card>)}

      {tab === 'mentor' && (<Card className="space-y-4 p-5">
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Ask a specific question about funding, hiring, valuation, or scaling — answers use your startup's own data.
          </p>
          <form onSubmit={handleAsk} className="flex gap-2">
            <input value={question} onChange={(e) => setQuestion(e.target.value)} placeholder="e.g. Is ₹2 Crore too much for seed funding?" className="flex-1 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 dark:border-slate-700 dark:bg-slate-900"/>
            <Button type="submit" loading={askMentor.isPending}>
              Ask
            </Button>
          </form>
          {askMentor.data && (<div className="rounded-xl bg-slate-50 p-4 dark:bg-slate-800/60">
              <p className="text-sm text-slate-700 dark:text-slate-300">{askMentor.data.answer}</p>
              <p className="mt-2 text-xs text-slate-400">Based on: {askMentor.data.basedOn}</p>
            </div>)}
        </Card>)}

      {tab === 'research' && (<Card className="space-y-4 p-5">
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Type an industry or description to see competitors and market signals from a curated lookup (not live data).
          </p>
          <form onSubmit={handleResearch} className="flex gap-2">
            <input value={researchQuery} onChange={(e) => setResearchQuery(e.target.value)} placeholder="e.g. Organic grocery marketplace" className="flex-1 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 dark:border-slate-700 dark:bg-slate-900"/>
            <Button type="submit" loading={marketResearch.isPending}>
              Research
            </Button>
          </form>
          {marketResearch.data && (<div className="space-y-2 text-sm">
              <p className="text-slate-700 dark:text-slate-300">
                <span className="font-medium">Top competitors:</span>{' '}
                {marketResearch.data.topCompetitors.length ? marketResearch.data.topCompetitors.join(', ') : '—'}
              </p>
              <p className="text-slate-700 dark:text-slate-300">
                <span className="font-medium">Estimated growth:</span> {marketResearch.data.estimatedGrowth}
              </p>
              <p className="text-slate-700 dark:text-slate-300">
                <span className="font-medium">Major challenges:</span> {marketResearch.data.majorChallenges.join(', ')}
              </p>
              <p className="text-xs text-slate-400">{marketResearch.data.note}</p>
            </div>)}
        </Card>)}

      {tab === 'matches' && (<Card className="space-y-3 p-5">
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Explainable investor matching — weighted industry/stage/keyword overlap, not a black-box score.
          </p>
          {loadingMatches ? (<Spinner />) : !matches || matches.length === 0 ? (<p className="text-sm text-slate-400">No investors registered yet.</p>) : (matches.map((m) => (<div key={m.id} className="rounded-xl border border-slate-100 p-3 dark:border-slate-800">
                <div className="flex items-center justify-between">
                  <span className="font-medium text-slate-800 dark:text-slate-200">
                    {m.name} · {m.subtitle}
                  </span>
                  <Badge tone="brand">{m.matchPercent.toFixed(0)}% match</Badge>
                </div>
                <ul className="mt-1 list-disc pl-5 text-xs text-slate-500 dark:text-slate-400">
                  {m.reasons.map((r, i) => (<li key={i}>{r}</li>))}
                </ul>
              </div>)))}
        </Card>)}
    </DashboardLayout>);
}
