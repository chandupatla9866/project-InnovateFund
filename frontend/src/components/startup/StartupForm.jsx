import { useState } from 'react';
import { Input } from '../ui/Input';
import { TextArea } from '../ui/TextArea';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';
import { FileUploadField } from '../ui/FileUploadField';
const stages = ['IDEA', 'MVP', 'EARLY_TRACTION', 'GROWTH', 'SCALING'];
function toFormValues(startup) {
    if (!startup) {
        return { name: '' };
    }
    return {
        name: startup.name,
        logoUrl: startup.logoUrl ?? '',
        coverImageUrl: startup.coverImageUrl ?? '',
        industry: startup.industry ?? '',
        country: startup.country ?? '',
        stage: startup.stage ?? undefined,
        problem: startup.problem ?? '',
        solution: startup.solution ?? '',
        businessModel: startup.businessModel ?? '',
        revenueModel: startup.revenueModel ?? '',
        targetAudience: startup.targetAudience ?? '',
        market: startup.market ?? '',
        competitors: startup.competitors ?? '',
        fundingGoal: startup.fundingGoal ?? undefined,
        pitchDeckUrl: startup.pitchDeckUrl ?? '',
        demoVideoUrl: startup.demoVideoUrl ?? '',
        equityOffered: startup.equityOffered ?? undefined,
        websiteUrl: startup.websiteUrl ?? '',
        socialLinks: startup.socialLinks ?? '',
    };
}
export function StartupForm({ initial, submitting, submitLabel, onSubmit }) {
    const [values, setValues] = useState(() => toFormValues(initial));
    const update = (key, value) => setValues((v) => ({ ...v, [key]: value }));
    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(values);
    };
    return (<form onSubmit={handleSubmit} className="space-y-8">
      <section className="grid gap-4 sm:grid-cols-2">
        <Input label="Startup name" required value={values.name} onChange={(e) => update('name', e.target.value)}/>
        <Input label="Industry" placeholder="e.g. AgriTech, FinTech" value={values.industry ?? ''} onChange={(e) => update('industry', e.target.value)}/>
        <Input label="Country" placeholder="e.g. India" value={values.country ?? ''} onChange={(e) => update('country', e.target.value)}/>
        <Select label="Stage" value={values.stage ?? ''} onChange={(e) => update('stage', (e.target.value || undefined))}>
          <option value="">Select stage</option>
          {stages.map((s) => (<option key={s} value={s}>
              {s.replace('_', ' ')}
            </option>))}
        </Select>
        <Input label="Funding goal (₹)" type="number" min="0" value={values.fundingGoal ?? ''} onChange={(e) => update('fundingGoal', e.target.value ? Number(e.target.value) : undefined)}/>
        <Input label="Equity offered (%)" type="number" min="0" max="100" step="0.1" value={values.equityOffered ?? ''} onChange={(e) => update('equityOffered', e.target.value ? Number(e.target.value) : undefined)}/>
        <Input label="Website" placeholder="https://yourstartup.com" value={values.websiteUrl ?? ''} onChange={(e) => update('websiteUrl', e.target.value)}/>
        <Input label="Social links" placeholder="LinkedIn, Twitter/X, Instagram — comma-separated" value={values.socialLinks ?? ''} onChange={(e) => update('socialLinks', e.target.value)}/>
      </section>

      <section className="grid gap-4 sm:grid-cols-2">
        <FileUploadField label="Logo" value={values.logoUrl ?? ''} onChange={(url) => update('logoUrl', url)} folder="startup-logos" accept="image/*" preview="image"/>
        <FileUploadField label="Cover image" value={values.coverImageUrl ?? ''} onChange={(url) => update('coverImageUrl', url)} folder="startup-covers" accept="image/*" preview="image"/>
      </section>

      <section className="space-y-4">
        <TextArea label="Problem" placeholder="What problem are you solving, and for whom?" value={values.problem ?? ''} onChange={(e) => update('problem', e.target.value)}/>
        <TextArea label="Solution" placeholder="How does your product solve it?" value={values.solution ?? ''} onChange={(e) => update('solution', e.target.value)}/>
        <TextArea label="Business model" placeholder="How do you make money?" value={values.businessModel ?? ''} onChange={(e) => update('businessModel', e.target.value)}/>
        <TextArea label="Revenue model" placeholder="Pricing, tiers, unit economics" value={values.revenueModel ?? ''} onChange={(e) => update('revenueModel', e.target.value)}/>
        <TextArea label="Target audience" value={values.targetAudience ?? ''} onChange={(e) => update('targetAudience', e.target.value)}/>
        <TextArea label="Market" placeholder="Market size, TAM/SAM/SOM, growth rate" value={values.market ?? ''} onChange={(e) => update('market', e.target.value)}/>
        <TextArea label="Competitors" placeholder="Comma-separated list of named competitors" value={values.competitors ?? ''} onChange={(e) => update('competitors', e.target.value)}/>
      </section>

      <section className="grid gap-4 sm:grid-cols-2">
        <FileUploadField label="Pitch deck" value={values.pitchDeckUrl ?? ''} onChange={(url) => update('pitchDeckUrl', url)} folder="pitch-decks" accept="application/pdf"/>
        <FileUploadField label="Demo video" value={values.demoVideoUrl ?? ''} onChange={(url) => update('demoVideoUrl', url)} folder="demo-videos" accept="video/*"/>
      </section>

      <Button type="submit" loading={submitting} size="lg">
        {submitLabel}
      </Button>
    </form>);
}
