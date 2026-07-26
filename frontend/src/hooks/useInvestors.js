import { useQuery } from '@tanstack/react-query';
import * as investorApi from '../api/investorApi';
export function useFeaturedInvestors(limit = 6) {
    return useQuery({ queryKey: ['investors', 'featured', limit], queryFn: () => investorApi.getFeaturedInvestors(limit) });
}
