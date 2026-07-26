import { axiosClient } from './axiosClient';
export async function submitReport(payload) {
    const { data } = await axiosClient.post('/reports', payload);
    return data;
}
export async function getReports(status = 'PENDING') {
    const { data } = await axiosClient.get('/admin/reports', { params: { status } });
    return data;
}
export async function resolveReport(id) {
    const { data } = await axiosClient.patch(`/admin/reports/${id}/resolve`);
    return data;
}
export async function dismissReport(id) {
    const { data } = await axiosClient.patch(`/admin/reports/${id}/dismiss`);
    return data;
}
