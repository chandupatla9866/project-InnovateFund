import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as meetingApi from '../api/meetingApi';
export function useMyMeetings() {
    return useQuery({ queryKey: ['meetings'], queryFn: meetingApi.getMyMeetings });
}
export function useRequestMeeting() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => meetingApi.requestMeeting(payload),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['meetings'] }),
    });
}
export function useAcceptMeeting() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => meetingApi.acceptMeeting(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['meetings'] }),
    });
}
export function useRejectMeeting() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => meetingApi.rejectMeeting(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['meetings'] }),
    });
}
export function useCancelMeeting() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => meetingApi.cancelMeeting(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['meetings'] }),
    });
}
