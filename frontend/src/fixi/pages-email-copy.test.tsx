import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import type { ReactNode } from 'react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AcceptInvitationPage, ForgotPasswordPage, ResetPasswordPage, SignupPage, VerifyEmailPage } from './pages'

const apiMocks = vi.hoisted(() => ({
  signup: vi.fn(),
  forgotPassword: vi.fn(),
  verifyEmail: vi.fn(),
  resetPassword: vi.fn(),
  acceptInvitation: vi.fn(),
}))

vi.mock('./api', async importOriginal => {
  const actual = await importOriginal<typeof import('./api')>()
  return { ...actual, ...apiMocks }
})

function renderRoute(path: string, element: ReactNode) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes><Route path="*" element={element} /></Routes>
    </MemoryRouter>,
  )
}

describe('Fixi email guidance', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    apiMocks.signup.mockResolvedValue(undefined)
    apiMocks.forgotPassword.mockResolvedValue(undefined)
    apiMocks.verifyEmail.mockResolvedValue(undefined)
    apiMocks.resetPassword.mockResolvedValue(undefined)
    apiMocks.acceptInvitation.mockResolvedValue(undefined)
  })

  it('shows inbox and spam guidance after signup requests verification', async () => {
    renderRoute('/signup', <SignupPage />)
    fireEvent.change(screen.getByLabelText('Nombre del propietario'), { target: { value: 'Synthetic Owner' } })
    fireEvent.change(screen.getByLabelText('Correo'), { target: { value: 'owner@example.test' } })
    fireEvent.change(screen.getByLabelText('Contraseña de Fixi'), { target: { value: 'ValidPass1' } })
    fireEvent.change(screen.getByLabelText('Nombre del negocio'), { target: { value: 'Synthetic Shop' } })
    fireEvent.change(screen.getByLabelText('Primera sucursal'), { target: { value: 'Main Branch' } })
    fireEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Te enviamos un correo de verificación. Revisa tu bandeja de entrada y, si no aparece, Spam o correo no deseado.',
    )
  })

  it('keeps spam guidance in the password-recovery request state', async () => {
    renderRoute('/forgot-password', <ForgotPasswordPage />)
    fireEvent.change(screen.getByPlaceholderText('tu@negocio.com'), { target: { value: 'owner@example.test' } })
    fireEvent.click(screen.getByRole('button', { name: 'Enviar instrucciones' }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Si existe una cuenta asociada, recibirás las instrucciones por correo. Revisa también Spam o correo no deseado.',
    )
  })

  it.each([
    ['/verify-email?token=synthetic', <VerifyEmailPage />],
    ['/reset-password?token=synthetic', <ResetPasswordPage />],
    ['/accept-invitation?token=synthetic', <AcceptInvitationPage />],
  ])('does not show spam guidance on an email-link page: %s', async (path, page) => {
    window.history.replaceState({}, '', path)
    renderRoute(path, page)
    await waitFor(() => expect(screen.queryByText(/Spam o correo no deseado/i)).not.toBeInTheDocument())
  })

  it('keeps email-link success states free of spam guidance', async () => {
    window.history.replaceState({}, '', '/verify-email?token=synthetic')
    const { unmount } = renderRoute('/verify-email', <VerifyEmailPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Confirmar correo' }))
    expect(await screen.findByText('Correo verificado. Ya puedes iniciar sesión.')).toBeInTheDocument()
    expect(screen.queryByText(/Spam o correo no deseado/i)).not.toBeInTheDocument()
    unmount()

    window.history.replaceState({}, '', '/reset-password?token=synthetic')
    renderRoute('/reset-password', <ResetPasswordPage />)
    fireEvent.change(screen.getByPlaceholderText('Al menos 6 caracteres y una mayúscula'), {
      target: { value: 'ValidPass1' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Guardar contraseña' }))
    expect(await screen.findByRole('link', { name: 'Iniciar sesión' })).toBeInTheDocument()
    expect(screen.queryByText(/Spam o correo no deseado/i)).not.toBeInTheDocument()

    window.history.replaceState({}, '', '/accept-invitation?token=synthetic')
    renderRoute('/accept-invitation', <AcceptInvitationPage />)
    fireEvent.change(screen.getByPlaceholderText('Nombre completo'), { target: { value: 'Synthetic User' } })
    fireEvent.change(screen.getByPlaceholderText('Usuario'), { target: { value: 'synthetic-user' } })
    fireEvent.change(screen.getByPlaceholderText('Crea tu contraseña de Fixi (12+ caracteres)'), {
      target: { value: 'SyntheticPass123' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Aceptar invitación' }))
    expect(await screen.findByRole('link', { name: 'Iniciar sesión' })).toBeInTheDocument()
    expect(screen.queryByText(/Spam o correo no deseado/i)).not.toBeInTheDocument()
  })
})
