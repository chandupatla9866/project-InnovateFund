import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as startupApi from '../api/startupApi';
export function usePublishedStartups(params = {}) {
    return useQuery({
        queryKey: ['startups', 'published', params],
        queryFn: () => startupApi.listPublishedStartups(params),
    });
}
export function useStartupSearch(query, page = 0, enabled = true) {
    return useQuery({
        queryKey: ['startups', 'search', query, page],
        queryFn: () => startupApi.searchStartups(query, page),
        enabled: enabled && query.trim().length > 0,
    });
}
export function useTrendingStartups(limit = 10) {
    return useQuery({ queryKey: ['startups', 'trending', limit], queryFn: () => startupApi.getTrendingStartups(limit) });
}
export function useMyStartups(enabled = true) {
    return useQuery({
        queryKey: ['startups', 'mine'],
        queryFn: startupApi.listMyStartups,
        enabled,
    });
}
export function useStartup(id, options = {}) {
    return useQuery({
        queryKey: ['startups', id],
        queryFn: () => startupApi.getStartup(id),
        enabled: !!id,
        ...options,
    });
}
export function useCreateStartup() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => startupApi.createStartup(payload),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['startups', 'mine'] });
        },
    });
}
export function useUpdateStartup(id) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => startupApi.updateStartup(id, payload),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['startups', 'mine'] });
            queryClient.invalidateQueries({ queryKey: ['startups', id] });
        },
    });
}
export function usePublishStartup() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => startupApi.publishStartup(id),
        onSuccess: (_data, id) => {
            queryClient.invalidateQueries({ queryKey: ['startups', 'mine'] });
            queryClient.invalidateQueries({ queryKey: ['startups', id] });
            queryClient.invalidateQueries({ queryKey: ['startups', 'published'] });
        },
    });
}
export function useUnpublishStartup() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => startupApi.unpublishStartup(id),
        onSuccess: (_data, id) => {
            queryClient.invalidateQueries({ queryKey: ['startups', 'mine'] });
            queryClient.invalidateQueries({ queryKey: ['startups', id] });
            queryClient.invalidateQueries({ queryKey: ['startups', 'published'] });
        },
    });
}
export function useFollowStartup() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, follow }) => follow ? startupApi.followStartup(id) : startupApi.unfollowStartup(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['following'] });
        },
    });
}
export function useSaveStartup() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, saved }) => saved ? startupApi.unsaveStartup(id) : startupApi.saveStartup(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['savedStartups'] });
        },
    });
}
