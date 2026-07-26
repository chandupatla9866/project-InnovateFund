import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import { useQueryClient } from '@tanstack/react-query';
import { getToken } from '../api/axiosClient';
import { useAuth } from './useAuth';
function wsUrl() {
    const apiBase = import.meta.env.VITE_API_BASE_URL;
    return apiBase.replace(/^http/, 'ws').replace(/\/api\/?$/, '') + '/ws';
}
/** Connects to the STOMP WebSocket and keeps React Query chat caches fresh on incoming messages. */
export function useChatSocket() {
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const clientRef = useRef(null);
    useEffect(() => {
        if (!user)
            return;
        const token = getToken();
        if (!token)
            return;
        const client = new Client({
            brokerURL: wsUrl(),
            connectHeaders: { Authorization: `Bearer ${token}` },
            reconnectDelay: 4000,
            onConnect: () => {
                client.subscribe('/user/queue/messages', (frame) => {
                    const message = JSON.parse(frame.body);
                    const counterpartId = message.senderId === user.id ? message.recipientId : message.senderId;
                    queryClient.invalidateQueries({ queryKey: ['chat', 'messages', counterpartId] });
                    queryClient.invalidateQueries({ queryKey: ['chat', 'conversations'] });
                });
            },
        });
        client.activate();
        clientRef.current = client;
        return () => {
            client.deactivate();
            clientRef.current = null;
        };
    }, [user, queryClient]);
}
