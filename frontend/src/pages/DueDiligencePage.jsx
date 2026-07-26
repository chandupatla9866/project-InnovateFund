import { useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { FileText, FolderLock, Loader2, Trash2, Upload } from 'lucide-react';
import { uploadFile } from '../api/uploadApi';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { Spinner } from '../components/ui/Spinner';
import { EmptyState } from '../components/ui/EmptyState';
import { useStartup } from '../hooks/useStartups';
import { useAuth } from '../hooks/useAuth';
import { useApproveDueDiligenceRequest, useDueDiligenceDocuments, useDueDiligenceRequests, useDeleteDueDiligenceDocument, useMyDueDiligenceStatus, useRejectDueDiligenceRequest, useRequestDueDiligenceAccess, useUploadDueDiligenceDocument, } from '../hooks/useDueDiligence';
const docTypes = ['FINANCIAL_REPORT', 'REVENUE', 'CERTIFICATE', 'PATENT', 'LEGAL', 'OTHER'];
export function DueDiligencePage() {
    const { id: startupId } = useParams();
    const { user } = useAuth();
    const { data: startup } = useStartup(startupId);
    const isOwner = user?.id === startup?.founderId;
    const { data: myStatus } = useMyDueDiligenceStatus(user?.role === 'INVESTOR' ? startupId : undefined);
    const requestAccess = useRequestDueDiligenceAccess(startupId ?? '');
    const { data: requests } = useDueDiligenceRequests(isOwner ? startupId : undefined);
    const approveRequest = useApproveDueDiligenceRequest(startupId ?? '');
    const rejectRequest = useRejectDueDiligenceRequest(startupId ?? '');
    const hasAccess = isOwner || myStatus?.status === 'APPROVED';
    const { data: documents, isLoading: loadingDocs } = useDueDiligenceDocuments(startupId, hasAccess);
    const uploadDocument = useUploadDueDiligenceDocument(startupId ?? '');
    const deleteDocument = useDeleteDueDiligenceDocument(startupId ?? '');
    const [title, setTitle] = useState('');
    const [url, setUrl] = useState('');
    const [docType, setDocType] = useState('FINANCIAL_REPORT');
    const [uploadingFile, setUploadingFile] = useState(false);
    const fileInputRef = useRef(null);
    const handleFileSelect = async (e) => {
        const file = e.target.files?.[0];
        e.target.value = '';
        if (!file)
            return;
        setUploadingFile(true);
        try {
            const uploadedUrl = await uploadFile(file, 'due-diligence-documents');
            setUrl(uploadedUrl);
            if (!title)
                setTitle(file.name.replace(/\.[^.]+$/, ''));
        }
        catch {
            toast.error('Could not upload file');
        }
        finally {
            setUploadingFile(false);
        }
    };
    const handleUpload = (e) => {
        e.preventDefault();
        uploadDocument.mutate({ title, url, documentType: docType }, {
            onSuccess: () => {
                toast.success('Document uploaded');
                setTitle('');
                setUrl('');
            },
            onError: () => toast.error('Could not upload document'),
        });
    };
    return (<DashboardLayout>
      <div className="mb-6 flex items-center gap-2">
        <FolderLock className="size-5 text-brand-500"/>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
          Due Diligence {startup && `— ${startup.name}`}
        </h1>
      </div>

      {user?.role === 'INVESTOR' && !isOwner && (<Card className="mb-6 p-5">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-slate-800 dark:text-slate-200">Your access status</p>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Founders review and approve access before financial documents become visible.
              </p>
            </div>
            {!myStatus ? (<Button size="sm" onClick={() => requestAccess.mutate()} loading={requestAccess.isPending}>
                Request access
              </Button>) : (<Badge tone={myStatus.status === 'APPROVED' ? 'green' : myStatus.status === 'REJECTED' ? 'red' : 'amber'}>
                {myStatus.status}
              </Badge>)}
          </div>
        </Card>)}

      {isOwner && requests && requests.filter((r) => r.status === 'REQUESTED').length > 0 && (<Card className="mb-6 space-y-3 p-5">
          <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">Pending access requests</p>
          {requests
                .filter((r) => r.status === 'REQUESTED')
                .map((r) => (<div key={r.id} className="flex items-center justify-between rounded-xl border border-slate-100 px-3 py-2.5 dark:border-slate-800">
                <span className="text-sm text-slate-700 dark:text-slate-300">{r.investorName}</span>
                <div className="flex gap-2">
                  <Button size="sm" variant="secondary" onClick={() => rejectRequest.mutate(r.id)}>
                    Decline
                  </Button>
                  <Button size="sm" onClick={() => approveRequest.mutate(r.id)}>
                    Approve
                  </Button>
                </div>
              </div>))}
        </Card>)}

      {isOwner && (<Card className="mb-6 p-5">
          <p className="mb-3 text-sm font-semibold text-slate-800 dark:text-slate-200">Upload a document</p>
          <form onSubmit={handleUpload} className="grid gap-3 sm:grid-cols-3">
            <Input placeholder="Title" required value={title} onChange={(e) => setTitle(e.target.value)}/>
            <div className="flex items-center gap-2">
              <Input placeholder="Document URL" required value={url} onChange={(e) => setUrl(e.target.value)} className="flex-1"/>
              <input ref={fileInputRef} type="file" accept="application/pdf,image/*" onChange={handleFileSelect} className="hidden"/>
              <button type="button" onClick={() => fileInputRef.current?.click()} disabled={uploadingFile} aria-label="Upload document" className="flex shrink-0 items-center justify-center rounded-xl border border-slate-200 p-2.5 text-slate-500 transition-colors hover:bg-slate-50 disabled:opacity-60 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-800">
                {uploadingFile ? <Loader2 className="size-4 animate-spin"/> : <Upload className="size-4"/>}
              </button>
            </div>
            <div className="flex gap-2">
              <Select value={docType} onChange={(e) => setDocType(e.target.value)}>
                {docTypes.map((t) => (<option key={t} value={t}>
                    {t.replace('_', ' ')}
                  </option>))}
              </Select>
              <Button type="submit" loading={uploadDocument.isPending}>
                Upload
              </Button>
            </div>
          </form>
        </Card>)}

      <Card className="p-5">
        <p className="mb-3 text-sm font-semibold text-slate-800 dark:text-slate-200">Documents</p>
        {!hasAccess ? (<EmptyState icon={FolderLock} title="No access yet" description="Documents appear here once the founder approves your due diligence request."/>) : loadingDocs ? (<Spinner />) : !documents || documents.length === 0 ? (<EmptyState icon={FileText} title="No documents uploaded yet"/>) : (<div className="space-y-2">
            {documents.map((d) => (<div key={d.id} className="flex items-center justify-between rounded-xl border border-slate-100 px-3 py-2.5 dark:border-slate-800">
                <a href={d.url} target="_blank" rel="noreferrer" className="flex items-center gap-2 text-sm text-brand-600 hover:underline dark:text-brand-400">
                  <FileText className="size-4"/>
                  {d.title}
                  <Badge tone="slate">{d.documentType.replace('_', ' ')}</Badge>
                </a>
                {isOwner && (<button onClick={() => deleteDocument.mutate(d.id)} className="rounded-lg p-1.5 text-slate-400 hover:bg-red-50 hover:text-red-500 dark:hover:bg-red-500/10">
                    <Trash2 className="size-4"/>
                  </button>)}
              </div>))}
          </div>)}
      </Card>
    </DashboardLayout>);
}
