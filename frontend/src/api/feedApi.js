import { axiosClient } from './axiosClient';
export async function getFeed(page = 0, size = 20) {
    const { data } = await axiosClient.get('/posts/feed', { params: { page, size } });
    return data;
}
export async function getPostsByStartup(startupId) {
    const { data } = await axiosClient.get(`/posts/startup/${startupId}`);
    return data;
}
export async function getTrending(limit = 10) {
    const { data } = await axiosClient.get('/posts/trending', { params: { limit } });
    return data;
}
export async function createPost(payload) {
    const { data } = await axiosClient.post('/posts', payload);
    return data;
}
export async function deletePost(id) {
    await axiosClient.delete(`/posts/${id}`);
}
export async function likePost(id) {
    await axiosClient.post(`/posts/${id}/like`);
}
export async function unlikePost(id) {
    await axiosClient.delete(`/posts/${id}/like`);
}
export async function getComments(postId) {
    const { data } = await axiosClient.get(`/posts/${postId}/comments`);
    return data;
}
export async function addComment(postId, text) {
    const { data } = await axiosClient.post(`/posts/${postId}/comments`, { text });
    return data;
}
export async function deleteComment(id) {
    await axiosClient.delete(`/comments/${id}`);
}
