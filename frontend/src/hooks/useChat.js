import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as chatApi from '../api/chatApi';
export function useConversations() {
    return useQuery({
        queryKey: ['chat', 'conversations'],
        queryFn: chatApi.getConversations,
        refetchInterval: 15_000,
    });
}
export function useMessages(otherUserId) {
    return useQuery({
        queryKey: ['chat', 'messages', otherUserId],
        queryFn: () => chatApi.getMessages(otherUserId),
        enabled: !!otherUserId,
        refetchInterval: 10_000,
    });
}
export function useSendMessage(otherUserId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (text) => chatApi.sendMessage(otherUserId, text),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['chat', 'messages', otherUserId] });
            queryClient.invalidateQueries({ queryKey: ['chat', 'conversations'] });
        },
    });
}
