import { useState } from 'react';
import toast from 'react-hot-toast';
import { Check, Copy, CreditCard } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { TextArea } from '../ui/TextArea';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';
import { useRecordInvestment } from '../../hooks/useInvestment';
export function RecordInvestmentModal({ open, onClose, startupId, interestedInvestors }) {
    const recordInvestment = useRecordInvestment(startupId);
    // Auto-select when there's only one investor to pick from (e.g. the "Ask for more" shortcut
    // on the Funding page always targets one specific investor) — no point making someone open a
    // dropdown just to choose the only option in it.
    const [investorId, setInvestorId] = useState(() => (interestedInvestors.length === 1 ? interestedInvestors[0].investorId : ''));
    const [amount, setAmount] = useState('');
    const [notes, setNotes] = useState('');
    const [result, setResult] = useState(null);
    const [copied, setCopied] = useState(false);
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!investorId || !amount)
            return;
        recordInvestment.mutate({ investorId, amount: Number(amount), notes: notes || undefined }, {
            onSuccess: (data) => {
                toast.success('Payment link created — the investor has been notified');
                setResult(data);
            },
            onError: () => toast.error('Could not create payment link'),
        });
    };
    const handleClose = () => {
        setInvestorId('');
        setAmount('');
        setNotes('');
        setResult(null);
        setCopied(false);
        onClose();
    };
    const copyLink = async () => {
        if (!result?.paymentLinkUrl)
            return;
        await navigator.clipboard.writeText(result.paymentLinkUrl);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };
    return (<Modal open={open} onClose={handleClose} title="Record an investment">
      {result ? (<div className="space-y-4">
          <p className="text-sm text-slate-600 dark:text-slate-400">
            A real Razorpay payment link was created and the investor has been notified. Your funding progress will update
            automatically once they pay — this platform doesn't take your word for it.
          </p>
          <div className="flex items-center gap-2 rounded-xl border border-slate-200 p-3 dark:border-slate-700">
            <a href={result.paymentLinkUrl} target="_blank" rel="noreferrer" className="min-w-0 flex-1 truncate text-sm text-brand-600 hover:underline dark:text-brand-400">
              {result.paymentLinkUrl}
            </a>
            <button type="button" onClick={copyLink} aria-label="Copy link" className="shrink-0 rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800">
              {copied ? <Check className="size-4 text-emerald-500"/> : <Copy className="size-4"/>}
            </button>
          </div>
          <Button className="w-full" onClick={handleClose}>Done</Button>
        </div>) : (<form onSubmit={handleSubmit} className="space-y-4">
          <p className="text-xs text-slate-500 dark:text-slate-400">
            This creates a real Razorpay payment link for the investor to pay — nothing is recorded as funded until they actually pay it.
          </p>
          <Select label="Investor" required value={investorId} onChange={(e) => setInvestorId(e.target.value)}>
            <option value="">Select an investor</option>
            {interestedInvestors.map((i) => (<option key={i.investorId} value={i.investorId}>
                {i.investorName}
                {i.firmName ? ` (${i.firmName})` : ''}
              </option>))}
          </Select>
          <Input label="Amount (₹)" type="number" min="1" step="0.01" required value={amount} onChange={(e) => setAmount(e.target.value)}/>
          <TextArea label="Notes (optional)" value={notes} onChange={(e) => setNotes(e.target.value)} rows={2}/>
          <Button type="submit" className="w-full" loading={recordInvestment.isPending}>
            <CreditCard className="size-4"/>
            Create payment link
          </Button>
        </form>)}
    </Modal>);
}
