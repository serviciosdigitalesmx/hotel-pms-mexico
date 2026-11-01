import { expect, test } from '@playwright/test'

// A visitor signs up from the pricing page, works during the trial and picks a plan.
// In development the subscription is activated by the simulated provider (no real charge).
test('a new shop signs up, gets the trial and picks a plan', async ({ page }) => {
  await page.goto('/precios')
  await expect(page.getByRole('heading', { name: 'El sistema para tu servicio técnico' })).toBeVisible()
  await expect(page.getByText('Estándar').first()).toBeVisible()
  await page.screenshot({ path: 'test-results/precios.png', fullPage: true })

  await page.getByRole('link', { name: 'Probar gratis' }).click()
  await page.getByLabel('Nombre del taller').fill('Taller E2E')
  await page.getByLabel('Tu nombre').fill('Dueño E2E')
  await page.getByLabel('Correo electrónico').fill(`dueno${Date.now()}@example.com`)
  await page.getByLabel('Contraseña').fill('Fixi26')
  await page.getByLabel('País').selectOption('AR')
  await page.getByRole('checkbox').check()
  await page.screenshot({ path: 'test-results/registro.png', fullPage: true })
  await page.getByRole('button', { name: 'Crear mi cuenta' }).click()

  await expect(page.getByText(/Prueba gratis: te quedan 14 días/)).toBeVisible()
  await expect(page.getByText(/Confirma tu correo electrónico/)).toBeVisible()
  await page.screenshot({ path: 'test-results/inicio-prueba.png', fullPage: true })

  await page.getByRole('link', { name: 'Suscripción' }).first().click()
  await expect(page.getByRole('heading', { name: 'Suscripción' })).toBeVisible()
  await page.screenshot({ path: 'test-results/suscripcion.png', fullPage: true })

  await page.getByRole('button', { name: 'Elegir este plan' }).nth(1).click()
  await expect(page.getByText('Plan actualizado.')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Plan actual' })).toBeVisible()
  await expect(page.getByText(/Prueba gratis: te quedan/)).toHaveCount(0)
})

test('signup explains a short password in the form without sending the request', async ({ page }) => {
  let signupRequestMade = false
  page.on('request', (request) => {
    if (request.url().includes('/api/v1/auth/signup')) signupRequestMade = true
  })

  await page.goto('/registro')
  await page.getByLabel('Nombre del taller').fill('Taller E2E')
  await page.getByLabel('Tu nombre').fill('Dueño E2E')
  await page.getByLabel('Correo electrónico').fill(`dueno${Date.now()}@example.com`)
  await page.getByLabel('Contraseña').fill('Ab123')
  await page.getByLabel('País').selectOption('MX')
  await page.getByRole('checkbox').check()
  await page.getByRole('button', { name: 'Crear mi cuenta' }).click()

  await expect(page.getByText('La contraseña debe tener al menos 6 caracteres.')).toBeVisible()
  expect(signupRequestMade).toBe(false)
})
