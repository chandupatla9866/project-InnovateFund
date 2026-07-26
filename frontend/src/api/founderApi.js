import { axiosClient } from './axiosClient';
export async function getMyFounderProfile() {
    const { data } = await axiosClient.get('/founders/me');
    return data;
}
export async function updateMyFounderProfile(payload) {
    const { data } = await axiosClient.put('/founders/me', payload);
    return data;
}
export async function getFounderProfile(id) {
    const { data } = await axiosClient.get(`/founders/${id}`);
    return data;
}
