import { useState } from 'react';
import toast from 'react-hot-toast';
import { CalendarDays, MapPin, Plus, Trash2, Trophy } from 'lucide-react';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { TextArea } from '../components/ui/TextArea';
import { Select } from '../components/ui/Select';
import { Spinner } from '../components/ui/Spinner';
import { EmptyState } from '../components/ui/EmptyState';
import { Modal } from '../components/ui/Modal';
import { useCreateEvent, useDeleteEvent, useEvents } from '../hooks/useEvents';
import { useAuth } from '../hooks/useAuth';
export function EventsPage() {
    const { user } = useAuth();
    const { data: events, isLoading } = useEvents();
    const createEvent = useCreateEvent();
    const deleteEvent = useDeleteEvent();
    const [modalOpen, setModalOpen] = useState(false);
    const [type, setType] = useState('EVENT');
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [eventDate, setEventDate] = useState('');
    const [location, setLocation] = useState('');
    const [link, setLink] = useState('');
    const handleSubmit = (e) => {
        e.preventDefault();
        createEvent.mutate({ type, title, description, eventDate: new Date(eventDate).toISOString(), location, link }, {
            onSuccess: () => {
                toast.success('Published');
                setModalOpen(false);
                setTitle('');
                setDescription('');
                setEventDate('');
                setLocation('');
                setLink('');
            },
            onError: () => toast.error('Could not publish'),
        });
    };
    return (<DashboardLayout>
      <div className="mb-6 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <CalendarDays className="size-5 text-brand-500"/>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Events &amp; Competitions</h1>
        </div>
        {user?.role === 'ADMIN' && (<Button size="sm" onClick={() => setModalOpen(true)}>
            <Plus className="size-4"/>
            New
          </Button>)}
      </div>

      {isLoading ? (<Spinner />) : !events || events.length === 0 ? (<EmptyState icon={CalendarDays} title="No upcoming events" description="Check back soon for networking events and competitions."/>) : (<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {events.map((ev) => (<Card key={ev.id} className="space-y-2 p-5">
              <div className="flex items-center justify-between">
                <Badge tone={ev.type === 'COMPETITION' ? 'brand' : 'green'}>
                  {ev.type === 'COMPETITION' ? <Trophy className="size-3"/> : <CalendarDays className="size-3"/>}
                  {ev.type}
                </Badge>
                {user?.role === 'ADMIN' && (<button onClick={() => deleteEvent.mutate(ev.id)} className="rounded-lg p-1 text-slate-400 hover:bg-red-50 hover:text-red-500 dark:hover:bg-red-500/10">
                    <Trash2 className="size-4"/>
                  </button>)}
              </div>
              <h3 className="font-semibold text-slate-900 dark:text-slate-100">{ev.title}</h3>
              {ev.description && <p className="text-sm text-slate-600 dark:text-slate-400">{ev.description}</p>}
              <p className="text-xs text-slate-500 dark:text-slate-400">{new Date(ev.eventDate).toLocaleString()}</p>
              {ev.location && (<p className="flex items-center gap-1 text-xs text-slate-500 dark:text-slate-400">
                  <MapPin className="size-3"/>
                  {ev.location}
                </p>)}
              {ev.link && (<a href={ev.link} target="_blank" rel="noreferrer" className="text-xs text-brand-600 hover:underline dark:text-brand-400">
                  More info
                </a>)}
            </Card>))}
        </div>)}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="New event or competition">
        <form onSubmit={handleSubmit} className="space-y-3">
          <Select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="EVENT">Event</option>
            <option value="COMPETITION">Competition</option>
          </Select>
          <Input label="Title" required value={title} onChange={(e) => setTitle(e.target.value)}/>
          <TextArea label="Description" value={description} onChange={(e) => setDescription(e.target.value)} rows={3}/>
          <Input label="Date & time" type="datetime-local" required value={eventDate} onChange={(e) => setEventDate(e.target.value)}/>
          <Input label="Location (optional)" value={location} onChange={(e) => setLocation(e.target.value)}/>
          <Input label="Link (optional)" value={link} onChange={(e) => setLink(e.target.value)}/>
          <Button type="submit" className="w-full" loading={createEvent.isPending}>
            Publish
          </Button>
        </form>
      </Modal>
    </DashboardLayout>);
}
