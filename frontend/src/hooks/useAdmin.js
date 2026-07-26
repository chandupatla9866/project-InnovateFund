import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as adminApi from '../api/adminApi';
export function usePendingFounders() {
    return useQuery({ queryKey: ['admin', 'founders'], queryFn: () => adminApi.listFounders(false) });
}
export function usePendingInvestors() {
    return useQuery({ queryKey: ['admin', 'investors'], queryFn: () => adminApi.listInvestors(false) });
}
export function usePendingStartups() {
    return useQuery({ queryKey: ['admin', 'startups'], queryFn: () => adminApi.listStartupsAdmin(false) });
}
export function useVerifyFounder() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => adminApi.verifyFounder(id, true),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'founders'] }),
    });
}
export function useVerifyInvestor() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => adminApi.verifyInvestor(id, true),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'investors'] }),
    });
}
export function useVerifyStartup() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => adminApi.verifyStartupAdmin(id, true),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'startups'] }),
    });
}
