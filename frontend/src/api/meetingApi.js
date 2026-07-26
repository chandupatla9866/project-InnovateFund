import { axiosClient } from './axiosClient';
export async function requestMeeting(payload) {
    const { data } = await axiosClient.post('/meetings', payload);
    return data;
}
export async function getMyMeetings() {
    const { data } = await axiosClient.get('/meetings/mine');
    return data;
}
export async function acceptMeeting(id) {
    const { data } = await axiosClient.patch(`/meetings/${id}/accept`);
    return data;
}
export async function rejectMeeting(id) {
    const { data } = await axiosClient.patch(`/meetings/${id}/reject`);
    return data;
}
export async function cancelMeeting(id) {
    const { data } = await axiosClient.patch(`/meetings/${id}/cancel`);
    return data;
}
