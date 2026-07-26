import { axiosClient } from './axiosClient';
export async function getEvents(upcomingOnly = true) {
    const { data } = await axiosClient.get('/events', { params: { upcomingOnly } });
    return data;
}
export async function createEvent(payload) {
    const { data } = await axiosClient.post('/events', payload);
    return data;
}
export async function deleteEvent(id) {
    await axiosClient.delete(`/events/${id}`);
}
