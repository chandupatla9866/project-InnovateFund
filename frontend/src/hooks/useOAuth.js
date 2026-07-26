import { useQuery } from '@tanstack/react-query';
import { isOAuthEnabled } from '../api/authApi';
export function useOAuthEnabled() {
    return useQuery({
        queryKey: ['oauth2', 'enabled'],
        queryFn: isOAuthEnabled,
        staleTime: Infinity,
        retry: false,
    });
}
