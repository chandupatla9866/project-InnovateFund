import { Navbar } from './Navbar';
import { Sidebar } from './Sidebar';
export function DashboardLayout({ children }) {
    return (<div className="flex min-h-screen flex-col">
      <Navbar />
      <div className="mx-auto flex w-full max-w-7xl flex-1">
        <Sidebar />
        <main className="min-w-0 flex-1 p-4 sm:p-6">{children}</main>
      </div>
    </div>);
}
