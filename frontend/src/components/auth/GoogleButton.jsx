import { oauthStartUrl } from '../../api/authApi';
import { useOAuthEnabled } from '../../hooks/useOAuth';
function GoogleIcon() {
    return (<svg className="size-4" viewBox="0 0 24 24" aria-hidden="true">
      <path fill="#4285F4" d="M23.49 12.27c0-.79-.07-1.54-.19-2.27H12v4.51h6.47c-.29 1.48-1.14 2.73-2.43 3.58v3h3.93c2.3-2.12 3.62-5.23 3.62-8.82z"/>
      <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.93-3c-1.09.73-2.49 1.16-4 1.16-3.08 0-5.69-2.08-6.62-4.87H1.32v3.09C3.29 21.3 7.32 24 12 24z"/>
      <path fill="#FBBC05" d="M5.38 14.38c-.24-.73-.38-1.5-.38-2.38s.14-1.65.38-2.38V6.53H1.32C.48 8.16 0 10.03 0 12s.48 3.84 1.32 5.47l4.06-3.09z"/>
      <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.44-3.44C17.94 1.19 15.24 0 12 0 7.32 0 3.29 2.7 1.32 6.53l4.06 3.09C6.31 6.83 8.92 4.75 12 4.75z"/>
    </svg>);
}
export function GoogleButton({ role }) {
    const { data: enabled } = useOAuthEnabled();
    if (!enabled)
        return null;
    return (<a href={oauthStartUrl(role)} className="flex w-full items-center justify-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 shadow-sm transition-colors hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200 dark:hover:bg-slate-800">
      <GoogleIcon />
      Continue with Google
    </a>);
}
