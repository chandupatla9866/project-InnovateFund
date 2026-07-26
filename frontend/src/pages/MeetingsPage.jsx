import { useState } from 'react';
import toast from 'react-hot-toast';
import { Calendar, Clock, FileText, Video } from 'lucide-react';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { EmptyState } from '../components/ui/EmptyState';
import { MeetingSummaryModal } from '../components/meeting/MeetingSummaryModal';
import { useAcceptMeeting, useCancelMeeting, useMyMeetings, useRejectMeeting } from '../hooks/useMeetings';
import { useAuth } from '../hooks/useAuth';
const statusTone = {
    PENDING: 'amber',
    ACCEPTED: 'green',
    REJECTED: 'red',
    CANCELLED: 'slate',
};
export function MeetingsPage() {
    const { user } = useAuth();
    const { data: meetings, isLoading } = useMyMeetings();
    const acceptMeeting = useAcceptMeeting();
    const rejectMeeting = useRejectMeeting();
    const cancelMeeting = useCancelMeeting();
    const [summaryMeetingId, setSummaryMeetingId] = useState(null);
    return (<DashboardLayout>
      <div className="mb-6 flex items-center gap-2">
        <Calendar className="size-5 text-brand-500"/>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Meetings</h1>
      </div>

      {isLoading ? (<Spinner />) : !meetings || meetings.length === 0 ? (<EmptyState icon={Calendar} title="No meetings yet" description="Meeting requests you send or receive will show up here."/>) : (<div className="space-y-3">
          {meetings.map((m) => {
                const isRecipient = user?.id === m.recipientId;
                const counterpart = isRecipient ? m.requesterName : m.recipientName;
                return (<Card key={m.id} className="flex flex-wrap items-center justify-between gap-3 p-4">
                <div>
                  <div className="flex items-center gap-2">
                    <p className="font-medium text-slate-800 dark:text-slate-200">
                      {isRecipient ? `${counterpart} requested a meeting` : `Meeting with ${counterpart}`}
                    </p>
                    <Badge tone={statusTone[m.status]}>{m.status}</Badge>
                  </div>
                  <p className="mt-1 flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
                    <Clock className="size-3.5"/>
                    {new Date(m.scheduledAt).toLocaleString()} · {m.durationMinutes} min
                    {m.startupName && ` · ${m.startupName}`}
                  </p>
                  {m.notes && <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">{m.notes}</p>}
                </div>
                <div className="flex gap-2">
                  {isRecipient && m.status === 'PENDING' && (<>
                      <Button size="sm" variant="secondary" onClick={() => rejectMeeting.mutate(m.id, { onSuccess: () => toast.success('Meeting declined') })}>
                        Decline
                      </Button>
                      <Button size="sm" onClick={() => acceptMeeting.mutate(m.id, { onSuccess: () => toast.success('Meeting accepted') })}>
                        Accept
                      </Button>
                    </>)}
                  {m.status === 'ACCEPTED' && m.meetingLink && (<a href={m.meetingLink} target="_blank" rel="noreferrer">
                      <Button size="sm" variant="primary">
                        <Video className="size-4"/>
                        Join Video Call
                      </Button>
                    </a>)}
                  {m.status === 'ACCEPTED' && (<Button size="sm" variant="secondary" onClick={() => setSummaryMeetingId(m.id)}>
                      <FileText className="size-4"/>
                      Summarize
                    </Button>)}
                  {(m.status === 'PENDING' || m.status === 'ACCEPTED') && (<Button size="sm" variant="ghost" onClick={() => cancelMeeting.mutate(m.id, { onSuccess: () => toast.success('Meeting cancelled') })}>
                      Cancel
                    </Button>)}
                </div>
              </Card>);
            })}
        </div>)}

      {summaryMeetingId && (<MeetingSummaryModal open={!!summaryMeetingId} onClose={() => setSummaryMeetingId(null)} meetingId={summaryMeetingId}/>)}
    </DashboardLayout>);
}
