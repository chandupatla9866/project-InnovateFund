import { axiosClient } from './axiosClient';
export async function listFounders(verified) {
    const { data } = await axiosClient.get('/admin/founders', { params: { verified } });
    return data;
}
export async function verifyFounder(id, verified) {
    const { data } = await axiosClient.patch(`/admin/founders/${id}/verify`, null, { params: { verified } });
    return data;
}
export async function listInvestors(verified) {
    const { data } = await axiosClient.get('/admin/investors', { params: { verified } });
    return data;
}
export async function verifyInvestor(id, verified) {
    const { data } = await axiosClient.patch(`/admin/investors/${id}/verify`, null, { params: { verified } });
    return data;
}
export async function listStartupsAdmin(verified) {
    const { data } = await axiosClient.get('/admin/startups', { params: { verified } });
    return data;
}
export async function verifyStartupAdmin(id, verified) {
    const { data } = await axiosClient.patch(`/admin/startups/${id}/verify`, null, { params: { verified } });
    return data;
}
