import { describe, expect, it } from 'vitest'
import { visibleFixiNavGroups, type FixiNavItem } from './app-sidebar'
import { visibleOrderTabs } from '@/fixi/order-navigation'

const flatten = (items: FixiNavItem[]): FixiNavItem[] => items.flatMap(item => [item, ...flatten(item.items ?? [])])
const itemsFor = (capabilities: string[], role: string) => flatten(visibleFixiNavGroups(new Set(capabilities), role).flatMap(group => group.items))

describe('Fixi navigation authorization', () => {
  it('fails closed while capabilities are unavailable', () => {
    expect(visibleFixiNavGroups(undefined)).toEqual([])
  })

  it('exposes only navigation whose effective backend capability is present', () => {
    const labels = itemsFor(['ORDERS_READ', 'ORDERS_UPDATE'], 'TECHNICIAN').map(item => item.title)
    expect(labels).toContain('Órdenes de servicio')
    expect(labels).toContain('Agenda')
    expect(labels).not.toContain('Dashboard')
    expect(labels).not.toContain('Usuarios y permisos')
    expect(labels).not.toContain('Finanzas')
    expect(labels).not.toContain('Configuración')
  })

  it('filters nested actions individually and removes empty administrative groups', () => {
    const items = itemsFor(['ORDERS_READ', 'CUSTOMERS_READ'], 'RECEPTIONIST')
    expect(items.map(item => item.url)).toContain('/customers')
    expect(items.map(item => item.url)).not.toContain('/reception/new')
    expect(items.map(item => item.url)).not.toContain('/customers/import')
    expect(items.map(item => item.url)).not.toContain('/settings')
    expect(items.map(item => item.url)).not.toContain('/team')
  })

  it('keeps ordinary navigation compact and settings nested for an owner', () => {
    const caps = ['ORDERS_READ', 'ORDERS_UPDATE', 'ORDERS_CREATE', 'CUSTOMERS_READ', 'CUSTOMERS_WRITE', 'DEVICES_READ', 'INVENTORY_READ', 'INVENTORY_PURCHASE', 'FINANCE_READ', 'PAYMENTS_RECEIVE', 'REPORTS_READ', 'USERS_MANAGE', 'SETTINGS_READ', 'SETTINGS_MANAGE', 'BRANCHES_READ', 'WORKFLOWS_MANAGE']
    const roots = visibleFixiNavGroups(new Set(caps), 'OWNER').flatMap(group => group.items)
    expect(roots).toHaveLength(9)
    expect(roots.map(item => item.url)).not.toContain('/branches')
    const config = roots.find(item => item.title === 'Configuración')
    expect(config?.items?.map(item => item.url)).toContain('/branches')
    expect(flatten(roots).map(item => item.url)).not.toContain('/platform')
    expect(flatten(roots).map(item => item.url)).not.toContain('/parts')
  })

  it('does not show role-restricted routes based on a capability override alone', () => {
    const items = itemsFor(['USERS_MANAGE', 'SETTINGS_READ', 'FINANCE_READ'], 'TECHNICIAN')
    expect(items.map(item => item.url)).not.toContain('/team')
    expect(items.map(item => item.url)).not.toContain('/settings')
    expect(items.map(item => item.url)).not.toContain('/reports/finance')
  })

  it('keeps order states contextual and independently capability filtered', () => {
    expect(visibleOrderTabs(undefined)).toEqual([])
    const urls = visibleOrderTabs(new Set(['ORDERS_READ', 'ORDERS_UPDATE'])).map(item => item.url)
    expect(urls).toContain('/diagnostics')
    expect(urls).toContain('/repairs')
    expect(urls).not.toContain('/delivery')
    expect(urls).not.toContain('/orders/pending-payment')
    expect(urls).not.toContain('/orders/unassigned')
  })
})
