import axios from 'axios';
const TOKEN_KEY = 'innovatefund_token';
export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}
export function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}
export function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
}
export const axiosClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
});
axiosClient.interceptors.request.use((config) => {
    const token = getToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
let onUnauthorized = null;
export function registerUnauthorizedHandler(handler) {
    onUnauthorized = handler;
}
axiosClient.interceptors.response.use((response) => response, (error) => {
    if (error.response?.status === 401) {
        clearToken();
        onUnauthorized?.();
    }
    return Promise.reject(error);
});
