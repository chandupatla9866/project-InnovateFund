import { Card } from './Card';
export function StatCard({ icon: Icon, label, value, tone = 'brand', }) {
    const toneClasses = {
        brand: 'bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-300',
        green: 'bg-green-50 text-green-600 dark:bg-green-500/10 dark:text-green-300',
        amber: 'bg-amber-50 text-amber-600 dark:bg-amber-500/10 dark:text-amber-300',
        pink: 'bg-pink-50 text-pink-600 dark:bg-pink-500/10 dark:text-pink-300',
    };
    return (<Card className="flex items-center gap-3 p-4">
      <div className={`flex size-10 shrink-0 items-center justify-center rounded-xl ${toneClasses[tone]}`}>
        <Icon className="size-5"/>
      </div>
      <div className="min-w-0">
        <p className="truncate text-xl font-bold text-slate-900 dark:text-slate-100">{value}</p>
        <p className="truncate text-xs text-slate-500 dark:text-slate-400">{label}</p>
      </div>
    </Card>);
}
