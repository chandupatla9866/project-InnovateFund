import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as interestApi from '../api/interestApi';
export function useMyInterests(enabled = true) {
    return useQuery({ queryKey: ['interests', 'mine'], queryFn: interestApi.getMyInterests, enabled });
}
export function useInterestedInvestors(startupId, enabled) {
    return useQuery({
        queryKey: ['interests', 'startup', startupId],
        queryFn: () => interestApi.getInterestedInvestors(startupId),
        enabled: !!startupId && enabled,
    });
}
export function useExpressInterest() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, express }) => express ? interestApi.expressInterest(id) : interestApi.withdrawInterest(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['interests'] });
            queryClient.invalidateQueries({ queryKey: ['startups'] });
        },
    });
}
export function useAcceptInterest(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (investorId) => interestApi.acceptInterest(startupId, investorId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['interests', 'startup', startupId] }),
    });
}
export function useRejectInterest(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (investorId) => interestApi.rejectInterest(startupId, investorId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['interests', 'startup', startupId] }),
    });
}
// Founder-wide variants for the consolidated Investors page, where each row can belong to a
// different startup — unlike useAcceptInterest/useRejectInterest, startupId isn't fixed at hook creation.
export function useFounderInterestedInvestors() {
    return useQuery({ queryKey: ['interests', 'founder'], queryFn: interestApi.getFounderInterestedInvestors });
}
export function useAcceptInterestAny() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ startupId, investorId }) => interestApi.acceptInterest(startupId, investorId),
        onSuccess: (_data, { startupId }) => {
            queryClient.invalidateQueries({ queryKey: ['interests', 'startup', startupId] });
            queryClient.invalidateQueries({ queryKey: ['interests', 'founder'] });
        },
    });
}
export function useRejectInterestAny() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ startupId, investorId }) => interestApi.rejectInterest(startupId, investorId),
        onSuccess: (_data, { startupId }) => {
            queryClient.invalidateQueries({ queryKey: ['interests', 'startup', startupId] });
            queryClient.invalidateQueries({ queryKey: ['interests', 'founder'] });
        },
    });
}
