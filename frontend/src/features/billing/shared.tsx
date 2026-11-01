import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { authApi } from '../../api/endpoints'
import { errorMessage } from '../../api/http'
import { Alert } from '../../components/ui'

export const SUBSCRIPTION_KEY = ['billing', 'subscription'] as const

export function VerifyEmailAlert() {
  const resend = useMutation({
    mutationFn: authApi.resendVerification,
    onSuccess: () => toast.success('Te enviamos un nuevo enlace. Revisa también la carpeta de spam.'),
    onError: (err) => toast.error(errorMessage(err)),
  })
  return (
    <Alert tone="amber">
      Confirma tu correo electrónico con el enlace que te enviamos para recibir avisos de pagos y recuperar tu contraseña.{' '}
      <button type="button" className="font-medium underline" disabled={resend.isPending} onClick={() => resend.mutate()}>
        Reenviar enlace
      </button>
    </Alert>
  )
}
