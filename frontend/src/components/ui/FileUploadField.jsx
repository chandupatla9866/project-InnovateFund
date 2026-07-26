import { useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { Loader2, Upload, X } from 'lucide-react';
import { Input } from './Input';
import { uploadFile } from '../../api/uploadApi';
export function FileUploadField({ label, value, onChange, folder, accept, preview }) {
    const [uploading, setUploading] = useState(false);
    const fileInputRef = useRef(null);
    const handleFileSelect = async (e) => {
        const file = e.target.files?.[0];
        e.target.value = '';
        if (!file)
            return;
        setUploading(true);
        try {
            const url = await uploadFile(file, folder);
            onChange(url);
            toast.success(`${label} uploaded`);
        }
        catch {
            toast.error(`Could not upload ${label.toLowerCase()}`);
        }
        finally {
            setUploading(false);
        }
    };
    return (<div className="flex flex-col gap-1.5">
      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">{label}</label>
      <div className="flex items-center gap-2">
        <Input value={value} onChange={(e) => onChange(e.target.value)} placeholder="Paste a URL or upload a file" className="flex-1"/>
        <input ref={fileInputRef} type="file" accept={accept} onChange={handleFileSelect} className="hidden"/>
        <button type="button" onClick={() => fileInputRef.current?.click()} disabled={uploading} className="flex shrink-0 items-center gap-1.5 rounded-xl border border-slate-200 px-3 py-2.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 disabled:opacity-60 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800">
          {uploading ? <Loader2 className="size-4 animate-spin"/> : <Upload className="size-4"/>}
          Upload
        </button>
        {value && (<button type="button" onClick={() => onChange('')} aria-label={`Clear ${label}`} className="shrink-0 rounded-lg p-2 text-slate-400 hover:bg-red-50 hover:text-red-500 dark:hover:bg-red-500/10">
            <X className="size-4"/>
          </button>)}
      </div>
      {preview === 'image' && value && (<img src={value} alt="" className="mt-1 h-20 w-20 rounded-xl border border-slate-200 object-cover dark:border-slate-700"/>)}
    </div>);
}
