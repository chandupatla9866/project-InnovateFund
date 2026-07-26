import { useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { Image, Loader2, X } from 'lucide-react';
import { Card } from '../ui/Card';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';
import { useCreatePost } from '../../hooks/useFeed';
import { useMyStartups } from '../../hooks/useStartups';
import { useAuth } from '../../hooks/useAuth';
import { uploadFile } from '../../api/uploadApi';
function isVideoUrl(url) {
    return /\.(mp4|webm|mov|ogg)(\?.*)?$/i.test(url);
}
const founderTypes = [
    { value: 'PRODUCT_LAUNCH', label: 'Product Launch' },
    { value: 'MILESTONE', label: 'Milestone' },
    { value: 'FUNDING_UPDATE', label: 'Funding Update' },
    { value: 'ACHIEVEMENT', label: 'Achievement' },
    { value: 'HIRING', label: 'Hiring' },
];
const investorTypes = [
    { value: 'INVESTMENT_OPPORTUNITY', label: 'Investment Opportunity' },
    { value: 'MARKET_INSIGHT', label: 'Market Insight' },
    { value: 'STARTUP_TIPS', label: 'Startup Tips' },
];
const adminTypes = [{ value: 'ANNOUNCEMENT', label: 'Announcement' }];
export function PostComposer() {
    const { user } = useAuth();
    const { data: myStartups } = useMyStartups(user?.role === 'FOUNDER');
    const createPost = useCreatePost();
    const [text, setText] = useState('');
    const [startupId, setStartupId] = useState('');
    const [mediaUrl, setMediaUrl] = useState('');
    const [uploadingMedia, setUploadingMedia] = useState(false);
    const fileInputRef = useRef(null);
    const typeOptions = user?.role === 'FOUNDER' ? founderTypes : user?.role === 'INVESTOR' ? investorTypes : adminTypes;
    // Lazy-initialized from typeOptions so the <select>'s value always matches a real <option> —
    // 'GENERAL' isn't offered in any role's list, which previously left the dropdown showing one
    // option while the submitted value silently stayed GENERAL underneath.
    const [type, setType] = useState(typeOptions[0].value);
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!text.trim())
            return;
        createPost.mutate({ text: text.trim(), type, startupId: startupId || undefined, mediaUrl: mediaUrl || undefined }, {
            onSuccess: () => {
                setText('');
                setMediaUrl('');
                toast.success('Posted to the feed');
            },
            onError: () => toast.error('Could not create post'),
        });
    };
    const handleMediaSelect = async (e) => {
        const file = e.target.files?.[0];
        e.target.value = '';
        if (!file)
            return;
        setUploadingMedia(true);
        try {
            const url = await uploadFile(file, 'post-media');
            setMediaUrl(url);
        }
        catch {
            toast.error('Could not upload media');
        }
        finally {
            setUploadingMedia(false);
        }
    };
    return (<Card className="space-y-3 p-5">
      <form onSubmit={handleSubmit} className="space-y-3">
        <textarea value={text} onChange={(e) => setText(e.target.value)} placeholder="Share an update with the community..." rows={3} className="w-full resize-none rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 dark:border-slate-700 dark:bg-slate-900"/>
        {mediaUrl && (<div className="relative inline-block">
            {isVideoUrl(mediaUrl) ? (<video src={mediaUrl} className="h-24 rounded-xl bg-black object-contain"/>) : (<img src={mediaUrl} alt="" className="h-24 rounded-xl object-cover"/>)}
            <button type="button" onClick={() => setMediaUrl('')} aria-label="Remove media" className="absolute -right-1.5 -top-1.5 flex size-5 items-center justify-center rounded-full bg-slate-900 text-white shadow">
              <X className="size-3"/>
            </button>
          </div>)}

        <div className="flex flex-wrap items-center gap-3">
          <Select value={type} onChange={(e) => setType(e.target.value)} className="w-auto">
            {typeOptions.map((t) => (<option key={t.value} value={t.value}>
                {t.label}
              </option>))}
          </Select>
          {user?.role === 'FOUNDER' && myStartups && myStartups.length > 0 && (<Select value={startupId} onChange={(e) => setStartupId(e.target.value)} className="w-auto">
              <option value="">No startup tag</option>
              {myStartups.map((s) => (<option key={s.id} value={s.id}>
                  {s.name}
                </option>))}
            </Select>)}
          <input ref={fileInputRef} type="file" accept="image/*,video/*" onChange={handleMediaSelect} className="hidden"/>
          <button type="button" onClick={() => fileInputRef.current?.click()} disabled={uploadingMedia} aria-label="Attach image or video" className="flex items-center justify-center rounded-lg p-2 text-slate-500 transition-colors hover:bg-slate-100 disabled:opacity-60 dark:text-slate-400 dark:hover:bg-slate-800">
            {uploadingMedia ? <Loader2 className="size-4.5 animate-spin"/> : <Image className="size-4.5"/>}
          </button>
          <Button type="submit" size="sm" className="ml-auto" loading={createPost.isPending}>
            Post
          </Button>
        </div>
      </form>
    </Card>);
}
