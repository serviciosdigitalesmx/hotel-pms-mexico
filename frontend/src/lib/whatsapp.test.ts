import { describe, expect, it } from 'vitest'
import { trackingWhatsAppUrl } from './whatsapp'

describe('trackingWhatsAppUrl', () => {
  it('uses the server-normalized phone and includes the customer, order and real tracking URL', () => {
    const tracking = 'https://app.serviciosdigitalesmx.online/t/secure-order-token'
    const result = trackingWhatsAppUrl('+52 55 1234 5678', 'Ana Pérez López', '#000031', tracking)

    expect(result).not.toBeNull()
    const url = new URL(result!)
    expect(url.origin).toBe('https://wa.me')
    expect(url.pathname).toBe('/525512345678')
    expect(url.searchParams.get('text')).toBe(`Hola Ana Pérez López, puedes revisar el estatus de tu reparación #000031 en este enlace: ${tracking}`)
  })

  it('does not build a WhatsApp link for missing, implausible or overlong numbers', () => {
    expect(trackingWhatsAppUrl(null, 'Ana', '#000031', 'https://example.com/t/token')).toBeNull()
    expect(trackingWhatsAppUrl('12345', 'Ana', '#000031', 'https://example.com/t/token')).toBeNull()
    expect(trackingWhatsAppUrl('1'.repeat(16), 'Ana', '#000031', 'https://example.com/t/token')).toBeNull()
  })
})
