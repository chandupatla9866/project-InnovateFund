import { axiosClient } from './axiosClient';
export async function uploadFile(file, folder) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('folder', folder);
    const { data } = await axiosClient.post('/uploads', formData);
    return data.url;
}
