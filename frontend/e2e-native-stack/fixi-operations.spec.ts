import { test, expect } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { json, loginUI, status, uniqueTag } from './support';

test('Fixi shop-floor journey: diagnose, quote and authorize a service order', async ({ page }) => {
  test.setTimeout(180_000);
  const tag = uniqueTag();
  const api = await loginUI(page);
  const customer = await json<{ id: string }>(await api.mutate('POST', '/api/v1/customers', {
    firstName: 'Shop floor', lastName: tag, email: `${tag}@fixi-journey.test`, phone: null,
  }), 201);
  const device = await json<{ id: string }>(await api.mutate('POST', `/api/v1/customers/${customer.id}/devices`, {
    category: 'PHONE', manufacturer: 'Fixi', model: 'Operations E2E', serialNumber: randomUUID(),
  }), 201);
  const branch = await json<{ id: string }>(await api.mutate('POST', '/api/v1/auth/branches', {
    name: `${tag} operations branch`,
  }), 201);
  const order = await json<{ id: string; status: string }>(await api.mutate('POST', '/api/v1/service-orders', {
    customerId: customer.id,
    deviceId: device.id,
    branchId: branch.id,
    summary: `${tag} shop-floor repair`,
    reportedFailure: 'Intermittent charging',
    intakeChecklist: 'Device and accessories received',
  }), 201);
  expect(order.status).toBe('INTAKE');

  await page.goto(`/service-orders/${order.id}`);
  await expect(page.getByRole('heading', { name: 'Orden de servicio' })).toBeVisible();
  const progressForm = page.locator('form').first();
  await progressForm.locator('select').first().selectOption('REVIEW');
  await progressForm.locator('textarea').nth(0).fill('Charging port requires cleaning');
  await progressForm.locator('textarea').nth(1).fill('Initial bench inspection completed');
  await progressForm.getByRole('button', { name: 'Guardar avance' }).click();
  await expect(progressForm.locator('select').first()).toHaveValue('REVIEW');

  const quoteForm = page.locator('form').filter({ has: page.getByPlaceholder('Solución propuesta') });
  await quoteForm.getByPlaceholder('Solución propuesta').fill('Clean charging port and verify charging');
  await quoteForm.locator('input[type="number"]').nth(0).fill('30');
  await quoteForm.getByPlaceholder('Mano de obra').fill('450');
  await quoteForm.getByPlaceholder('Refacciones').fill('0');
  await quoteForm.getByRole('button', { name: 'Generar cotización' }).click();
  await expect.poll(async () => {
    const current = await json<{ status: string }>(await api.get(`/api/v1/service-orders/${order.id}`));
    return current.status;
  }).toBe('QUOTED');
  await expect(page.getByRole('button', { name: 'Autorizar' })).toBeVisible();
  const quoted = await json<{ status: string; quoteTotal: number }>(await api.get(`/api/v1/service-orders/${order.id}`));
  expect(quoted.status).toBe('QUOTED');
  expect(quoted.quoteTotal).toBeGreaterThanOrEqual(450);

  await page.getByRole('button', { name: 'Autorizar' }).click();
  await expect(page.getByText('Cotización autorizada')).toBeVisible();
  const authorized = await json<{ status: string; authorizationEvidence?: string }>(
    await api.get(`/api/v1/service-orders/${order.id}`));
  expect(authorized.status).toBe('AUTHORIZED');
  expect(authorized.authorizationEvidence).toContain('Autorización registrada en Fixi');
  await status(await api.get(`/api/v1/customers/${customer.id}/devices`), 200);
});
