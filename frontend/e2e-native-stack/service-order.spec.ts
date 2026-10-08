import { test, expect } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { baseURL, json, PmsApi, primaryCredentials, status, uniqueTag } from './support';

test('creates a no-inventory service order, diagnoses it and records authorization', async ({ browser }) => {
  test.setTimeout(120_000);
  const context = await browser.newContext({ baseURL });
  try {
    const api = new PmsApi(await context.newPage());
    const user = await api.login(primaryCredentials());
    const customerId = randomUUID();
    const deviceId = randomUUID();
    const branch = await json<{ id: string }>(await api.mutate('POST', '/api/v1/auth/branches', {
      name: `${uniqueTag()} stock branch`,
    }), 201);
    const product = await json<{ id: string; hotelId: string }>(
      await api.mutate('POST', '/api/v1/inventory/products', {
        sku: `${uniqueTag()}-SKU`, name: 'Repair part', optional: false,
      }), 200);
    const balance = await json<{ branchId: string; productId: string; quantity: number }>(
      await api.mutate('POST', '/api/v1/inventory/movements', {
        branchId: branch.id, productId: product.id, delta: 3,
      }), 200);
    expect(balance).toMatchObject({ branchId: branch.id, productId: product.id, quantity: 3 });
    const order = await json<{ id: string; hotelId: string; status: string }>(
      await api.mutate('POST', '/api/v1/service-orders', {
        customerId, deviceId, branchId: branch.id, summary: `${uniqueTag()} no-inventory repair`,
      }), 201);
    expect(order.hotelId).toBe(user.hotelId);
    expect(order.status).toBe('INTAKE');
    const diagnosis = await json<{ status: string; diagnosis: string }>(
      await api.mutate('PATCH', `/api/v1/service-orders/${order.id}`, {
        status: 'QUOTED', diagnosis: 'Bench diagnosis', workLog: 'No parts consumed',
      }), 200);
    expect(diagnosis.status).toBe('QUOTED');
    const authorized = await json<{ status: string; authorizationEvidence: string }>(
      await api.mutate('POST', `/api/v1/service-orders/${order.id}/authorize`, {
        evidence: 'Customer approved quote',
      }), 200);
    expect(authorized.status).toBe('AUTHORIZED');
    expect(authorized.authorizationEvidence).toBe('Customer approved quote');
    await status(await api.get(`/api/v1/service-orders/${order.id}`), 200);
  } finally {
    await context.close();
  }
});
