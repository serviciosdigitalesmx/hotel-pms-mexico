import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import * as api from './api'
import { TeamUserEditor } from './team-user-editor'

vi.mock('./api', () => ({
  listUsers: vi.fn(),
  updateUser: vi.fn(),
}))

describe('Fixi employee role selection', () => {
  beforeEach(() => {
    vi.mocked(api.listUsers).mockResolvedValue([
      { id: 'legacy', username: 'legacy-user', email: 'legacy@example.test', role: 'HOUSEKEEPER', active: true, mustChangePassword: false },
    ])
  })

  it('does not offer PMS/platform roles while preserving a historical role for editing', async () => {
    render(<TeamUserEditor />)
    await waitFor(() => expect(screen.getByRole('option', { name: /legacy-user/i })).toBeInTheDocument())
    fireEvent.change(screen.getByLabelText('Usuario a editar'), { target: { value: 'legacy' } })

    const roleSelect = screen.getByLabelText('Rol del usuario') as HTMLSelectElement
    expect(roleSelect.value).toBe('HOUSEKEEPER')
    expect(screen.getByRole('option', { name: /HOUSEKEEPER.*histórico/i })).toBeDisabled()
    expect(screen.getByRole('option', { name: 'MANAGER' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'RECEPTIONIST' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'TECHNICIAN' })).toBeInTheDocument()
    expect(screen.queryByRole('option', { name: 'KITCHEN' })).not.toBeInTheDocument()
    expect(screen.queryByRole('option', { name: 'SUPER_ADMIN' })).not.toBeInTheDocument()
  })
})
