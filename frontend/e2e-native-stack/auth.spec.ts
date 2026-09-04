import { test, expect } from '@playwright/test';
import type { GuestResponseDTO } from '../src/types/guest.types';
import type { SpringPage } from '../src/types/page.types';
import { baseURL, csrfHeaders, json, loginUI, PmsApi, primaryCredentials, status, uniqueTag, waitForApi } from './support';

test('UI login, auth/me, cookie refresh rotation/replay and CSRF enforcement', async ({ page, browser }) => {
  const api = await loginUI(page);
  const before = await page.context().storageState();
  for (const name of ['jwt', 'refresh_token']) {
    const cookie = before.cookies.find(item => item.name === name);
    expect(Boolean(cookie?.value), `Missing ${name} cookie`).toBe(true);
    expect(cookie?.httpOnly).toBe(true);
  }
  expect(before.cookies.find(item => item.name === 'csrf_token')?.httpOnly).toBe(false);
  const oldCsrf = await csrfHeaders(page);

  await test.step('rotate the real refresh cookie and reject reuse of its predecessor', async () => {
    await status(await api.mutate('POST', '/api/v1/auth/refresh'), 200);
    const after = await page.context().storageState();
    expect(after.cookies.find(c => c.name === 'refresh_token')?.value
      !== before.cookies.find(c => c.name === 'refresh_token')?.value).toBe(true);
    expect((await csrfHeaders(page))['X-CSRF-Token'] !== oldCsrf['X-CSRF-Token']).toBe(true);

    const replay = await browser.newContext({ baseURL, storageState: before });
    try {
      const replayPage = await replay.newPage();
      await status(await new PmsApi(replayPage).mutate('POST', '/api/v1/auth/refresh'), 401);
    } finally {
      await replay.close();
    }
    const me = waitForApi(page, 'GET', '/api/v1/auth/me');
    await page.reload();
    expect(await json(await me)).toMatchObject({ username: primaryCredentials().username });
    await expect(page.locator('input[name="username"]')).toHaveCount(0);
  });

  await test.step('reject absent, mismatched and stale CSRF headers without persisting the guest', async () => {
    const tag = uniqueTag();
    const data = { firstName: 'Native', lastName: tag, email: `${tag}@example.test` };
    for (const headers of [{}, { 'X-CSRF-Token': 'deliberately-invalid' }, oldCsrf]) {
      // Deliberate negative probes are the only non-bootstrap mutations that do
      // not use the current CSRF cookie.
      await status(await api.rawMutation('POST', '/api/v1/guests', data, headers), 403);
    }
    const search = `/api/v1/guests/search?query=${encodeURIComponent(data.email)}`;
    expect((await json<SpringPage<GuestResponseDTO>>(await api.get(search))).content).toEqual([]);
    const created = await json<GuestResponseDTO>(await api.mutate('POST', '/api/v1/guests', data), 201);
    expect(created).toMatchObject(data);
    expect((await json<SpringPage<GuestResponseDTO>>(await api.get(search))).content.map(g => g.id)).toContain(created.id);
  });
});
