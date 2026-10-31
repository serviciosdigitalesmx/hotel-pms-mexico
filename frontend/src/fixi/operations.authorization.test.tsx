import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import * as api from './api'
import { Protected } from './operations'

vi.mock('./api', () => ({
  currentUser: vi.fn(),
  effectiveCapabilities: vi.fn(),
}))

describe('Fixi direct-route authorization', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(api.currentUser).mockResolvedValue({
      username: 'tech', role: 'TECHNICIAN', hotelId: 'tenant-a', mustChangePassword: false,
    })
    vi.mocked(api.effectiveCapabilities).mockResolvedValue({
      tenantId: 'tenant-a', capabilities: ['ORDERS_READ', 'ORDERS_UPDATE'],
    })
  })

  it('blocks direct navigation to a screen not included in effective capabilities', async () => {
    render(
      <MemoryRouter initialEntries={['/reports/finance']}>
        <Routes>
          <Route path="/reports/finance" element={
            <Protected capabilities={['FINANCE_READ']}><div>Finanzas privadas</div></Protected>
          } />
        </Routes>
      </MemoryRouter>,
    )

    await waitFor(() => expect(screen.getByRole('heading', { name: 'Acceso no autorizado' })).toBeInTheDocument())
    expect(screen.queryByText('Finanzas privadas')).not.toBeInTheDocument()
  })

  it('blocks platform UI unless the session has the persisted platform grant role', async () => {
    render(
      <MemoryRouter initialEntries={['/platform']}>
        <Routes>
          <Route path="/platform" element={
            <Protected platformRole="SUPER_ADMIN"><div>Consola de plataforma</div></Protected>
          } />
        </Routes>
      </MemoryRouter>,
    )

    await waitFor(() => expect(screen.getByRole('heading', { name: 'Acceso no autorizado' })).toBeInTheDocument())
    expect(screen.queryByText('Consola de plataforma')).not.toBeInTheDocument()
    expect(api.effectiveCapabilities).not.toHaveBeenCalled()
  })
})
