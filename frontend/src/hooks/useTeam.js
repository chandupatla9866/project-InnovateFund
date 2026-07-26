import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as teamApi from '../api/teamApi';
export function useTeamMembers(startupId) {
    return useQuery({
        queryKey: ['team', startupId],
        queryFn: () => teamApi.getTeamMembers(startupId),
        enabled: !!startupId,
    });
}
export function useCreateTeamMember(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => teamApi.createTeamMember(startupId, payload),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['team', startupId] }),
    });
}
export function useDeleteTeamMember(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (memberId) => teamApi.deleteTeamMember(startupId, memberId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['team', startupId] }),
    });
}
