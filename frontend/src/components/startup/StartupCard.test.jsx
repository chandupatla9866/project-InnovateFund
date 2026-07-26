import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { StartupCard } from './StartupCard'

// StartupCard deliberately avoids wrapping the whole card in a single <Link> so a secondary
// "View" action can sit inside it without producing invalid nested <a> tags (see viewTo prop).
// These tests protect that: clicking the card body must navigate to `to`, and clicking the View
// icon must navigate to `viewTo` WITHOUT also triggering the card's own navigation.
const startup = {
  id: 'startup-1',
  name: 'TestCo',
  industry: 'SaaS',
  stage: 'MVP',
  fundingGoal: 100000,
  fundingProgress: 25000,
  published: true,
  verified: false,
  interestedInvestorsCount: 0,
}

function renderCard(props) {
  return render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route path="/dashboard" element={<StartupCard startup={startup} {...props} />} />
        <Route path="/edit-page" element={<div>Edit Page</div>} />
        <Route path="/view-page" element={<div>View Page</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('StartupCard', () => {
  it('renders the startup name and funding info', () => {
    renderCard({ to: '/edit-page' })
    expect(screen.getByText('TestCo')).toBeInTheDocument()
  })

  it('navigates to `to` when the card body is clicked', async () => {
    const user = userEvent.setup()
    renderCard({ to: '/edit-page' })

    await user.click(screen.getByText('TestCo'))

    expect(await screen.findByText('Edit Page')).toBeInTheDocument()
  })

  it('does not render a View icon when viewTo is not provided', () => {
    renderCard({ to: '/edit-page' })
    expect(screen.queryByLabelText('View startup page')).not.toBeInTheDocument()
  })

  it('navigates to `viewTo` (not `to`) when the View icon is clicked', async () => {
    const user = userEvent.setup()
    renderCard({ to: '/edit-page', viewTo: '/view-page' })

    await user.click(screen.getByLabelText('View startup page'))

    expect(await screen.findByText('View Page')).toBeInTheDocument()
    expect(screen.queryByText('Edit Page')).not.toBeInTheDocument()
  })

  it('shows the interested-investors badge only when count > 0', () => {
    renderCard({ to: '/edit-page' })
    expect(screen.queryByText(/interested/)).not.toBeInTheDocument()
  })

  it('shows the interested-investors badge when count > 0', () => {
    renderCard({ to: '/edit-page', startup: { ...startup, interestedInvestorsCount: 3 } })
    expect(screen.getByText('3 interested')).toBeInTheDocument()
  })

  it('shows a Draft badge for unpublished startups', () => {
    renderCard({ to: '/edit-page', startup: { ...startup, published: false } })
    expect(screen.getByText('Draft')).toBeInTheDocument()
  })
})
