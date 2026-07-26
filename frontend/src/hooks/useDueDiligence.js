import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as dueDiligenceApi from '../api/dueDiligenceApi';
export function useMyDueDiligenceStatus(startupId) {
    return useQuery({
        queryKey: ['dueDiligence', 'my-status', startupId],
        queryFn: () => dueDiligenceApi.getMyStatus(startupId),
        enabled: !!startupId,
    });
}
export function useDueDiligenceRequests(startupId) {
    return useQuery({
        queryKey: ['dueDiligence', 'requests', startupId],
        queryFn: () => dueDiligenceApi.getRequests(startupId),
        enabled: !!startupId,
    });
}
export function useDueDiligenceDocuments(startupId, enabled) {
    return useQuery({
        queryKey: ['dueDiligence', 'documents', startupId],
        queryFn: () => dueDiligenceApi.getDocuments(startupId),
        enabled: !!startupId && enabled,
    });
}
export function useRequestDueDiligenceAccess(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: () => dueDiligenceApi.requestAccess(startupId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dueDiligence', 'my-status', startupId] }),
    });
}
export function useApproveDueDiligenceRequest(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (requestId) => dueDiligenceApi.approveRequest(startupId, requestId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dueDiligence', 'requests', startupId] }),
    });
}
export function useRejectDueDiligenceRequest(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (requestId) => dueDiligenceApi.rejectRequest(startupId, requestId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dueDiligence', 'requests', startupId] }),
    });
}
export function useUploadDueDiligenceDocument(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => dueDiligenceApi.uploadDocument(startupId, payload),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dueDiligence', 'documents', startupId] }),
    });
}
export function useDeleteDueDiligenceDocument(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (documentId) => dueDiligenceApi.deleteDocument(startupId, documentId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dueDiligence', 'documents', startupId] }),
    });
}
