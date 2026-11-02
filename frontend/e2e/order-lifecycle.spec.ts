import { expect, test } from '@playwright/test'
import { createOrderViaUi, ensureCashOpen, login, moveOrderTo, uniquePhone } from './helpers'

const QA_ITEMS = ['Enciende', 'Pantalla', 'Táctil', 'Cámaras', 'Audio / parlantes', 'Micrófono', 'Botones', 'Carga', 'WiFi / señal / Bluetooth', 'Face ID / huella']

test('an order goes from reception to delivery: quote, QA, payment and warranty', async ({ page }) => {
  test.setTimeout(120_000)
  await login(page, 'admin')
  await ensureCashOpen(page)
  const code = await createOrderViaUi(page)
  const orderUrl = page.url()

  await moveOrderTo(page, 'En diagnóstico')

  // Starting the repair without an approved quote is blocked and leads to the quote tab.
  await page.getByRole('button', { name: /En reparación/ }).click()
  await expect(page.getByRole('tab', { name: /^Cotizaciones/, selected: true })).toBeVisible()

  await page.getByRole('button', { name: '+ Armar cotización' }).click()
  await page.getByLabel('Precio unit.').first().fill('25000')
  await page.getByRole('button', { name: 'Guardar borrador' }).click()
  await expect(page.getByText('Versión 1')).toBeVisible()
  await page.getByRole('button', { name: 'Aprobado', exact: true }).click()
  await page.getByRole('button', { name: 'Aprobar', exact: true }).click()
  await expect(page.getByText('Aprobado', { exact: true }).first()).toBeVisible()

  await moveOrderTo(page, 'En reparación')

  // Quality control before "ready".
  await page.getByRole('tab', { name: /^Checklists/ }).click()
  for (const item of QA_ITEMS) await page.getByRole('group', { name: item }).getByRole('button', { name: 'OK' }).click()
  await page.getByRole('button', { name: 'Aprobar control' }).click()
  await expect(page.getByText(/^Aprobado/).first()).toBeVisible()

  await moveOrderTo(page, 'Listo para retirar')

  // Pay the balance, then deliver.
  await page.getByRole('tab', { name: /^Pagos/ }).click()
  await page.getByRole('button', { name: 'Registrar pago / seña' }).click()
  await page.getByLabel('Medio de pago').selectOption('Transfer')
  await page.getByRole('button', { name: /^Registrar \$/ }).click()
  await expect(page.getByText('Pago registrado')).toBeVisible()

  await moveOrderTo(page, 'Entregado')
  await expect(page.getByRole('button', { name: 'Garantía', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Reingreso por garantía' })).toBeVisible()

  // History keeps every step.
  await page.goto(orderUrl + '?tab=history')
  await page.getByRole('tab', { name: /^Historial/ }).click()
  for (const label of ['En diagnóstico', 'En reparación', 'Listo para retirar', 'Entregado']) await expect(page.getByText(label).first()).toBeVisible()
  expect(code).toMatch(/^#\d+$/)
})

test('order confirmation prepares a consented WhatsApp link with the real tracking URL and distinguishes receipt from QR label', async ({ page }) => {
  test.setTimeout(120_000)
  await login(page, 'reception')
  await page.goto('/orders/new')
  await page.getByRole('button', { name: '+ Cliente nuevo' }).click()

  const customerName = `Cliente seguimiento ${Date.now()}`
  await page.getByLabel('Nombre y apellido').fill(customerName)
  await page.getByLabel('Teléfono / WhatsApp').fill(uniquePhone())
  await page.getByLabel('Acepta recibir avisos del estado de su equipo').check()
  const customerResponsePromise = page.waitForResponse((response) =>
    response.request().method() === 'POST' && new URL(response.url()).pathname.endsWith('/customers')
  )
  await page.getByRole('button', { name: 'Crear cliente y seguir' }).click()
  const customerResponse = await customerResponsePromise
  expect(customerResponse.ok()).toBeTruthy()
  const customerPayload = await customerResponse.json()
  const customerWhatsApp = String(customerPayload.data.whatsAppNumber).replace(/\D/g, '')

  await page.getByLabel('Marca').fill('Motorola')
  await page.getByLabel('Modelo').fill('Moto G54')
  await page.getByRole('button', { name: 'Agregar equipo y seguir' }).click()
  await page.getByLabel('Falla que reporta el cliente').fill('No carga; revisar centro de carga')
  await page.getByRole('button', { name: 'Crear orden' }).click()
  await expect(page.getByText('Orden creada')).toBeVisible()

  const trackingLink = page.getByRole('link', { name: /https?:\/\// })
  const trackingUrl = await trackingLink.getAttribute('href')
  expect(trackingUrl).toBeTruthy()
  const whatsappLink = page.getByRole('link', { name: 'Enviar seguimiento por WhatsApp' })
  const whatsappUrl = new URL((await whatsappLink.getAttribute('href'))!)
  expect(whatsappUrl.origin + whatsappUrl.pathname).toBe(`https://wa.me/${customerWhatsApp}`)
  expect(whatsappUrl.searchParams.get('text')).toContain(`Hola ${customerName},`)
  expect(whatsappUrl.searchParams.get('text')).toContain(trackingUrl)
  await expect(whatsappLink).toHaveAttribute('target', '_blank')

  await expect(page.getByRole('button', { name: 'Imprimir comprobante de ingreso' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Imprimir etiqueta QR' })).toBeVisible()
  await expect(page.getByText(/El comprobante incluye los datos de ingreso/)).toBeVisible()
})
