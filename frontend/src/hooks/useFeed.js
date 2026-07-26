import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as feedApi from '../api/feedApi';
export function useFeed(page = 0) {
    return useQuery({
        queryKey: ['feed', page],
        queryFn: () => feedApi.getFeed(page),
    });
}
export function useTrending() {
    return useQuery({ queryKey: ['feed', 'trending'], queryFn: () => feedApi.getTrending() });
}
export function useStartupPosts(startupId) {
    return useQuery({
        queryKey: ['posts', 'startup', startupId],
        queryFn: () => feedApi.getPostsByStartup(startupId),
        enabled: !!startupId,
    });
}
export function useCreatePost() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload) => feedApi.createPost(payload),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['feed'] });
        },
    });
}
export function useToggleLike() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, liked }) => liked ? feedApi.unlikePost(id) : feedApi.likePost(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['feed'] });
        },
    });
}
export function useComments(postId) {
    return useQuery({
        queryKey: ['comments', postId],
        queryFn: () => feedApi.getComments(postId),
        enabled: !!postId,
    });
}
export function useAddComment(postId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (text) => feedApi.addComment(postId, text),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['comments', postId] });
            queryClient.invalidateQueries({ queryKey: ['feed'] });
        },
    });
}
export function useDeletePost() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => feedApi.deletePost(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['feed'] });
            queryClient.invalidateQueries({ queryKey: ['posts', 'startup'] });
        },
    });
}
export function useDeleteComment(postId) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => feedApi.deleteComment(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['comments', postId] });
            queryClient.invalidateQueries({ queryKey: ['feed'] });
        },
    });
}
