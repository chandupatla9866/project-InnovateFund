import { axiosClient } from './axiosClient';
export async function expressInterest(startupId) {
    await axiosClient.post(`/startups/${startupId}/interest`);
}
export async function withdrawInterest(startupId) {
    await axiosClient.delete(`/startups/${startupId}/interest`);
}
export async function getInterestedInvestors(startupId) {
    const { data } = await axiosClient.get(`/startups/${startupId}/interested-investors`);
    return data;
}
export async function acceptInterest(startupId, investorId) {
    await axiosClient.patch(`/startups/${startupId}/interested-investors/${investorId}/accept`);
}
export async function rejectInterest(startupId, investorId) {
    await axiosClient.patch(`/startups/${startupId}/interested-investors/${investorId}/reject`);
}
export async function getMyInterests() {
    const { data } = await axiosClient.get('/investors/me/interests');
    return data;
}
export async function getFounderInterestedInvestors() {
    const { data } = await axiosClient.get('/founders/me/interested-investors');
    return data;
}
