import { useQuery } from '@tanstack/react-query';
import * as analyticsApi from '../api/analyticsApi';
export function useFounderAnalytics() {
    return useQuery({ queryKey: ['analytics', 'founder'], queryFn: analyticsApi.getFounderAnalytics });
}
export function useInvestorAnalytics() {
    return useQuery({ queryKey: ['analytics', 'investor'], queryFn: analyticsApi.getInvestorAnalytics });
}
export function usePlatformAnalytics() {
    return useQuery({ queryKey: ['analytics', 'platform'], queryFn: analyticsApi.getPlatformAnalytics });
}
