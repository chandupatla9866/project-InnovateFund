import { axiosClient } from './axiosClient';
export async function getMilestones(startupId) {
    const { data } = await axiosClient.get(`/startups/${startupId}/milestones`);
    return data;
}
export async function createMilestone(startupId, payload) {
    const { data } = await axiosClient.post(`/startups/${startupId}/milestones`, payload);
    return data;
}
export async function updateMilestone(startupId, milestoneId, payload) {
    const { data } = await axiosClient.put(`/startups/${startupId}/milestones/${milestoneId}`, payload);
    return data;
}
export async function toggleMilestoneComplete(startupId, milestoneId) {
    const { data } = await axiosClient.patch(`/startups/${startupId}/milestones/${milestoneId}/complete`);
    return data;
}
export async function deleteMilestone(startupId, milestoneId) {
    await axiosClient.delete(`/startups/${startupId}/milestones/${milestoneId}`);
}
