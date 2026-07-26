import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as aiReportApi from '../api/aiReportApi';
export function useAnalyzeStartup(startupId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: () => aiReportApi.analyzeStartup(startupId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['aiReports', startupId] });
        },
    });
}
export function useReportHistory(startupId) {
    return useQuery({
        queryKey: ['aiReports', startupId],
        queryFn: () => aiReportApi.getReportHistory(startupId),
        enabled: !!startupId,
    });
}
export function useLatestReport(startupId) {
    return useQuery({
        queryKey: ['aiReports', startupId, 'latest'],
        queryFn: () => aiReportApi.getLatestReport(startupId),
        enabled: !!startupId,
        retry: false,
    });
}
export function useReportSummary(startupId, enabled = true) {
    return useQuery({
        queryKey: ['aiReports', startupId, 'summary'],
        queryFn: () => aiReportApi.getReportSummary(startupId),
        enabled: !!startupId && enabled,
    });
}
