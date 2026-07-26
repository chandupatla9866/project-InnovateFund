import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { isAxiosError } from 'axios';
import toast from 'react-hot-toast';
import { ArrowLeft, MessageCircle, Send, User as UserIcon } from 'lucide-react';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { EmptyState } from '../components/ui/EmptyState';
import { Spinner } from '../components/ui/Spinner';
import { useConversations, useMessages, useSendMessage } from '../hooks/useChat';
import { useAuth } from '../hooks/useAuth';
import { cn } from '../lib/cn';
function timeAgo(iso) {
    const diffMs = Date.now() - new Date(iso).getTime();
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 1)
        return 'just now';
    if (minutes < 60)
        return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24)
        return `${hours}h ago`;
    return `${Math.floor(hours / 24)}d ago`;
}
export function ChatPage() {
    const { userId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();
    const { user } = useAuth();
    const { data: conversations, isLoading: loadingConversations } = useConversations();
    const { data: messages, isLoading: loadingMessages } = useMessages(userId);
    const sendMessage = useSendMessage(userId ?? '');
    const [text, setText] = useState('');
    const bottomRef = useRef(null);
    const lastCountRef = useRef(0);
    const stateName = location.state?.name;
    const activeConversation = conversations?.find((c) => c.counterpartId === userId);
    const counterpartName = activeConversation?.counterpartName ?? stateName ?? 'Conversation';
    // Only auto-scroll when a new message actually arrives, not on every background poll —
    // otherwise the view yanks to the bottom every 10s even while the user is reading history.
    useEffect(() => {
        const count = messages?.length ?? 0;
        if (count !== lastCountRef.current) {
            bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
            lastCountRef.current = count;
        }
    }, [messages]);
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!text.trim() || !userId)
            return;
        sendMessage.mutate(text.trim(), {
            onSuccess: () => setText(''),
            onError: (error) => {
                const message = isAxiosError(error) && error.response?.status === 403
                    ? error.response.data?.message ?? 'Chat is not open with this person yet'
                    : 'Could not send message';
                toast.error(message);
            },
        });
    };
    return (<DashboardLayout>
      <div className="flex h-[calc(100vh-8rem)] overflow-hidden rounded-2xl border border-slate-200 dark:border-slate-800">
        <div className={cn('w-full shrink-0 overflow-y-auto border-r border-slate-200 dark:border-slate-800 md:max-w-xs', userId && 'hidden md:block')}>
          <div className="border-b border-slate-100 px-4 py-3 dark:border-slate-800">
            <h2 className="font-semibold text-slate-900 dark:text-slate-100">Messages</h2>
          </div>
          {loadingConversations ? (<Spinner />) : !conversations || conversations.length === 0 ? (<p className="p-6 text-center text-sm text-slate-400">No conversations yet</p>) : (conversations.map((c) => (<button key={c.counterpartId} onClick={() => navigate(`/chat/${c.counterpartId}`, { state: { name: c.counterpartName } })} className={cn('flex w-full items-center gap-3 border-b border-slate-50 px-4 py-3 text-left transition-colors hover:bg-slate-50 dark:border-slate-800/60 dark:hover:bg-slate-800/60', c.counterpartId === userId && 'bg-brand-50 dark:bg-brand-500/10')}>
                <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-brand-100 text-brand-600 dark:bg-brand-500/15 dark:text-brand-300">
                  <UserIcon className="size-4"/>
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between">
                    <p className="truncate text-sm font-medium text-slate-800 dark:text-slate-200">{c.counterpartName}</p>
                    {c.unreadCount > 0 && (<span className="ml-1 flex size-4 shrink-0 items-center justify-center rounded-full bg-pink-500 text-[10px] font-semibold text-white">
                        {c.unreadCount}
                      </span>)}
                  </div>
                  <p className="truncate text-xs text-slate-400">{c.lastMessage}</p>
                </div>
              </button>)))}
        </div>

        <div className={cn('flex flex-1 flex-col', !userId && 'hidden md:flex')}>
          {!userId ? (<div className="flex flex-1 items-center justify-center">
              <EmptyState icon={MessageCircle} title="Select a conversation" description="Choose a conversation from the list to start chatting."/>
            </div>) : (<>
              <div className="flex items-center gap-2 border-b border-slate-100 px-4 py-3 dark:border-slate-800">
                <button onClick={() => navigate('/chat')} aria-label="Back to conversations" className="rounded-lg p-1 text-slate-500 hover:bg-slate-100 md:hidden dark:text-slate-400 dark:hover:bg-slate-800">
                  <ArrowLeft className="size-4.5"/>
                </button>
                <h3 className="font-semibold text-slate-900 dark:text-slate-100">{counterpartName}</h3>
              </div>
              <div className="flex-1 space-y-3 overflow-y-auto p-4">
                {loadingMessages ? (<Spinner />) : !messages || messages.length === 0 ? (<p className="text-center text-sm text-slate-400">Say hello 👋</p>) : (messages.map((m) => {
                const mine = m.senderId === user?.id;
                return (<div key={m.id} className={cn('flex', mine ? 'justify-end' : 'justify-start')}>
                        <div className={cn('max-w-xs rounded-2xl px-4 py-2 text-sm', mine
                        ? 'gradient-brand text-white'
                        : 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300')}>
                          <p className="whitespace-pre-wrap">{m.text}</p>
                          <p className={cn('mt-1 text-[10px]', mine ? 'text-white/70' : 'text-slate-400')}>
                            {timeAgo(m.createdAt)}
                          </p>
                        </div>
                      </div>);
            }))}
                <div ref={bottomRef}/>
              </div>
              <form onSubmit={handleSubmit} className="flex items-center gap-2 border-t border-slate-100 p-3 dark:border-slate-800">
                <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Type a message..." className="flex-1 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 dark:border-slate-700 dark:bg-slate-900"/>
                <button type="submit" disabled={sendMessage.isPending} className="rounded-xl bg-brand-500 p-2.5 text-white transition-colors hover:bg-brand-600 disabled:opacity-50">
                  <Send className="size-4"/>
                </button>
              </form>
            </>)}
        </div>
      </div>
    </DashboardLayout>);
}
