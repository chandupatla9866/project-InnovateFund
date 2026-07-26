import { useState } from 'react';
import toast from 'react-hot-toast';
import { Modal } from './Modal';
import { TextArea } from './TextArea';
import { Button } from './Button';
import { useSubmitReport } from '../../hooks/useReports';
export function ReportModal({ open, onClose, targetType, targetId }) {
    const submitReport = useSubmitReport();
    const [reason, setReason] = useState('');
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!reason.trim())
            return;
        submitReport.mutate({ targetType, targetId, reason: reason.trim() }, {
            onSuccess: () => {
                toast.success('Reported — an admin will review this');
                setReason('');
                onClose();
            },
            onError: () => toast.error('Could not submit report'),
        });
    };
    return (<Modal open={open} onClose={onClose} title="Report content">
      <form onSubmit={handleSubmit} className="space-y-4">
        <TextArea label="Why are you reporting this?" required rows={3} value={reason} onChange={(e) => setReason(e.target.value)} placeholder="e.g. spam, copied pitch, inappropriate content..."/>
        <Button type="submit" variant="danger" className="w-full" loading={submitReport.isPending}>
          Submit report
        </Button>
      </form>
    </Modal>);
}
