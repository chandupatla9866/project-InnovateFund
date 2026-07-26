import { Link } from 'react-router-dom';
import { Briefcase, CreditCard } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Spinner } from '../../components/ui/Spinner';
import { EmptyState } from '../../components/ui/EmptyState';
import { useMyPortfolio } from '../../hooks/useInvestment';
function formatCurrency(value) {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);
}
export function PortfolioPage() {
    const { data: investments, isLoading } = useMyPortfolio();
    const total = investments?.filter((i) => i.status === 'PAID').reduce((sum, i) => sum + i.amount, 0) ?? 0;
    return (<DashboardLayout>
      <div className="mb-6 flex items-center gap-2">
        <Briefcase className="size-5 text-brand-500"/>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Portfolio</h1>
      </div>

      {isLoading ? (<Spinner />) : !investments || investments.length === 0 ? (<EmptyState icon={Briefcase} title="No investments yet" description="Investments a founder records against your name will show up here."/>) : (<>
          <Card className="mb-4 p-5">
            <p className="text-sm text-slate-500 dark:text-slate-400">Total invested (paid)</p>
            <p className="text-2xl font-bold text-slate-900 dark:text-slate-100">{formatCurrency(total)}</p>
          </Card>
          <div className="space-y-3">
            {investments.map((i) => (<Card key={i.id} className="flex flex-wrap items-center justify-between gap-3 p-4">
                <div>
                  <div className="flex flex-wrap items-center gap-1.5">
                    <Link to={`/startups/${i.startupId}`} className="font-medium text-slate-800 hover:underline dark:text-slate-200">
                      {i.startupName}
                    </Link>
                    {i.status === 'PAID' && <Badge tone="green">Paid</Badge>}
                    {i.status === 'PENDING' && <Badge tone="amber">Pending payment</Badge>}
                    {i.status === 'CANCELLED' && <Badge tone="slate">Cancelled</Badge>}
                  </div>
                  <p className="text-xs text-slate-500 dark:text-slate-400">
                    {i.status === 'PAID' && i.paidAt ? `Paid ${new Date(i.paidAt).toLocaleDateString()}` : `Recorded ${new Date(i.createdAt).toLocaleDateString()}`}
                  </p>
                  {i.notes && <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">{i.notes}</p>}
                </div>
                <div className="flex items-center gap-3">
                  <span className="font-semibold text-slate-900 dark:text-slate-100">{formatCurrency(i.amount)}</span>
                  {i.status === 'PENDING' && i.paymentLinkUrl && (<a href={i.paymentLinkUrl} target="_blank" rel="noreferrer">
                      <Button size="sm">
                        <CreditCard className="size-4"/>
                        Pay now
                      </Button>
                    </a>)}
                </div>
              </Card>))}
          </div>
        </>)}
    </DashboardLayout>);
}
