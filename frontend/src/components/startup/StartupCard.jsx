import { motion } from 'framer-motion';
import { Link, useNavigate } from 'react-router-dom';
import { BadgeCheck, ExternalLink, Rocket, Star, TrendingUp } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
const stageLabels = {
    IDEA: 'Idea',
    MVP: 'MVP',
    EARLY_TRACTION: 'Early Traction',
    GROWTH: 'Growth',
    SCALING: 'Scaling',
};
function formatCurrency(value) {
    if (value == null)
        return '—';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);
}
export function StartupCard({ startup, to, viewTo }) {
    const navigate = useNavigate();
    const progressPct = startup.fundingGoal && startup.fundingGoal > 0
        ? Math.min(100, Math.round(((startup.fundingProgress ?? 0) / startup.fundingGoal) * 100))
        : 0;
    return (<motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} whileHover={{ y: -3 }} transition={{ duration: 0.25 }}>
      <Card onClick={() => navigate(to)} className="flex h-full cursor-pointer flex-col gap-3 p-5 transition-shadow hover:shadow-md">
          <div className="flex items-center gap-3">
            <div className="flex size-11 shrink-0 items-center justify-center overflow-hidden rounded-xl bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-300">
              {startup.logoUrl ? (<img src={startup.logoUrl} alt="" className="size-full object-cover"/>) : (<Rocket className="size-5"/>)}
            </div>
            <div className="min-w-0">
              <h3 className="truncate font-semibold text-slate-900 dark:text-slate-100">{startup.name}</h3>
              <p className="truncate text-xs text-slate-500 dark:text-slate-400">{startup.industry ?? 'Uncategorized'}</p>
            </div>
            {startup.verified && (<BadgeCheck className="ml-auto size-5 shrink-0 text-brand-500" aria-label="Verified"/>)}
            {viewTo && (<Link to={viewTo} onClick={(e) => e.stopPropagation()} aria-label="View startup page" className="shrink-0 rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-brand-600 dark:hover:bg-slate-800 dark:hover:text-brand-400">
                <ExternalLink className="size-4"/>
              </Link>)}
          </div>

          <div className="flex flex-wrap gap-1.5">
            {startup.stage && <Badge tone="brand">{stageLabels[startup.stage] ?? startup.stage}</Badge>}
            {!startup.published && <Badge tone="amber">Draft</Badge>}
            {startup.interestedInvestorsCount > 0 && (<Badge tone="amber">
                <Star className="size-3"/>
                {startup.interestedInvestorsCount} interested
              </Badge>)}
          </div>

          <div className="mt-auto space-y-1.5">
            <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
              <span className="flex items-center gap-1">
                <TrendingUp className="size-3.5"/>
                {formatCurrency(startup.fundingProgress)} raised
              </span>
              <span>{formatCurrency(startup.fundingGoal)} goal</span>
            </div>
            <div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
              <div className="gradient-brand h-full rounded-full" style={{ width: `${progressPct}%` }}/>
            </div>
          </div>
        </Card>
    </motion.div>);
}
