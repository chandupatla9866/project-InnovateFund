import { axiosClient } from './axiosClient';
export async function getTeamMembers(startupId) {
    const { data } = await axiosClient.get(`/startups/${startupId}/team`);
    return data;
}
export async function createTeamMember(startupId, payload) {
    const { data } = await axiosClient.post(`/startups/${startupId}/team`, payload);
    return data;
}
export async function updateTeamMember(startupId, memberId, payload) {
    const { data } = await axiosClient.put(`/startups/${startupId}/team/${memberId}`, payload);
    return data;
}
export async function deleteTeamMember(startupId, memberId) {
    await axiosClient.delete(`/startups/${startupId}/team/${memberId}`);
}
