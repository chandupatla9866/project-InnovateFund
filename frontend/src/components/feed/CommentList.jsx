import { useState } from 'react';
import { Send, Trash2 } from 'lucide-react';
import { useAddComment, useComments, useDeleteComment } from '../../hooks/useFeed';
import { useAuth } from '../../hooks/useAuth';
import { Spinner } from '../ui/Spinner';
export function CommentList({ postId }) {
    const { data: comments, isLoading } = useComments(postId);
    const addComment = useAddComment(postId);
    const deleteComment = useDeleteComment(postId);
    const { user } = useAuth();
    const [text, setText] = useState('');
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!text.trim())
            return;
        addComment.mutate(text.trim(), { onSuccess: () => setText('') });
    };
    return (<div className="space-y-3 border-t border-slate-100 pt-3 dark:border-slate-800">
      {isLoading ? (<Spinner />) : (comments?.map((c) => (<div key={c.id} className="group flex items-start justify-between gap-2 rounded-xl bg-slate-50 px-3 py-2 text-sm dark:bg-slate-800/60">
            <p>
              <span className="font-medium text-slate-800 dark:text-slate-200">{c.authorName}</span>{' '}
              <span className="text-slate-600 dark:text-slate-400">{c.text}</span>
            </p>
            {user?.id === c.authorId && (<button onClick={() => deleteComment.mutate(c.id)} aria-label="Delete comment" className="shrink-0 rounded-lg p-1 text-slate-400 opacity-0 transition-opacity hover:text-red-500 group-hover:opacity-100">
                <Trash2 className="size-3.5"/>
              </button>)}
          </div>)))}
      <form onSubmit={handleSubmit} className="flex items-center gap-2">
        <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Ask about revenue, customers, valuation..." className="flex-1 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 dark:border-slate-700 dark:bg-slate-900"/>
        <button type="submit" disabled={addComment.isPending} className="rounded-xl bg-brand-500 p-2 text-white transition-colors hover:bg-brand-600 disabled:opacity-50">
          <Send className="size-4"/>
        </button>
      </form>
    </div>);
}
