import { useMutation } from '@tanstack/react-query';
import * as authApi from '../api/authApi';
export function useForgotPassword() {
    return useMutation({ mutationFn: (email) => authApi.forgotPassword(email) });
}
export function useResetPassword() {
    return useMutation({ mutationFn: ({ token, newPassword }) => authApi.resetPassword(token, newPassword) });
}
