import { useNavigate, useParams, Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ExternalLink, Sparkles, Eye, EyeOff, Wand2 } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { StartupForm } from '../../components/startup/StartupForm';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Spinner } from '../../components/ui/Spinner';
import { useCreateStartup, usePublishStartup, useStartup, useUnpublishStartup, useUpdateStartup, } from '../../hooks/useStartups';
export function StartupEditorPage() {
    const { id } = useParams();
    const isEdit = !!id;
    const navigate = useNavigate();
    const { data: startup, isLoading } = useStartup(id);
    const createStartup = useCreateStartup();
    const updateStartup = useUpdateStartup(id ?? '');
    const publishStartup = usePublishStartup();
    const unpublishStartup = useUnpublishStartup();
    const handleSubmit = (values) => {
        if (isEdit && id) {
            updateStartup.mutate(values, {
                onSuccess: () => toast.success('Startup updated'),
                onError: () => toast.error('Could not update startup'),
            });
        }
        else {
            createStartup.mutate(values, {
                onSuccess: (created) => {
                    toast.success('Startup created as a draft');
                    navigate(`/founder/startups/${created.id}/edit`);
                },
                onError: () => toast.error('Could not create startup'),
            });
        }
    };
    const togglePublish = () => {
        if (!id || !startup)
            return;
        if (startup.published) {
            unpublishStartup.mutate(id, { onSuccess: () => toast.success('Startup unpublished') });
        }
        else {
            publishStartup.mutate(id, { onSuccess: () => toast.success('Startup published!') });
        }
    };
    if (isEdit && isLoading) {
        return (<DashboardLayout>
        <Spinner />
      </DashboardLayout>);
    }
    return (<DashboardLayout>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
            {isEdit ? 'Edit Startup' : 'Create Startup'}
          </h1>
          {startup && (<div className="mt-1 flex items-center gap-2">
              <Badge tone={startup.published ? 'green' : 'amber'}>{startup.published ? 'Published' : 'Draft'}</Badge>
              {startup.verified && <Badge tone="brand">Verified</Badge>}
            </div>)}
        </div>
        {isEdit && startup && (<div className="flex flex-wrap gap-2">
            <Link to={`/startups/${id}`}>
              <Button variant="secondary" size="sm">
                <ExternalLink className="size-4"/>
                View
              </Button>
            </Link>
            <Link to={`/founder/startups/${id}/ai-report`}>
              <Button variant="secondary" size="sm">
                <Sparkles className="size-4"/>
                AI Analysis
              </Button>
            </Link>
            <Link to={`/founder/startups/${id}/ai-tools`}>
              <Button variant="secondary" size="sm">
                <Wand2 className="size-4"/>
                AI Tools
              </Button>
            </Link>
            <Button variant={startup.published ? 'secondary' : 'primary'} size="sm" onClick={togglePublish} loading={publishStartup.isPending || unpublishStartup.isPending}>
              {startup.published ? <EyeOff className="size-4"/> : <Eye className="size-4"/>}
              {startup.published ? 'Unpublish' : 'Publish'}
            </Button>
          </div>)}
      </div>

      <Card className="p-6">
        <StartupForm initial={startup} submitLabel={isEdit ? 'Save changes' : 'Create draft'} submitting={createStartup.isPending || updateStartup.isPending} onSubmit={handleSubmit}/>
      </Card>
    </DashboardLayout>);
}
