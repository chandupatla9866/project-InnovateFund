import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as eventApi from '../api/eventApi';
export function useEvents() {
    return useQuery({ queryKey: ['events'], queryFn: () => eventApi.getEvents() });
}
export function useCreateEvent() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => eventApi.createEvent(payload),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['events'] }),
    });
}
export function useDeleteEvent() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => eventApi.deleteEvent(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['events'] }),
    });
}
