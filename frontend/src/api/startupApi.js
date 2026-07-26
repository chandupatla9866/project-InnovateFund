import { axiosClient } from './axiosClient';
export async function listPublishedStartups(params = {}) {
    const { data } = await axiosClient.get('/startups', { params });
    return data;
}
export async function searchStartups(q, page = 0, size = 20) {
    const { data } = await axiosClient.get('/startups/search', { params: { q, page, size } });
    return data;
}
export async function getTrendingStartups(limit = 10) {
    const { data } = await axiosClient.get('/startups/trending', { params: { limit } });
    return data;
}
export async function listMyStartups() {
    const { data } = await axiosClient.get('/startups/mine');
    return data;
}
export async function getStartup(id) {
    const { data } = await axiosClient.get(`/startups/${id}`);
    return data;
}
export async function createStartup(payload) {
    const { data } = await axiosClient.post('/startups', payload);
    return data;
}
export async function updateStartup(id, payload) {
    const { data } = await axiosClient.put(`/startups/${id}`, payload);
    return data;
}
export async function publishStartup(id) {
    const { data } = await axiosClient.patch(`/startups/${id}/publish`);
    return data;
}
export async function unpublishStartup(id) {
    const { data } = await axiosClient.patch(`/startups/${id}/unpublish`);
    return data;
}
export async function deleteStartup(id) {
    await axiosClient.delete(`/startups/${id}`);
}
export async function followStartup(id) {
    await axiosClient.post(`/startups/${id}/follow`);
}
export async function unfollowStartup(id) {
    await axiosClient.delete(`/startups/${id}/follow`);
}
export async function getFollowerCount(id) {
    const { data } = await axiosClient.get(`/startups/${id}/followers/count`);
    return data;
}
export async function saveStartup(id) {
    await axiosClient.post(`/startups/${id}/save`);
}
export async function unsaveStartup(id) {
    await axiosClient.delete(`/startups/${id}/save`);
}
export async function getMySavedStartups() {
    const { data } = await axiosClient.get('/investors/me/saved-startups');
    return data;
}
