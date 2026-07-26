import { useState } from 'react';
import toast from 'react-hot-toast';
import { Plus, Trash2, Users, X } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { TextArea } from '../ui/TextArea';
import { FileUploadField } from '../ui/FileUploadField';
import { useCreateTeamMember, useDeleteTeamMember, useTeamMembers } from '../../hooks/useTeam';
export function TeamCard({ startupId, isOwner }) {
    const { data: members, isLoading } = useTeamMembers(startupId);
    const createMember = useCreateTeamMember(startupId);
    const deleteMember = useDeleteTeamMember(startupId);
    const [adding, setAdding] = useState(false);
    const [name, setName] = useState('');
    const [role, setRole] = useState('');
    const [bio, setBio] = useState('');
    const [photoUrl, setPhotoUrl] = useState('');
    const resetForm = () => {
        setName('');
        setRole('');
        setBio('');
        setPhotoUrl('');
        setAdding(false);
    };
    const handleCreate = () => {
        if (!name.trim())
            return;
        createMember.mutate({ name: name.trim(), role: role.trim() || undefined, bio: bio.trim() || undefined, photoUrl: photoUrl.trim() || undefined, displayOrder: members?.length ?? 0 }, {
            onSuccess: () => {
                toast.success('Team member added');
                resetForm();
            },
            onError: () => toast.error('Could not add team member'),
        });
    };
    const handleDelete = (memberId) => {
        if (!confirm('Remove this team member?'))
            return;
        deleteMember.mutate(memberId, {
            onSuccess: () => toast.success('Removed'),
            onError: () => toast.error('Could not remove team member'),
        });
    };
    if (!isLoading && (!members || members.length === 0) && !isOwner) {
        return null;
    }
    return (<Card className="p-5">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="flex items-center gap-1.5 font-semibold text-slate-900 dark:text-slate-100">
          <Users className="size-4 text-brand-500"/>
          Team
        </h3>
        {isOwner && !adding && (<Button size="sm" variant="ghost" onClick={() => setAdding(true)}>
            <Plus className="size-4"/>
            Add
          </Button>)}
      </div>

      {adding && (<div className="mb-4 space-y-2 rounded-xl border border-slate-100 p-3 dark:border-slate-800">
          <Input value={name} onChange={(e) => setName(e.target.value)} placeholder="Name"/>
          <Input value={role} onChange={(e) => setRole(e.target.value)} placeholder="Role (e.g. Co-founder & CTO)"/>
          <TextArea value={bio} onChange={(e) => setBio(e.target.value)} placeholder="Short bio (optional)" rows={2}/>
          <FileUploadField label="Photo" value={photoUrl} onChange={setPhotoUrl} folder="team-photos" accept="image/*" preview="image"/>
          <div className="flex gap-2">
            <Button size="sm" variant="primary" loading={createMember.isPending} onClick={handleCreate}>
              Save
            </Button>
            <Button size="sm" variant="ghost" onClick={resetForm}>
              <X className="size-4"/>
              Cancel
            </Button>
          </div>
        </div>)}

      {!members || members.length === 0 ? (<p className="text-sm text-slate-400">No team members added yet.</p>) : (<div className="grid gap-3 sm:grid-cols-2">
          {members.map((m) => (<div key={m.id} className="flex items-start gap-3 rounded-xl border border-slate-100 p-3 dark:border-slate-800">
              <div className="flex size-10 shrink-0 items-center justify-center overflow-hidden rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-300">
                {m.photoUrl ? <img src={m.photoUrl} alt="" className="size-full object-cover"/> : <Users className="size-4.5"/>}
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-sm font-medium text-slate-800 dark:text-slate-200">{m.name}</p>
                {m.role && <p className="text-xs text-brand-600 dark:text-brand-400">{m.role}</p>}
                {m.bio && <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{m.bio}</p>}
              </div>
              {isOwner && (<button onClick={() => handleDelete(m.id)} aria-label="Remove team member" className="shrink-0 rounded-lg p-1 text-slate-300 hover:bg-red-50 hover:text-red-500 dark:hover:bg-red-500/10">
                  <Trash2 className="size-3.5"/>
                </button>)}
            </div>))}
        </div>)}
    </Card>);
}
