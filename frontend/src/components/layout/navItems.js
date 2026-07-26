import { BarChart3, Bell, Bookmark, Briefcase, Calendar, CalendarDays, Compass, Heart, LayoutDashboard, MessageCircle, PlusCircle, Rss, ShieldCheck, Star, UserCircle, } from 'lucide-react';
const founderItems = [
    { to: '/founder/dashboard', label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: '/founder/startups/new', label: 'New Startup', icon: PlusCircle },
    { to: '/founder/investors', label: 'Investors', icon: Star },
    { to: '/feed', label: 'Startup Feed', icon: Rss },
    { to: '/notifications', label: 'Notifications', icon: Bell },
    { to: '/chat', label: 'Messages', icon: MessageCircle },
    { to: '/meetings', label: 'Meetings', icon: Calendar },
    { to: '/events', label: 'Events', icon: CalendarDays },
    { to: '/founder/analytics', label: 'Analytics', icon: BarChart3 },
    { to: '/founder/profile', label: 'Profile', icon: UserCircle },
];
const investorItems = [
    { to: '/investor/dashboard', label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: '/investor/browse', label: 'Discover Startups', icon: Compass },
    { to: '/investor/dashboard#following', label: 'Following', icon: Heart },
    { to: '/investor/portfolio', label: 'Portfolio', icon: Briefcase },
    { to: '/investor/saved-startups', label: 'Saved Startups', icon: Bookmark },
    { to: '/feed', label: 'Startup Feed', icon: Rss },
    { to: '/notifications', label: 'Notifications', icon: Bell },
    { to: '/chat', label: 'Messages', icon: MessageCircle },
    { to: '/meetings', label: 'Meetings', icon: Calendar },
    { to: '/events', label: 'Events', icon: CalendarDays },
    { to: '/investor/analytics', label: 'Analytics', icon: BarChart3 },
    { to: '/investor/profile', label: 'Profile', icon: UserCircle },
];
const adminItems = [
    { to: '/admin/dashboard', label: 'Verification Queue', icon: ShieldCheck, end: true },
    { to: '/admin/analytics', label: 'Platform Analytics', icon: BarChart3 },
    { to: '/feed', label: 'Startup Feed', icon: Rss },
    { to: '/notifications', label: 'Notifications', icon: Bell },
    { to: '/events', label: 'Events', icon: CalendarDays },
];
export function getNavItems(role) {
    return role === 'FOUNDER' ? founderItems : role === 'INVESTOR' ? investorItems : adminItems;
}
