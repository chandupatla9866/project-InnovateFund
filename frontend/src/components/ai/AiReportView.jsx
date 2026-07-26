import { motion } from 'framer-motion';
import { CheckCircle2, Lightbulb, ShieldCheck, Sparkles, Wand2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { usePitchImprovement } from '../../hooks/useAiTools';
function scoreTone(pct) {
    if (pct >= 75)
        return 'bg-emerald-500';
    if (pct >= 50)
        return 'bg-amber-500';
    return 'bg-red-500';
}
function readinessTone(status) {
    if (status === 'Ready for Seed Investors')
        return 'green';
    if (status === 'Needs Improvement Before Pitching')
        return 'amber';
    return 'red';
}
const FIXABLE_FIELDS = {
    PROBLEM_CLARITY: 'improvedProblemStatement',
    SOLUTION_QUALITY: 'improvedSolution',
    BUSINESS_MODEL: 'improvedBusinessModel',
};
export function AiReportView({ report, startupId }) {
    const pitchImprovement = usePitchImprovement(startupId);
    const handleFix = () => {
        pitchImprovement.mutate(undefined, {
            onError: () => toast.error('Could not generate a fix right now'),
        });
    };
    return (<div className="space-y-6">
      <Card className="gradient-brand flex flex-col items-center gap-2 p-8 text-center text-white">
        <span className="text-sm font-medium uppercase tracking-wide opacity-90">Overall Investment Readiness</span>
        <span className="text-5xl font-bold">{report.overallScore.toFixed(1)}</span>
        <span className="text-sm opacity-90">out of 100</span>
      </Card>

      {report.investorReadinessStatus && (<Card className="flex items-center justify-between p-5">
          <div className="flex items-center gap-2">
            <ShieldCheck className="size-4.5 text-brand-500"/>
            <div>
              <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">Investor Readiness</p>
              <p className="text-xs text-slate-500 dark:text-slate-400">{report.investorReadinessStatus}</p>
            </div>
          </div>
          <div className="text-right">
            <Badge tone={readinessTone(report.investorReadinessStatus)}>{report.investorReadinessStatus}</Badge>
            {report.investorReadinessConfidence && (<p className="mt-1 text-xs text-slate-400">Confidence: {report.investorReadinessConfidence}</p>)}
          </div>
        </Card>)}

      <Card className="space-y-1 p-5">
        <div className="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300">
          <Sparkles className="size-4 text-brand-500"/>
          Assessment summary
        </div>
        <p className="text-sm text-slate-600 dark:text-slate-400">{report.summaryText}</p>
        <p className="text-xs text-slate-400">Model: {report.modelVersion}</p>
      </Card>

      {report.strengths.length > 0 && (<Card className="space-y-2 p-5">
          <div className="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300">
            <CheckCircle2 className="size-4 text-emerald-500"/>
            Strengths
          </div>
          <ul className="space-y-1.5 text-sm text-slate-600 dark:text-slate-400">
            {report.strengths.map((s, i) => (<li key={i} className="flex gap-2">
                <span className="text-emerald-500">✓</span>
                {s}
              </li>))}
          </ul>
        </Card>)}

      <div className="grid gap-3 sm:grid-cols-2">
        {report.categoryScores.map((cs, i) => {
            const pct = (cs.rawScore / 20) * 100;
            const fixField = FIXABLE_FIELDS[cs.category];
            const fixedText = fixField ? pitchImprovement.data?.[fixField] : undefined;
            return (<motion.div key={cs.category} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.04 }}>
              <Card className="space-y-2 p-4">
                <div className="flex items-center justify-between text-sm">
                  <span className="font-medium text-slate-800 dark:text-slate-200">{cs.displayName}</span>
                  <span className="text-slate-500 dark:text-slate-400">
                    {cs.rawScore.toFixed(1)}/20 · {(cs.weight * 100).toFixed(0)}% weight
                  </span>
                </div>
                <div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
                  <div className={`h-full rounded-full ${scoreTone(pct)}`} style={{ width: `${pct}%` }}/>
                </div>
                <p className="text-xs text-slate-500 dark:text-slate-400">{cs.reasoning}</p>
                {fixField && cs.rawScore < 16 && (<div className="pt-1">
                    {fixedText ? (<div className="rounded-lg bg-brand-50 p-2.5 text-xs text-slate-700 dark:bg-brand-500/10 dark:text-slate-300">
                        {fixedText}
                      </div>) : (<Button size="sm" variant="ghost" loading={pitchImprovement.isPending} onClick={handleFix}>
                        <Wand2 className="size-3.5"/>
                        Fix with AI
                      </Button>)}
                  </div>)}
              </Card>
            </motion.div>);
        })}
      </div>

      <Card className="space-y-2 p-5">
        <div className="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300">
          <Lightbulb className="size-4 text-amber-500"/>
          Suggestions to improve
        </div>
        <ul className="list-disc space-y-1.5 pl-5 text-sm text-slate-600 dark:text-slate-400">
          {report.suggestions.map((s, i) => (<li key={i}>{s}</li>))}
        </ul>
      </Card>
    </div>);
}
