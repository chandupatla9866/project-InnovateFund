import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as reportApi from '../api/reportApi';
export function usePendingReports() {
    return useQuery({ queryKey: ['admin', 'reports'], queryFn: () => reportApi.getReports('PENDING') });
}
export function useSubmitReport() {
    return useMutation({ mutationFn: (payload) => reportApi.submitReport(payload) });
}
export function useResolveReport() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => reportApi.resolveReport(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'reports'] }),
    });
}
export function useDismissReport() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => reportApi.dismissReport(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'reports'] }),
    });
}
