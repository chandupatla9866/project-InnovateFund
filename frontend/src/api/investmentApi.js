import { axiosClient } from './axiosClient';
export async function recordInvestment(startupId, payload) {
    const { data } = await axiosClient.post(`/startups/${startupId}/investments`, payload);
    return data;
}
export async function getStartupInvestments(startupId) {
    const { data } = await axiosClient.get(`/startups/${startupId}/investments`);
    return data;
}
export async function getMyPortfolio() {
    const { data } = await axiosClient.get('/investors/me/portfolio');
    return data;
}
