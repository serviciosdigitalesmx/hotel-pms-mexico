import { test, expect } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { baseURL, json, PmsApi, primaryCredentials, status, uniqueTag } from './support';

test('public portal binds authorization to an OTP-verified customer token', async ({ browser }) => {
  test.setTimeout(120_000);
  const staffContext = await browser.newContext({ baseURL });
  const publicContext = await browser.newContext({ baseURL });
  try {
    const staff = new PmsApi(await staffContext.newPage());
    await staff.login(primaryCredentials());
    const branch = await json<{ id: string }>(await staff.mutate('POST', '/api/v1/auth/branches', {
      name: `${uniqueTag()} portal branch`,
    }), 201);
    const order = await json<{ id: string; customerId: string }>(
      await staff.mutate('POST', '/api/v1/service-orders', {
        customerId: randomUUID(), deviceId: randomUUID(), branchId: branch.id,
        summary: `${uniqueTag()} public portal authorization`,
      }), 201);
    await status(await staff.mutate('PATCH', `/api/v1/service-orders/${order.id}`, {
      status: 'QUOTED', diagnosis: 'Portal E2E diagnosis', workLog: 'Portal E2E quote',
    }), 200);
    const issued = await json<{ token: string; resourceId: string }>(
      await staff.mutate('POST', `/api/v1/portal/tokens/service-orders/${order.id}`), 201);
    expect(issued.resourceId).toBe(order.id);

    const operation = await json<{ resourceId: string; customerId: string }>(
      await publicContext.request.get(`${baseURL}/api/public/portal/operations/${issued.token}`));
    expect(operation).toMatchObject({ resourceId: order.id, customerId: order.customerId });

    const challenge = await json<{ testCode: string }>(
      await publicContext.request.post(
        `${baseURL}/api/public/portal/operations/${issued.token}/otp/challenge`), 200);
    const verified = await json<{ verified: boolean }>(
      await publicContext.request.post(
        `${baseURL}/api/public/portal/operations/${issued.token}/otp/verify?code=${challenge.testCode}`), 200);
    expect(verified.verified).toBe(true);
    const replay = await publicContext.request.post(
      `${baseURL}/api/public/portal/operations/${issued.token}/otp/verify?code=${challenge.testCode}`);
    expect(replay.status()).toBe(404);

    const authorized = await publicContext.request.post(
      `${baseURL}/api/public/portal/operations/${issued.token}/authorize?evidence=OTP%20verified%20customer%20approval`);
    expect(authorized.status()).toBe(200);
    await status(await staff.get(`/api/v1/service-orders/${order.id}`), 200);
  } finally {
    await staffContext.close();
    await publicContext.close();
  }
});
