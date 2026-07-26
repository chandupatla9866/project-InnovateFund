import { axiosClient } from './axiosClient';
export async function getNotifications(page = 0, size = 20) {
    const { data } = await axiosClient.get('/notifications', { params: { page, size } });
    return data;
}
export async function getUnreadCount() {
    const { data } = await axiosClient.get('/notifications/unread-count');
    return data.count;
}
export async function markAsRead(id) {
    await axiosClient.patch(`/notifications/${id}/read`);
}
export async function markAllAsRead() {
    await axiosClient.patch('/notifications/read-all');
}
