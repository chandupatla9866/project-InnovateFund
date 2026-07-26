import { axiosClient } from './axiosClient';
export async function reviewPitch(startupId) {
    const { data } = await axiosClient.post(`/startups/${startupId}/pitch-review`);
    return data;
}
export async function improvePitch(startupId) {
    const { data } = await axiosClient.post(`/startups/${startupId}/pitch-review/improve`);
    return data;
}
export async function askMentor(startupId, question) {
    const { data } = await axiosClient.post(`/startups/${startupId}/mentor/ask`, { question });
    return data;
}
export async function marketResearch(query) {
    const { data } = await axiosClient.post('/ai/market-research', { query });
    return data;
}
export async function matchesForStartup(startupId) {
    const { data } = await axiosClient.get(`/startups/${startupId}/matches`);
    return data;
}
export async function matchesForInvestor() {
    const { data } = await axiosClient.get('/investors/me/matches');
    return data;
}
export async function summarizeMeeting(meetingId, transcript) {
    const { data } = await axiosClient.post(`/meetings/${meetingId}/summarize`, { transcript });
    return data;
}
export async function getMeetingSummary(meetingId) {
    const { data } = await axiosClient.get(`/meetings/${meetingId}/summary`);
    return data;
}
export async function getFraudFlags() {
    const { data } = await axiosClient.get('/admin/fraud-flags');
    return data;
}
