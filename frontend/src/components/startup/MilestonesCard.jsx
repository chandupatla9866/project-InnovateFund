import { useState } from 'react';
import toast from 'react-hot-toast';
import { CheckCircle2, Circle, Flag, Plus, Trash2, X } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { TextArea } from '../ui/TextArea';
import { useCreateMilestone, useDeleteMilestone, useMilestones, useToggleMilestoneComplete } from '../../hooks/useMilestones';
function formatDate(iso) {
    if (!iso)
        return null;
    return new Date(iso).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
}
export function MilestonesCard({ startupId, isOwner }) {
    const { data: milestones, isLoading } = useMilestones(startupId);
    const createMilestone = useCreateMilestone(startupId);
    const toggleComplete = useToggleMilestoneComplete(startupId);
    const deleteMilestone = useDeleteMilestone(startupId);
    const [adding, setAdding] = useState(false);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [targetDate, setTargetDate] = useState('');
    const resetForm = () => {
        setTitle('');
        setDescription('');
        setTargetDate('');
        setAdding(false);
    };
    const handleCreate = () => {
        if (!title.trim())
            return;
        createMilestone.mutate({ title: title.trim(), description: description.trim() || undefined, targetDate: targetDate || undefined }, {
            onSuccess: () => {
                toast.success('Milestone added');
                resetForm();
            },
            onError: () => toast.error('Could not add milestone'),
        });
    };
    const handleToggle = (id, completed) => {
        toggleComplete.mutate(id, {
            onSuccess: () => toast.success(completed ? 'Milestone reopened' : 'Milestone marked complete — posted to your feed'),
            onError: () => toast.error('Something went wrong'),
        });
    };
    const handleDelete = (id) => {
        if (!confirm('Delete this milestone?'))
            return;
        deleteMilestone.mutate(id, {
            onSuccess: () => toast.success('Milestone deleted'),
            onError: () => toast.error('Could not delete milestone'),
        });
    };
    if (!isLoading && (!milestones || milestones.length === 0) && !isOwner) {
        return null;
    }
    return (<Card className="p-5">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="flex items-center gap-1.5 font-semibold text-slate-900 dark:text-slate-100">
          <Flag className="size-4 text-brand-500"/>
          Milestones
        </h3>
        {isOwner && !adding && (<Button size="sm" variant="ghost" onClick={() => setAdding(true)}>
            <Plus className="size-4"/>
            Add
          </Button>)}
      </div>

      {adding && (<div className="mb-4 space-y-2 rounded-xl border border-slate-100 p-3 dark:border-slate-800">
          <Input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Milestone title"/>
          <TextArea value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Description (optional)" rows={2}/>
          <Input type="date" value={targetDate} onChange={(e) => setTargetDate(e.target.value)}/>
          <div className="flex gap-2">
            <Button size="sm" variant="primary" loading={createMilestone.isPending} onClick={handleCreate}>
              Save
            </Button>
            <Button size="sm" variant="ghost" onClick={resetForm}>
              <X className="size-4"/>
              Cancel
            </Button>
          </div>
        </div>)}

      {!milestones || milestones.length === 0 ? (<p className="text-sm text-slate-400">No milestones tracked yet.</p>) : (<div className="space-y-2">
          {milestones.map((m) => (<div key={m.id} className="flex items-start gap-2.5 rounded-xl border border-slate-100 px-3 py-2.5 dark:border-slate-800">
              <button onClick={() => isOwner && handleToggle(m.id, m.completed)} disabled={!isOwner} aria-label={m.completed ? 'Mark incomplete' : 'Mark complete'} className="mt-0.5 shrink-0 text-slate-400 disabled:cursor-default">
                {m.completed ? (<CheckCircle2 className="size-4.5 text-green-500"/>) : (<Circle className="size-4.5 hover:text-brand-500"/>)}
              </button>
              <div className="min-w-0 flex-1">
                <p className={m.completed ? 'text-sm text-slate-400 line-through' : 'text-sm font-medium text-slate-800 dark:text-slate-200'}>
                  {m.title}
                </p>
                {m.description && <p className="mt-0.5 text-xs text-slate-500 dark:text-slate-400">{m.description}</p>}
                {m.targetDate && (<p className="mt-0.5 text-xs text-slate-400">
                    {m.completed ? 'Completed' : 'Target'}: {formatDate(m.completed ? m.completedAt : m.targetDate)}
                  </p>)}
              </div>
              {isOwner && (<button onClick={() => handleDelete(m.id)} aria-label="Delete milestone" className="shrink-0 rounded-lg p-1 text-slate-300 hover:bg-red-50 hover:text-red-500 dark:hover:bg-red-500/10">
                  <Trash2 className="size-3.5"/>
                </button>)}
            </div>))}
        </div>)}
    </Card>);
}
