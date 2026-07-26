import { axiosClient } from './axiosClient';
export async function register(payload) {
    const { data } = await axiosClient.post('/auth/register', payload);
    return data;
}
export async function login(payload) {
    const { data } = await axiosClient.post('/auth/login', payload);
    return data;
}
export async function me() {
    const { data } = await axiosClient.get('/auth/me');
    return data;
}
export async function isOAuthEnabled() {
    const { data } = await axiosClient.get('/auth/oauth2/enabled');
    return data.enabled;
}
export function oauthStartUrl(role) {
    return `${import.meta.env.VITE_API_BASE_URL}/auth/oauth2/start?role=${role}`;
}
export async function forgotPassword(email) {
    await axiosClient.post('/auth/forgot-password', { email });
}
export async function resetPassword(token, newPassword) {
    await axiosClient.post('/auth/reset-password', { token, newPassword });
}
