import { useState } from 'react';
import { motion } from 'framer-motion';
import { Bookmark, Flag, Heart, MessageCircle, Rocket, Star, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { ReportModal } from '../ui/ReportModal';
import { CommentList } from './CommentList';
import { useDeletePost, useToggleLike } from '../../hooks/useFeed';
import { useAuth } from '../../hooks/useAuth';
import { useFollowStartup, useSaveStartup } from '../../hooks/useStartups';
import { useMyFollowing } from '../../hooks/useFollowing';
import { useMySavedStartups } from '../../hooks/useSavedStartups';
import { useExpressInterest, useMyInterests } from '../../hooks/useInterest';
import { cn } from '../../lib/cn';
function isVideoUrl(url) {
    return /\.(mp4|webm|mov|ogg)(\?.*)?$/i.test(url);
}
const typeLabels = {
    PRODUCT_LAUNCH: 'Product Launch',
    MILESTONE: 'Milestone',
    FUNDING_UPDATE: 'Funding Update',
    ACHIEVEMENT: 'Achievement',
    HIRING: 'Hiring',
    ANNOUNCEMENT: 'Announcement',
    INVESTMENT_OPPORTUNITY: 'Investment Opportunity',
    MARKET_INSIGHT: 'Market Insight',
    STARTUP_TIPS: 'Startup Tips',
    GENERAL: 'Update',
};
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
export function PostCard({ post, id, defaultOpenComments = false }) {
    const [showComments, setShowComments] = useState(defaultOpenComments);
    const [reportOpen, setReportOpen] = useState(false);
    const toggleLike = useToggleLike();
    const deletePost = useDeletePost();
    const { user } = useAuth();
    const isAuthor = user?.id === post.authorId;
    const isInvestor = user?.role === 'INVESTOR';
    const { data: following } = useMyFollowing(isInvestor && !!post.startupId);
    const { data: myInterests } = useMyInterests(isInvestor && !!post.startupId);
    const { data: savedStartups } = useMySavedStartups(isInvestor && !!post.startupId);
    const followMutation = useFollowStartup();
    const expressInterestMutation = useExpressInterest();
    const saveStartupMutation = useSaveStartup();
    const isFollowing = !!post.startupId && !!following?.some((f) => f.id === post.startupId);
    const isInterested = !!post.startupId && !!myInterests?.some((s) => s.id === post.startupId);
    const isSaved = !!post.startupId && !!savedStartups?.some((s) => s.id === post.startupId);
    const handleDelete = () => {
        if (!confirm('Delete this post?'))
            return;
        deletePost.mutate(post.id, {
            onSuccess: () => toast.success('Post deleted'),
            onError: () => toast.error('Could not delete post'),
        });
    };
    const toggleFollow = () => {
        if (!post.startupId)
            return;
        followMutation.mutate({ id: post.startupId, follow: !isFollowing }, {
            onSuccess: () => toast.success(isFollowing ? 'Unfollowed' : 'Now following'),
            onError: () => toast.error('Something went wrong'),
        });
    };
    const toggleInterest = () => {
        if (!post.startupId)
            return;
        expressInterestMutation.mutate({ id: post.startupId, express: !isInterested }, {
            onSuccess: () => toast.success(isInterested ? 'Interest withdrawn' : 'Interest expressed — the founder has been notified'),
            onError: () => toast.error('Something went wrong'),
        });
    };
    const toggleSave = () => {
        if (!post.startupId)
            return;
        saveStartupMutation.mutate({ id: post.startupId, saved: isSaved }, {
            onSuccess: () => toast.success(isSaved ? 'Removed from saved startups' : 'Saved to your list'),
            onError: () => toast.error('Something went wrong'),
        });
    };
    return (<motion.div id={id} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}>
      <Card className="space-y-3 p-5">
        <div className="flex items-start gap-3">
          <div className="flex size-10 shrink-0 items-center justify-center rounded-full bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-300">
            {post.startupLogoUrl ? (<img src={post.startupLogoUrl} alt="" className="size-full rounded-full object-cover"/>) : (<Rocket className="size-4.5"/>)}
          </div>
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <span className="font-semibold text-slate-900 dark:text-slate-100">{post.authorName}</span>
              {post.startupName && post.startupId && (<Link to={`/startups/${post.startupId}`} className="text-sm text-brand-600 hover:underline dark:text-brand-400">
                  @ {post.startupName}
                </Link>)}
              <Badge tone="brand">{typeLabels[post.type] ?? post.type}</Badge>
            </div>
            <p className="text-xs text-slate-400">{timeAgo(post.createdAt)}</p>
          </div>
          {isAuthor ? (<button onClick={handleDelete} aria-label="Delete post" className="shrink-0 rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-red-50 hover:text-red-500 dark:hover:bg-red-500/10">
              <Trash2 className="size-4"/>
            </button>) : (<button onClick={() => setReportOpen(true)} aria-label="Report post" className="shrink-0 rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800">
              <Flag className="size-4"/>
            </button>)}
        </div>

        <p className="whitespace-pre-wrap text-sm text-slate-700 dark:text-slate-300">{post.text}</p>

        {post.mediaUrl && (isVideoUrl(post.mediaUrl) ? (<video src={post.mediaUrl} controls className="max-h-80 w-full rounded-xl bg-black object-contain"/>) : (<img src={post.mediaUrl} alt="" className="max-h-80 w-full rounded-xl object-cover"/>))}

        {isInvestor && post.startupId && !isAuthor && (<div className="flex flex-wrap gap-2">
            <Button variant={isFollowing ? 'secondary' : 'primary'} size="sm" loading={followMutation.isPending} onClick={toggleFollow}>
              <Heart className={cn('size-4', isFollowing && 'fill-current')}/>
              {isFollowing ? 'Following' : 'Follow Startup'}
            </Button>
            <Button variant={isInterested ? 'secondary' : 'primary'} size="sm" loading={expressInterestMutation.isPending} onClick={toggleInterest}>
              <Star className={cn('size-4', isInterested && 'fill-current')}/>
              {isInterested ? 'Interested' : 'Express Interest'}
            </Button>
            <Button variant="ghost" size="sm" loading={saveStartupMutation.isPending} onClick={toggleSave} aria-label={isSaved ? 'Unsave startup' : 'Save startup'}>
              <Bookmark className={cn('size-4', isSaved && 'fill-brand-600 text-brand-600 dark:fill-brand-400 dark:text-brand-400')}/>
              {isSaved ? 'Saved' : 'Save'}
            </Button>
          </div>)}

        <div className="flex items-center gap-4 border-t border-slate-100 pt-3 text-sm text-slate-500 dark:border-slate-800 dark:text-slate-400">
          <button onClick={() => toggleLike.mutate({ id: post.id, liked: post.likedByMe })} className={cn('flex items-center gap-1.5 rounded-lg px-2 py-1 transition-colors hover:bg-slate-100 dark:hover:bg-slate-800', post.likedByMe && 'text-pink-500')}>
            <Heart className={cn('size-4', post.likedByMe && 'fill-pink-500')}/>
            {post.likeCount}
          </button>
          <button onClick={() => setShowComments((s) => !s)} className="flex items-center gap-1.5 rounded-lg px-2 py-1 transition-colors hover:bg-slate-100 dark:hover:bg-slate-800">
            <MessageCircle className="size-4"/>
            {post.commentCount}
          </button>
        </div>

        {showComments && <CommentList postId={post.id}/>}
      </Card>
      <ReportModal open={reportOpen} onClose={() => setReportOpen(false)} targetType="POST" targetId={post.id}/>
    </motion.div>);
}
