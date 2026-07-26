import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as investmentApi from '../api/investmentApi';
// Payment confirmation happens via a backend job polling Razorpay, not a user action — the
// frontend has no other way to know it happened. Poll while anything is still PENDING so a
// paid investment flips to "Paid" on its own instead of needing a manual refresh; stop once
// nothing is pending so we're not polling forever for no reason.
function pollWhilePending(query) {
    const data = query.state.data;
    const hasPending = Array.isArray(data) && data.some((i) => i.status === 'PENDING');
    return hasPending ? 30_000 : false;
}
export function useStartupInvestments(startupId, enabled) {
    return useQuery({
        queryKey: ['investments', 'startup', startupId],
        queryFn: () => investmentApi.getStartupInvestments(startupId),
        enabled: !!startupId && enabled,
        refetchInterval: pollWhilePending,
    });
}
export function useMyPortfolio() {
    return useQuery({ queryKey: ['investments', 'portfolio'], queryFn: investmentApi.getMyPortfolio, refetchInterval: pollWhilePending });
}
export function useRecordInvestment(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => investmentApi.recordInvestment(startupId, payload),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['investments', 'startup', startupId] });
            queryClient.invalidateQueries({ queryKey: ['startups', startupId] });
        },
    });
}
