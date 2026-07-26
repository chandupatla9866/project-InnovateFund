import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as notificationApi from '../api/notificationApi';
import { useAuth } from './useAuth';
export function useNotifications(page = 0) {
    const { user } = useAuth();
    return useQuery({
        queryKey: ['notifications', page],
        queryFn: () => notificationApi.getNotifications(page),
        enabled: !!user,
        refetchInterval: 30_000,
    });
}
export function useUnreadCount() {
    const { user } = useAuth();
    return useQuery({
        queryKey: ['notifications', 'unread-count'],
        queryFn: notificationApi.getUnreadCount,
        enabled: !!user,
        refetchInterval: 30_000,
    });
}
export function useMarkNotificationRead() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => notificationApi.markAsRead(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['notifications'] });
        },
    });
}
export function useMarkAllNotificationsRead() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: notificationApi.markAllAsRead,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['notifications'] });
        },
    });
}
