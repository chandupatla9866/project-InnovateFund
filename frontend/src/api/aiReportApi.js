import { axiosClient } from './axiosClient';
export async function analyzeStartup(startupId) {
    const { data } = await axiosClient.post(`/startups/${startupId}/analyze`);
    return data;
}
export async function getReportHistory(startupId) {
    const { data } = await axiosClient.get(`/startups/${startupId}/reports`);
    return data;
}
export async function getLatestReport(startupId) {
    const { data } = await axiosClient.get(`/startups/${startupId}/reports/latest`);
    return data;
}
export async function getReportSummary(startupId) {
    const response = await axiosClient.get(`/startups/${startupId}/reports/summary`);
    return response.status === 204 ? null : response.data;
}
