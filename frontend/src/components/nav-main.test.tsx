import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { SidebarProvider } from './ui/sidebar'
import { NavMain } from './nav-main'

const items = [
  { title: 'Órdenes', url: '/service-orders', items: [{ title: 'Todas las órdenes', url: '/service-orders' }] },
  { title: 'Clientes', url: '/customers', items: [{ title: 'Directorio', url: '/customers' }, { title: 'Importar', url: '/customers/import' }] },
]
function show(path: string) {
  return render(<MemoryRouter initialEntries={[path]}><SidebarProvider><NavMain label="Taller" items={items}/></SidebarProvider></MemoryRouter>)
}

describe('compact module navigation', () => {
  it('starts collapsed outside a module and expands only the selected group', async () => {
    const user = userEvent.setup()
    show('/dashboard')
    expect(screen.queryByRole('link', { name: 'Todas las órdenes' })).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Órdenes' }))
    expect(screen.getByRole('link', { name: 'Todas las órdenes' })).toBeVisible()
    await user.click(screen.getByRole('button', { name: 'Clientes' }))
    expect(screen.queryByRole('link', { name: 'Todas las órdenes' })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Directorio' })).toBeVisible()
  })

  it('opens the right group on a direct URL and highlights only the most specific child', () => {
    show('/customers/import')
    expect(screen.getByRole('link', { name: 'Importar' })).toHaveAttribute('data-active', 'true')
    expect(screen.getByRole('link', { name: 'Directorio' })).toHaveAttribute('data-active', 'false')
    expect(screen.queryByRole('link', { name: 'Todas las órdenes' })).not.toBeInTheDocument()
  })
})
