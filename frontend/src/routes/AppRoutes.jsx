import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { LandingPage } from '../pages/LandingPage';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { ForgotPasswordPage } from '../pages/ForgotPasswordPage';
import { ResetPasswordPage } from '../pages/ResetPasswordPage';
import { OAuth2RedirectPage } from '../pages/OAuth2RedirectPage';
import { FeedPage } from '../pages/FeedPage';
import { SavedStartupsPage } from '../pages/investor/SavedStartupsPage';
import { NotificationsPage } from '../pages/NotificationsPage';
import { StartupDetailPage } from '../pages/StartupDetailPage';
import { ChatPage } from '../pages/ChatPage';
import { MeetingsPage } from '../pages/MeetingsPage';
import { DueDiligencePage } from '../pages/DueDiligencePage';
import { EventsPage } from '../pages/EventsPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { FounderDashboardPage } from '../pages/founder/FounderDashboardPage';
import { InvestorsPage } from '../pages/founder/InvestorsPage';
import { StartupEditorPage } from '../pages/founder/StartupEditorPage';
import { AiReportPage } from '../pages/founder/AiReportPage';
import { AiToolsPage } from '../pages/founder/AiToolsPage';
import { FounderProfilePage } from '../pages/founder/FounderProfilePage';
import { FounderAnalyticsPage } from '../pages/founder/FounderAnalyticsPage';
import { InvestorDashboardPage } from '../pages/investor/InvestorDashboardPage';
import { BrowseStartupsPage } from '../pages/investor/BrowseStartupsPage';
import { InvestorProfilePage } from '../pages/investor/InvestorProfilePage';
import { PortfolioPage } from '../pages/investor/PortfolioPage';
import { InvestorAnalyticsPage } from '../pages/investor/InvestorAnalyticsPage';
import { AdminDashboardPage } from '../pages/admin/AdminDashboardPage';
import { AdminAnalyticsPage } from '../pages/admin/AdminAnalyticsPage';
export function AppRoutes() {
    return (<Routes>
      <Route path="/" element={<LandingPage />}/>
      <Route path="/login" element={<LoginPage />}/>
      <Route path="/register" element={<RegisterPage />}/>
      <Route path="/forgot-password" element={<ForgotPasswordPage />}/>
      <Route path="/reset-password" element={<ResetPasswordPage />}/>
      <Route path="/oauth2/redirect" element={<OAuth2RedirectPage />}/>

      <Route path="/feed" element={<ProtectedRoute>
            <FeedPage />
          </ProtectedRoute>}/>
      <Route path="/investor/saved-startups" element={<ProtectedRoute allowedRoles={['INVESTOR']}>
            <SavedStartupsPage />
          </ProtectedRoute>}/>
      <Route path="/notifications" element={<ProtectedRoute>
            <NotificationsPage />
          </ProtectedRoute>}/>
      <Route path="/startups/:id" element={<ProtectedRoute>
            <StartupDetailPage />
          </ProtectedRoute>}/>
      <Route path="/chat" element={<ProtectedRoute>
            <ChatPage />
          </ProtectedRoute>}/>
      <Route path="/chat/:userId" element={<ProtectedRoute>
            <ChatPage />
          </ProtectedRoute>}/>
      <Route path="/meetings" element={<ProtectedRoute>
            <MeetingsPage />
          </ProtectedRoute>}/>
      <Route path="/startups/:id/due-diligence" element={<ProtectedRoute>
            <DueDiligencePage />
          </ProtectedRoute>}/>
      <Route path="/events" element={<ProtectedRoute>
            <EventsPage />
          </ProtectedRoute>}/>

      <Route path="/founder/dashboard" element={<ProtectedRoute allowedRoles={['FOUNDER']}>
            <FounderDashboardPage />
          </ProtectedRoute>}/>
      <Route path="/founder/startups/new" element={<ProtectedRoute allowedRoles={['FOUNDER']}>
            <StartupEditorPage />
          </ProtectedRoute>}/>
      <Route path="/founder/startups/:id/edit" element={<ProtectedRoute allowedRoles={['FOUNDER']}>
            <StartupEditorPage />
          </ProtectedRoute>}/>
      <Route path="/founder/startups/:id/ai-report" element={<ProtectedRoute allowedRoles={['FOUNDER']}>
            <AiReportPage />
          </ProtectedRoute>}/>
      <Route path="/founder/startups/:id/ai-tools" element={<ProtectedRoute allowedRoles={['FOUNDER']}>
            <AiToolsPage />
          </ProtectedRoute>}/>
      <Route path="/founder/profile" element={<ProtectedRoute allowedRoles={['FOUNDER']}>
            <FounderProfilePage />
          </ProtectedRoute>}/>
      <Route path="/founder/analytics" element={<ProtectedRoute allowedRoles={['FOUNDER']}>
            <FounderAnalyticsPage />
          </ProtectedRoute>}/>
      <Route path="/founder/investors" element={<ProtectedRoute allowedRoles={['FOUNDER']}>
            <InvestorsPage />
          </ProtectedRoute>}/>

      <Route path="/investor/dashboard" element={<ProtectedRoute allowedRoles={['INVESTOR']}>
            <InvestorDashboardPage />
          </ProtectedRoute>}/>
      <Route path="/investor/browse" element={<ProtectedRoute allowedRoles={['INVESTOR']}>
            <BrowseStartupsPage />
          </ProtectedRoute>}/>

      <Route path="/investor/portfolio" element={<ProtectedRoute allowedRoles={['INVESTOR']}>
            <PortfolioPage />
          </ProtectedRoute>}/>
      <Route path="/investor/analytics" element={<ProtectedRoute allowedRoles={['INVESTOR']}>
            <InvestorAnalyticsPage />
          </ProtectedRoute>}/>
      <Route path="/investor/profile" element={<ProtectedRoute allowedRoles={['INVESTOR']}>
            <InvestorProfilePage />
          </ProtectedRoute>}/>

      <Route path="/admin/dashboard" element={<ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminDashboardPage />
          </ProtectedRoute>}/>
      <Route path="/admin/analytics" element={<ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminAnalyticsPage />
          </ProtectedRoute>}/>

      <Route path="*" element={<NotFoundPage />}/>
    </Routes>);
}
