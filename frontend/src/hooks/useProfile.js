import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as founderApi from '../api/founderApi';
import * as investorApi from '../api/investorApi';
export function useMyFounderProfile(enabled = true) {
    return useQuery({ queryKey: ['profile', 'founder', 'me'], queryFn: founderApi.getMyFounderProfile, enabled });
}
export function useUpdateFounderProfile() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => founderApi.updateMyFounderProfile(payload),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['profile', 'founder', 'me'] }),
    });
}
export function useMyInvestorProfile(enabled = true) {
    return useQuery({ queryKey: ['profile', 'investor', 'me'], queryFn: investorApi.getMyInvestorProfile, enabled });
}
export function useUpdateInvestorProfile() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => investorApi.updateMyInvestorProfile(payload),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['profile', 'investor', 'me'] }),
    });
}
