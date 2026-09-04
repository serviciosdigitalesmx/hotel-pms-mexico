import { test, expect } from '@playwright/test';
import type { UserPayload } from '../src/types/auth.types';
import type { RoomTypeResponse } from '../src/types/inventory.types';
import { json, loginUI, PmsApi, status, uniqueTag } from './support';

test('real receptionist RBAC and forged internal headers cannot grant ADMIN or tenant access', async ({ page, playwright }) => {
  const admin = await loginUI(page);
  const tag = uniqueTag();
  const settings = await admin.settings();
  const username = `${tag}-reception`;
  const created = await json<{ username: string; role: string }>(await admin.mutate('POST', '/api/v1/auth/users', {
    username, password: 'NativeReception1A', email: `${tag}@staff.test`, role: 'RECEPTIONIST',
  }), 201);
  expect(created).toMatchObject({ username, role: 'RECEPTIONIST' });
  const receptionist = await playwright.request.newContext();
  const anonymous = await playwright.request.newContext();
  try {
    const api = new PmsApi(receptionist);
    const me: UserPayload = await api.login({ username, password: 'NativeReception1A', newPassword: 'NativeReception2B' });
    expect(me.role).toBe('RECEPTIONIST');
    await status(await api.get('/api/v1/rooms'), 200);

    const forged = {
      'X-Auth-User': 'admin', 'X-Auth-Role': 'ADMIN',
      'X-Auth-Hotel': '11111111-1111-1111-1111-111111111111',
      'X-Auth-Timestamp': Date.now().toString(), 'X-Auth-Nonce': uniqueTag(),
      'X-Internal-Signature': '0'.repeat(64),
    };
    const data = { name: `${tag}-forbidden`, description: 'RBAC probe', maxOccupancy: 2, basePrice: 100 };
    for (const headers of [undefined, forged]) {
      await status(await api.mutate('POST', '/api/v1/room-types', data, headers), 403);
      await status(await api.get('/api/v1/auth/users', headers), 403);
    }
    // Read permission still works after forged ADMIN/tenant/HMAC headers: the
    // gateway replaces them with claims from the actual receptionist session.
    const safeSettings = await json<{ hotelId: string }>(await api.get('/api/v1/stays/settings', forged));
    expect(safeSettings.hotelId).toBe(settings.hotelId);
    const roomTypes = await json<RoomTypeResponse[]>(await admin.get('/api/v1/room-types'));
    expect(roomTypes.some(item => item.name === data.name)).toBe(false);
    const anon = new PmsApi(anonymous);
    await status(await anon.get('/api/v1/rooms', forged), 401);
    await status(await anon.get('/api/v1/auth/users', forged), 401);
  } finally {
    await receptionist.dispose();
    await anonymous.dispose();
  }
});
