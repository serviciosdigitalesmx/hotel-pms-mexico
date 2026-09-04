import { randomUUID } from 'node:crypto';
import { expect, type APIRequestContext, type APIResponse, type Page, type Response } from '@playwright/test';
import type { UserPayload } from '../src/types/auth.types';
import type { HotelSettingsResponse, StayResponse } from '../src/types/stay.types';

export const baseURL = localOrigin(process.env.PLAYWRIGHT_NATIVE_BASE_URL ?? 'http://127.0.0.1:18080');
export const apiBaseURL = localOrigin(process.env.NATIVE_API_BASE_URL ?? baseURL);
export const mailpitURL = localOrigin(process.env.MAILPIT_BASE_URL ?? 'http://127.0.0.1:18025');
if (new URL(baseURL).hostname !== new URL(apiBaseURL).hostname) {
  throw new Error('PLAYWRIGHT_NATIVE_BASE_URL and NATIVE_API_BASE_URL must share a hostname for real browser cookies.');
}

// StayResponse.java exposes these fields; the existing frontend type omits them.
export type NativeStay = StayResponse & { hotelId: string; invoiceId: string | null };
export type HttpResponse = APIResponse | Response;
export interface Credentials { username: string; password: string; newPassword?: string }

let nextRequestAt = 0;
async function requestSlot(): Promise<void> {
  // Keep functional assertions below the real gateway's 10 req/s default.
  // Do not disable rate limiting or retry a failed mutation to hide an error.
  const now = Date.now();
  const scheduledAt = Math.max(now, nextRequestAt);
  nextRequestAt = scheduledAt + 200;
  if (scheduledAt > now) await new Promise(resolve => setTimeout(resolve, scheduledAt - now));
}

export function primaryCredentials(): Credentials {
  return {
    username: process.env.NATIVE_ADMIN_USERNAME ?? 'e2e-live-other-hotel-admin',
    password: process.env.NATIVE_ADMIN_PASSWORD ?? 'password',
  };
}

export function otherCredentials(): Credentials {
  return {
    username: process.env.NATIVE_OTHER_USERNAME ?? 'admin',
    password: process.env.NATIVE_OTHER_PASSWORD ?? 'password',
    newPassword: process.env.NATIVE_OTHER_NEW_PASSWORD ?? 'NativeOther2B',
  };
}

export function uniqueTag(): string {
  return `native-${randomUUID().replaceAll('-', '').slice(0, 16)}`;
}

function localOrigin(value: string): string {
  const url = new URL(value);
  // Loopback, private IPv4 and single-label Docker DNS only. This suite mutates
  // an ephemeral stack; do not accidentally run it against a public PMS.
  const host = url.hostname;
  const local = host === 'localhost' || host === '[::1]' || /^127\./.test(host)
    || /^10\./.test(host) || /^192\.168\./.test(host)
    || /^172\.(1[6-9]|2\d|3[01])\./.test(host) || /^[a-z][a-z0-9-]*$/i.test(host);
  if (!local || !['http:', 'https:'].includes(url.protocol)
    || url.username || url.password || url.pathname !== '/' || url.search || url.hash) {
    throw new Error(`Expected a local stack origin, received ${url.origin}${url.pathname}`);
  }
  return url.origin;
}

export async function status(response: HttpResponse, expected: number): Promise<void> {
  // Do not dump request headers/cookies into our own evidence.
  const detail = response.status() === expected ? '' : (await response.text()).slice(0, 1200);
  expect(response.status(), `${response.url()}: ${detail}`).toBe(expected);
}

export async function json<T>(response: HttpResponse, expected = 200): Promise<T> {
  await status(response, expected);
  return await response.json() as T;
}

export async function csrfHeaders(request: APIRequestContext): Promise<Record<string, string>> {
  const state = await request.storageState();
  const csrf = state.cookies.find(cookie => cookie.name === 'csrf_token'
    && cookie.domain.replace(/^\./, '') === new URL(apiBaseURL).hostname);
  expect(Boolean(csrf?.value), 'Login must provide the real csrf_token cookie').toBe(true);
  return { 'X-CSRF-Token': decodeURIComponent(csrf!.value) };
}

export class PmsApi {
  constructor(readonly request: APIRequestContext) {}

  async get(path: string, headers?: Record<string, string>): Promise<APIResponse> {
    await requestSlot();
    return this.request.get(`${apiBaseURL}${path}`, { headers, timeout: 20_000, maxRedirects: 0 });
  }

  async mutate(method: 'POST' | 'PUT' | 'PATCH' | 'DELETE', path: string,
    data?: unknown, extraHeaders?: Record<string, string>): Promise<APIResponse> {
    await requestSlot();
    return this.request.fetch(`${apiBaseURL}${path}`, {
      method, data, headers: { ...await csrfHeaders(this.request), ...extraHeaders },
      timeout: 30_000, maxRedirects: 0,
    });
  }

  async login(credentials: Credentials): Promise<UserPayload> {
    await requestSlot();
    // Login bootstraps the cookie pair and is an explicit CSRF exemption.
    const result = await json<{ mustChangePassword: boolean }>(await this.request.post(
      `${apiBaseURL}/api/v1/auth/login`, {
        data: { username: credentials.username, password: credentials.password },
        timeout: 20_000, maxRedirects: 0,
      }));
    if (result.mustChangePassword) {
      expect(Boolean(credentials.newPassword), 'Supply a new password for the official change-password flow').toBe(true);
      await status(await this.mutate('POST', '/api/v1/auth/change-password', {
        currentPassword: credentials.password, newPassword: credentials.newPassword,
      }), 200);
    }
    const me = await json<UserPayload>(await this.get('/api/v1/auth/me'));
    expect(me).toMatchObject({ username: credentials.username, mustChangePassword: false });
    return me;
  }

  async settings(): Promise<HotelSettingsResponse> {
    return json<HotelSettingsResponse>(await this.get('/api/v1/stays/settings'));
  }
}

export function waitForApi(page: Page, method: string, path: string, query?: string): Promise<Response> {
  return page.waitForResponse(response => {
    const url = new URL(response.url());
    return response.request().method() === method && url.pathname === path
      && (query === undefined || url.searchParams.get('query') === query);
  });
}

export async function loginUI(page: Page): Promise<PmsApi> {
  const credentials = primaryCredentials();
  await page.goto('/login');
  await page.locator('input[name="username"]').fill(credentials.username);
  await page.locator('input[name="password"]').fill(credentials.password);
  const loginResponse = waitForApi(page, 'POST', '/api/v1/auth/login');
  const meResponse = waitForApi(page, 'GET', '/api/v1/auth/me');
  await page.getByTestId('login-submit').click();
  const result = await json<{ mustChangePassword: boolean }>(await loginResponse);
  expect(result.mustChangePassword, 'Primary UI user must already have completed password setup').toBe(false);
  expect(await json<UserPayload>(await meResponse)).toMatchObject({
    username: credentials.username, role: 'ADMIN', mustChangePassword: false,
  });
  await expect(page).toHaveURL(`${baseURL}/`);
  return new PmsApi(page.request);
}

export function assertPdf(bytes: Buffer): void {
  expect(bytes.length).toBeGreaterThan(1000);
  expect(bytes.subarray(0, 5).toString('ascii')).toBe('%PDF-');
  expect(bytes.subarray(-100).toString('ascii')).toContain('%%EOF');
}

export function stayDates(): { checkInDate: string; checkOutDate: string } {
  // Matches the successful frontdesk gate: tomorrow through +3 days UTC.
  const day = new Date();
  day.setUTCHours(12, 0, 0, 0);
  day.setUTCDate(day.getUTCDate() + 1);
  const checkInDate = day.toISOString().slice(0, 10);
  day.setUTCDate(day.getUTCDate() + 2);
  return { checkInDate, checkOutDate: day.toISOString().slice(0, 10) };
}
