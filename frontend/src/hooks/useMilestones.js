import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as milestoneApi from '../api/milestoneApi';
export function useMilestones(startupId) {
    return useQuery({
        queryKey: ['milestones', startupId],
        queryFn: () => milestoneApi.getMilestones(startupId),
        enabled: !!startupId,
    });
}
export function useCreateMilestone(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => milestoneApi.createMilestone(startupId, payload),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['milestones', startupId] }),
    });
}
export function useToggleMilestoneComplete(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (milestoneId) => milestoneApi.toggleMilestoneComplete(startupId, milestoneId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['milestones', startupId] });
            queryClient.invalidateQueries({ queryKey: ['posts', 'startup', startupId] });
            queryClient.invalidateQueries({ queryKey: ['feed'] });
        },
    });
}
export function useDeleteMilestone(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (milestoneId) => milestoneApi.deleteMilestone(startupId, milestoneId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['milestones', startupId] }),
    });
}
