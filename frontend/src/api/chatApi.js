import { axiosClient } from './axiosClient';
export async function getConversations() {
    const { data } = await axiosClient.get('/chat/conversations');
    return data;
}
export async function getMessages(otherUserId) {
    const { data } = await axiosClient.get(`/chat/messages/${otherUserId}`);
    return data;
}
export async function sendMessage(recipientId, text) {
    const { data } = await axiosClient.post('/chat/messages', { recipientId, text });
    return data;
}
