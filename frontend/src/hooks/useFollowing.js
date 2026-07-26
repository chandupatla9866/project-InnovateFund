import { useQuery } from '@tanstack/react-query';
import * as investorApi from '../api/investorApi';
export function useMyFollowing(enabled = true) {
    return useQuery({ queryKey: ['following'], queryFn: investorApi.getMyFollowing, enabled });
}
