export function trackingWhatsAppUrl(
  whatsappNumber: string | null | undefined,
  customerName: string,
  orderCode: string,
  trackingUrl: string
): string | null {
  const digits = (whatsappNumber ?? '').replace(/\D/g, '')
  if (digits.length < 8 || digits.length > 15 || !trackingUrl) return null

  const name = customerName.trim()
  const greeting = name ? `Hola ${name},` : 'Hola,'
  const message = `${greeting} puedes revisar el estatus de tu reparación ${orderCode} en este enlace: ${trackingUrl}`
  return `https://wa.me/${digits}?text=${encodeURIComponent(message)}`
}
