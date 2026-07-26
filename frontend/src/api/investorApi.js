import { axiosClient } from './axiosClient';
export async function getFeaturedInvestors(limit = 6) {
    const { data } = await axiosClient.get('/investors/featured', { params: { limit } });
    return data;
}
export async function getMyInvestorProfile() {
    const { data } = await axiosClient.get('/investors/me');
    return data;
}
export async function updateMyInvestorProfile(payload) {
    const { data } = await axiosClient.put('/investors/me', payload);
    return data;
}
export async function getMyFollowing() {
    const { data } = await axiosClient.get('/investors/me/following');
    return data;
}
