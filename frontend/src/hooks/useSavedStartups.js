import { useQuery } from '@tanstack/react-query';
import * as startupApi from '../api/startupApi';
export function useMySavedStartups(enabled = true) {
    return useQuery({ queryKey: ['savedStartups'], queryFn: startupApi.getMySavedStartups, enabled });
}
