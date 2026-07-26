import { createContext, useCallback, useEffect, useState } from 'react';
import * as authApi from '../api/authApi';
import { clearToken, getToken, registerUnauthorizedHandler, setToken } from '../api/axiosClient';
export const AuthContext = createContext(undefined);
export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const logout = useCallback(() => {
        clearToken();
        setUser(null);
    }, []);
    useEffect(() => {
        registerUnauthorizedHandler(() => setUser(null));
    }, []);
    useEffect(() => {
        const token = getToken();
        if (!token) {
            setIsLoading(false);
            return;
        }
        authApi
            .me()
            .then(setUser)
            .catch(() => clearToken())
            .finally(() => setIsLoading(false));
    }, []);
    const login = useCallback(async (email, password) => {
        const response = await authApi.login({ email, password });
        setToken(response.token);
        setUser(response.user);
        return response.user;
    }, []);
    const register = useCallback(async (payload) => {
        const response = await authApi.register(payload);
        setToken(response.token);
        setUser(response.user);
        return response.user;
    }, []);
    const loginWithToken = useCallback(async (token) => {
        setToken(token);
        const currentUser = await authApi.me();
        setUser(currentUser);
        return currentUser;
    }, []);
    return (<AuthContext.Provider value={{ user, isLoading, login, register, loginWithToken, logout }}>
      {children}
    </AuthContext.Provider>);
}
