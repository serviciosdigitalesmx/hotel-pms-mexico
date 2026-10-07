import { test, expect } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { baseURL, json, PmsApi, primaryCredentials, status, uniqueTag } from './support';

test('FIXI Web business contract: login, customer, device and reception', async ({ page }) => {
  test.setTimeout(180_000);
  const tag = uniqueTag();
  const email = `${tag}@business.test`;
  const api = new PmsApi(page);

  await test.step('login through the real Fixi Web form', async () => {
    const credentials = primaryCredentials();
    await page.goto('/login');
    await page.getByLabel('Correo o usuario').fill(credentials.username);
    await page.getByLabel('Contraseña').fill(credentials.password);
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();
    await expect(page).toHaveURL(/\/dashboard|\/onboarding$/);
    await expect(page.getByText('Dashboard Fixi').or(page.getByText('Configura tu negocio'))).toBeVisible();
  });

  const customer = await test.step('create a customer through Web and verify persistence', async () => {
    await page.goto('/customers');
    await page.getByPlaceholder('Nombre').fill('Web Contract');
    await page.getByPlaceholder('Apellido').fill(tag);
    await page.getByPlaceholder('Email').fill(email);
    await page.getByPlaceholder('Teléfono').fill('8112345678');
    await page.getByRole('button', { name: 'Guardar' }).click();
    await expect(page.getByText(`Web Contract ${tag}`)).toBeVisible();
    const list = await json<{content?: Array<{id:string;email?:string}>} | Array<{id:string;email?:string}>>(await api.get('/api/v1/customers?page=0&size=100'));
    const rows = Array.isArray(list) ? list : list.content ?? [];
    const created = rows.find(row => row.email === email);
    expect(created?.id).toBeTruthy();
    return created!;
  });

  const device = await test.step('register a device through Web and verify ownership', async () => {
    await page.goto(`/customers/${customer.id}`);
    await page.goto('/equipment');
    await page.locator('select').first().selectOption(customer.id);
    await page.getByPlaceholder('manufacturer').fill('Fixi');
    await page.getByPlaceholder('model').fill(`Bench-${tag}`);
    await page.getByPlaceholder('serialNumber').fill(randomUUID());
    await page.getByRole('button', { name: 'Registrar equipo' }).click();
    const devices = await json<Array<{id:string;customerId:string;model?:string}>>(await api.get(`/api/v1/customers/${customer.id}/devices`));
    const created = devices.find(item => item.customerId === customer.id && item.model === `Bench-${tag}`);
    expect(created?.id).toBeTruthy();
    return created!;
  });

  await test.step('receive the device through Web and verify the durable order', async () => {
    await page.goto('/reception');
    const selects = page.locator('select');
    await selects.nth(0).selectOption(customer.id);
    await selects.nth(1).selectOption(device.id);
    const summary = `Falla reportada ${tag}`;
    await page.getByPlaceholder('Síntomas, accesorios y condición física').fill(summary);
    await page.getByRole('button', { name: 'Crear orden' }).click();
    await expect(page).toHaveURL(/\/service-orders\//);
    await expect(page.getByText('Detalle de orden')).toBeVisible();
    const orderId = new URL(page.url()).pathname.split('/').pop();
    expect(orderId).toBeTruthy();
    const order = await json<{id:string;customerId:string;deviceId?:string;summary:string;status:string}>(await api.get(`/api/v1/service-orders/${orderId}`));
    expect(order).toMatchObject({ id: orderId, customerId: customer.id, deviceId: device.id, summary });
    await page.reload();
    await expect(page.getByText('Detalle de orden')).toBeVisible();
  });

  await test.step('protected boundary rejects an unauthenticated request', async () => {
    const unauthenticated = await page.context().request.newContext({ baseURL });
    try { await status(await unauthenticated.get('/api/v1/service-orders'), 401); }
    finally { await unauthenticated.dispose(); }
  });
});
