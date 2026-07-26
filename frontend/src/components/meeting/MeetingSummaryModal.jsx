import { useState } from 'react';
import toast from 'react-hot-toast';
import { Modal } from '../ui/Modal';
import { TextArea } from '../ui/TextArea';
import { Button } from '../ui/Button';
import { useSummarizeMeeting } from '../../hooks/useAiTools';
export function MeetingSummaryModal({ open, onClose, meetingId }) {
    const summarize = useSummarizeMeeting(meetingId);
    const [transcript, setTranscript] = useState('');
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!transcript.trim())
            return;
        summarize.mutate(transcript.trim(), { onError: () => toast.error('Could not summarize transcript') });
    };
    return (<Modal open={open} onClose={onClose} title="Meeting summary">
      <div className="space-y-4">
        <form onSubmit={handleSubmit} className="space-y-3">
          <TextArea label="Paste meeting transcript" rows={6} value={transcript} onChange={(e) => setTranscript(e.target.value)} placeholder="Founder: We're asking for ₹25 lakh... Investor: I'm concerned about competition..."/>
          <Button type="submit" loading={summarize.isPending} className="w-full">
            Summarize
          </Button>
        </form>

        {summarize.data && (<div className="space-y-3 rounded-xl bg-slate-50 p-4 text-sm dark:bg-slate-800/60">
            <p className="text-slate-700 dark:text-slate-300">{summarize.data.summary}</p>
            {summarize.data.amountsMentioned.length > 0 && (<p>
                <span className="font-medium">Amounts:</span> {summarize.data.amountsMentioned.join(', ')}
              </p>)}
            {summarize.data.concerns.length > 0 && (<div>
                <p className="font-medium">Concerns</p>
                <ul className="list-disc pl-5 text-slate-600 dark:text-slate-400">
                  {summarize.data.concerns.map((c, i) => (<li key={i}>{c}</li>))}
                </ul>
              </div>)}
            {summarize.data.actionItems.length > 0 && (<div>
                <p className="font-medium">Action items</p>
                <ul className="list-disc pl-5 text-slate-600 dark:text-slate-400">
                  {summarize.data.actionItems.map((a, i) => (<li key={i}>{a}</li>))}
                </ul>
              </div>)}
          </div>)}
      </div>
    </Modal>);
}
