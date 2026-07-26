import { useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import * as aiToolsApi from '../api/aiToolsApi';
import { loadAiToolCache, saveAiToolCache } from '../lib/aiToolsCache';

// Wraps useMutation so its last result (and the input that produced it) survives navigation
// and page reloads via localStorage — the button still needs to be pressed to regenerate,
// but the previous result stays visible until then instead of vanishing on remount.
function usePersistedMutation(cacheKey, mutationFn) {
    const [cached, setCached] = useState(() => (cacheKey ? loadAiToolCache(cacheKey) : undefined));
    useEffect(() => {
        setCached(cacheKey ? loadAiToolCache(cacheKey) : undefined);
    }, [cacheKey]);
    const mutation = useMutation({
        mutationFn,
        onSuccess: (data, variables) => {
            if (cacheKey) {
                const entry = { data, input: variables };
                saveAiToolCache(cacheKey, entry);
                setCached(entry);
            }
        },
    });
    return { ...mutation, data: mutation.data ?? cached?.data, cachedInput: cached?.input };
}

export function usePitchReview(startupId) {
    return usePersistedMutation(startupId ? `pitch-review:${startupId}` : null, () => aiToolsApi.reviewPitch(startupId));
}
export function usePitchImprovement(startupId) {
    return usePersistedMutation(startupId ? `pitch-improvement:${startupId}` : null, () => aiToolsApi.improvePitch(startupId));
}
export function useAskMentor(startupId) {
    return usePersistedMutation(startupId ? `mentor:${startupId}` : null, (question) => aiToolsApi.askMentor(startupId, question));
}
export function useMarketResearch(startupId) {
    return usePersistedMutation(startupId ? `market-research:${startupId}` : null, (query) => aiToolsApi.marketResearch(query));
}
export function useStartupMatches(startupId) {
    return useQuery({ queryKey: ['matches', 'startup', startupId], queryFn: () => aiToolsApi.matchesForStartup(startupId) });
}
export function useInvestorMatches(enabled) {
    return useQuery({ queryKey: ['matches', 'investor'], queryFn: aiToolsApi.matchesForInvestor, enabled });
}
export function useSummarizeMeeting(meetingId) {
    return useMutation({ mutationFn: (transcript) => aiToolsApi.summarizeMeeting(meetingId, transcript) });
}
export function useFraudFlags() {
    return useQuery({ queryKey: ['admin', 'fraud-flags'], queryFn: aiToolsApi.getFraudFlags });
}
