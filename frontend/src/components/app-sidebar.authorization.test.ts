import { describe, expect, it } from 'vitest'
import { visibleFixiNavGroups } from './app-sidebar'

describe('Fixi navigation authorization', () => {
  it('fails closed while capabilities are unavailable', () => {
    expect(visibleFixiNavGroups(undefined)).toEqual([])
  })

  it('exposes only navigation whose effective backend capability is present', () => {
    const technician = visibleFixiNavGroups(new Set(['ORDERS_READ', 'ORDERS_UPDATE']))
    const labels = technician.flatMap(group => group.items.map(item => item.title))
    expect(labels).toContain('Órdenes de servicio')
    expect(labels).toContain('Agenda')
    expect(labels).not.toContain('Dashboard')
    expect(labels).not.toContain('Equipo / Usuarios')
    expect(labels).not.toContain('Finanzas')
  })
})
