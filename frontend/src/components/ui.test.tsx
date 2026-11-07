import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Drawer, Field, Input, Modal, Pagination, Table, Tabs, Td, Th } from './ui'

describe('Field', () => {
  it('links the label, the control and the error message', () => {
    render(
      <Field label="Teléfono" error="Es obligatorio">
        <Input />
      </Field>
    )
    const input = screen.getByLabelText('Teléfono')
    expect(input).toHaveAttribute('aria-invalid', 'true')
    expect(input).toHaveAccessibleDescription('Es obligatorio')
  })

  it('associates errors with controls inside composite fields', () => {
    render(
      <Field label="Costo" controlId="cost" error="Revisa el monto">
        <div><Input id="cost" /><Input aria-label="Moneda" /></div>
      </Field>
    )
    expect(screen.getByLabelText('Costo')).toHaveAttribute('aria-describedby')
    expect(screen.getByLabelText('Costo')).toHaveAccessibleDescription('Revisa el monto')
  })
})

describe('Pagination', () => {
  it('shows the range and moves between pages', async () => {
    const onChange = vi.fn()
    render(<Pagination skip={25} take={25} total={60} onChange={onChange} />)
    expect(screen.getByText('26–50 de 60')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: /Siguiente/ }))
    expect(onChange).toHaveBeenCalledWith(50)
    await userEvent.click(screen.getByRole('button', { name: /Anterior/ }))
    expect(onChange).toHaveBeenCalledWith(0)
  })

  it('only shows the count when everything fits in one page', () => {
    render(<Pagination skip={0} take={25} total={3} onChange={() => undefined} />)
    expect(screen.getByText('3 resultados')).toBeInTheDocument()
  })
})

describe('Modal', () => {
  it('renders its content and closes from the close button', async () => {
    const onClose = vi.fn()
    render(
      <Modal open onClose={onClose} title="Cobrar">
        <p>Contenido</p>
      </Modal>
    )
    expect(screen.getByText('Contenido')).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Cobrar' })).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Cerrar' }))
    expect(onClose).toHaveBeenCalled()
  })

  it('renders nothing when closed', () => {
    render(
      <Modal open={false} onClose={() => undefined} title="Oculto">
        <p>Nada</p>
      </Modal>
    )
    expect(screen.queryByText('Nada')).not.toBeInTheDocument()
  })
})

describe('Tabs', () => {
  it('supports arrow-key navigation and only puts the selected tab in the tab order', async () => {
    const onChange = vi.fn()
    render(<Tabs value="orders" onChange={onChange} tabs={[{ value: 'orders', label: 'Órdenes' }, { value: 'suppliers', label: 'Proveedores' }]} />)
    const orders = screen.getByRole('tab', { name: 'Órdenes' })
    const suppliers = screen.getByRole('tab', { name: 'Proveedores' })
    expect(orders).toHaveAttribute('tabindex', '0')
    expect(suppliers).toHaveAttribute('tabindex', '-1')
    orders.focus()
    await userEvent.keyboard('{ArrowRight}')
    expect(onChange).toHaveBeenCalledWith('suppliers')
    expect(suppliers).toHaveFocus()
  })
})

describe('Table', () => {
  it('adds visible column labels to cells for the mobile card layout', () => {
    render(<Table><thead><tr><Th>Folio</Th><Th>Cliente</Th></tr></thead><tbody><tr><Td>O-1</Td><Td>Alex</Td></tr></tbody></Table>)
    expect(screen.getByText('O-1')).toHaveAttribute('data-label', 'Folio')
    expect(screen.getByText('Alex')).toHaveAttribute('data-label', 'Cliente')
  })
})

describe('Drawer', () => {
  it('closes with Escape and exposes a modal dialog', async () => {
    const onClose = vi.fn()
    render(<Drawer open onClose={onClose} title="Nuevo producto"><Input aria-label="Nombre" /></Drawer>)
    expect(screen.getByRole('dialog', { name: 'Nuevo producto' })).toBeInTheDocument()
    await userEvent.keyboard('{Escape}')
    expect(onClose).toHaveBeenCalledOnce()
  })
})
