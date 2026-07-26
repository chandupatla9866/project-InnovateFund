import { useState } from 'react';
import toast from 'react-hot-toast';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { TextArea } from '../ui/TextArea';
import { Button } from '../ui/Button';
import { useRequestMeeting } from '../../hooks/useMeetings';
export function ScheduleMeetingModal({ open, onClose, recipientId, startupId }) {
    const requestMeeting = useRequestMeeting();
    const [scheduledAt, setScheduledAt] = useState('');
    const [notes, setNotes] = useState('');
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!scheduledAt)
            return;
        requestMeeting.mutate({ recipientId, startupId, scheduledAt: new Date(scheduledAt).toISOString(), notes: notes || undefined }, {
            onSuccess: () => {
                toast.success('Meeting request sent');
                setScheduledAt('');
                setNotes('');
                onClose();
            },
            onError: () => toast.error('Could not send meeting request'),
        });
    };
    return (<Modal open={open} onClose={onClose} title="Schedule a meeting">
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input label="Date & time" type="datetime-local" required value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)}/>
        <TextArea label="Agenda (optional)" value={notes} onChange={(e) => setNotes(e.target.value)} rows={3}/>
        <Button type="submit" className="w-full" loading={requestMeeting.isPending}>
          Send request
        </Button>
      </form>
    </Modal>);
}
