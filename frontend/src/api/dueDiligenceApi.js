import { axiosClient } from './axiosClient';
export async function requestAccess(startupId) {
    const { data } = await axiosClient.post(`/startups/${startupId}/due-diligence/request`);
    return data;
}
export async function getMyStatus(startupId) {
    const response = await axiosClient.get(`/startups/${startupId}/due-diligence/my-status`);
    return response.status === 204 ? null : response.data;
}
export async function getRequests(startupId) {
    const { data } = await axiosClient.get(`/startups/${startupId}/due-diligence/requests`);
    return data;
}
export async function approveRequest(startupId, requestId) {
    const { data } = await axiosClient.patch(`/startups/${startupId}/due-diligence/requests/${requestId}/approve`);
    return data;
}
export async function rejectRequest(startupId, requestId) {
    const { data } = await axiosClient.patch(`/startups/${startupId}/due-diligence/requests/${requestId}/reject`);
    return data;
}
export async function getDocuments(startupId) {
    const { data } = await axiosClient.get(`/startups/${startupId}/due-diligence/documents`);
    return data;
}
export async function uploadDocument(startupId, payload) {
    const { data } = await axiosClient.post(`/startups/${startupId}/due-diligence/documents`, payload);
    return data;
}
export async function deleteDocument(startupId, documentId) {
    await axiosClient.delete(`/startups/${startupId}/due-diligence/documents/${documentId}`);
}
