import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import { AppRoutes } from './routes/AppRoutes';
import { useChatSocket } from './hooks/useChatSocket';
const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 30_000,
            refetchOnWindowFocus: false,
            // A 4xx (not found, forbidden, etc.) will never succeed by retrying — retrying it
            // just delays the error state for several seconds for no benefit. Only retry things
            // that might actually be transient (network hiccups, 5xx).
            retry: (failureCount, error) => {
                const status = error?.response?.status;
                if (status >= 400 && status < 500)
                    return false;
                return failureCount < 3;
            },
        },
    },
});
function ChatSocketBridge() {
    useChatSocket();
    return null;
}
function App() {
    return (<QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <ChatSocketBridge />
          <AppRoutes />
          <Toaster position="top-right"/>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>);
}
export default App;
