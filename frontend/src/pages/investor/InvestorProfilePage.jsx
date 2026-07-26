import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { BadgeCheck, Clock, User as UserIcon } from 'lucide-react';
import { DashboardLayout } from '../../components/layout/DashboardLayout';
import { Card } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { TextArea } from '../../components/ui/TextArea';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Spinner } from '../../components/ui/Spinner';
import { useMyInvestorProfile, useUpdateInvestorProfile } from '../../hooks/useProfile';
export function InvestorProfilePage() {
    const { data: profile, isLoading, isError, refetch } = useMyInvestorProfile();
    const updateProfile = useUpdateInvestorProfile();
    const [fullName, setFullName] = useState('');
    const [bio, setBio] = useState('');
    const [firmName, setFirmName] = useState('');
    const [investmentInterests, setInvestmentInterests] = useState('');
    useEffect(() => {
        if (profile) {
            setFullName(profile.fullName);
            setBio(profile.bio ?? '');
            setFirmName(profile.firmName ?? '');
            setInvestmentInterests(profile.investmentInterests ?? '');
        }
    }, [profile]);
    const handleSubmit = (e) => {
        e.preventDefault();
        updateProfile.mutate({ fullName, bio, firmName, investmentInterests }, {
            onSuccess: () => toast.success('Profile updated'),
            onError: () => toast.error('Could not update profile'),
        });
    };
    return (<DashboardLayout>
      <div className="mx-auto max-w-xl">
        <div className="mb-6 flex items-center gap-2">
          <UserIcon className="size-5 text-brand-500"/>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Investor Profile</h1>
        </div>

        {isLoading ? (<Spinner />) : isError || !profile ? (<div className="flex flex-col items-center gap-3 py-10 text-center">
            <p className="text-sm text-slate-500 dark:text-slate-400">Couldn't load your profile.</p>
            <Button size="sm" variant="secondary" onClick={() => refetch()}>Try again</Button>
          </div>) : (<>
            <div className="mb-4 flex items-center gap-2">
              {profile.verified ? (<Badge tone="green">
                  <BadgeCheck className="size-3"/> Verified
                </Badge>) : (<Badge tone="amber">
                  <Clock className="size-3"/> Pending verification
                </Badge>)}
              <span className="text-sm text-slate-500 dark:text-slate-400">{profile.email}</span>
            </div>

            <Card className="p-6">
              <form onSubmit={handleSubmit} className="space-y-4">
                <Input label="Full name" required value={fullName} onChange={(e) => setFullName(e.target.value)}/>
                <Input label="Firm name" value={firmName} onChange={(e) => setFirmName(e.target.value)}/>
                <TextArea label="Bio" value={bio} onChange={(e) => setBio(e.target.value)}/>
                <Input label="Investment interests" placeholder="e.g. AgriTech, FinTech, Seed stage, ₹20L–₹2Cr" value={investmentInterests} onChange={(e) => setInvestmentInterests(e.target.value)}/>
                <Button type="submit" loading={updateProfile.isPending}>
                  Save changes
                </Button>
              </form>
            </Card>
          </>)}
      </div>
    </DashboardLayout>);
}
