import { axiosClient } from './axiosClient';
export async function getFounderAnalytics() {
    const { data } = await axiosClient.get('/founders/me/analytics');
    return data;
}
export async function getInvestorAnalytics() {
    const { data } = await axiosClient.get('/investors/me/analytics');
    return data;
}
export async function getPlatformAnalytics() {
    const { data } = await axiosClient.get('/admin/analytics');
    return data;
}
