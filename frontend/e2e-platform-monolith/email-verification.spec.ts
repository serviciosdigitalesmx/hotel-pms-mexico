import { expect, test } from '@playwright/test';
import { randomUUID } from 'node:crypto';

const monolithUrl = 'http://localhost:8080';
const mailpitUrl = 'http://127.0.0.1:8025';

test('a real signup verification email activates the account through the confirmation page', async ({ page, request }) => {
  const suffix = randomUUID().replaceAll('-', '').slice(0, 16);
  const email = `verify-${suffix}@example.test`;
  const password = 'VerificationE2e123';
  const pageErrors: string[] = [];
  const verificationRequests: { method: string; path: string; hasBody: boolean }[] = [];
  page.on('pageerror', error => pageErrors.push(error.message));
  page.on('request', browserRequest => {
    const url = new URL(browserRequest.url());
    if (url.pathname === '/api/v1/auth/public/verify-email') {
      verificationRequests.push({
        method: browserRequest.method(),
        path: url.pathname,
        hasBody: browserRequest.postData() !== null,
      });
    }
  });

  const signup = await request.post(`${monolithUrl}/api/v1/auth/register`, {
    data: {
      ownerName: 'Verification E2E',
      email,
      username: email,
      password,
      businessName: `Verification E2E ${suffix}`,
      slug: `verification-e2e-${suffix}`,
      branchName: 'Sucursal de prueba',
    },
    timeout: 15_000,
  });
  expect(signup.status()).toBe(202);

  let messageId: string | undefined;
  await expect.poll(async () => {
    const search = await request.get(`${mailpitUrl}/api/v1/search`, {
      params: { query: `to:${email}`, limit: 20 },
      timeout: 5_000,
    });
    expect(search.status()).toBe(200);
    const result = await search.json() as {
      messages: { ID: string; Subject: string; To: { Address: string }[] }[];
    };
    messageId = result.messages.find(item => item.Subject === 'Verifica tu cuenta Fixi'
      && item.To.some(recipient => recipient.Address === email))?.ID;
    return Boolean(messageId);
  }, { timeout: 30_000, intervals: [250, 500, 1000, 2000] }).toBe(true);

  const messageResponse = await request.get(
    `${mailpitUrl}/api/v1/message/${encodeURIComponent(messageId!)}`,
    { timeout: 5_000 },
  );
  expect(messageResponse.status()).toBe(200);
  const message = await messageResponse.json() as { Text: string; HTML: string };
  const verificationToken = `${message.Text}\n${message.HTML}`
    .match(/\/verify-email\?token=([A-Za-z0-9_-]+)/)?.[1];
  expect(verificationToken, 'the delivered verification message must contain its one-time token').toBeTruthy();

  await page.goto(`${monolithUrl}/verify-email?token=${encodeURIComponent(verificationToken!)}`);
  await expect(page.getByRole('heading', { name: 'Confirma tu correo' })).toBeVisible();
  await expect(page).toHaveURL(`${monolithUrl}/verify-email`);
  const verification = page.waitForResponse(response => {
    const url = new URL(response.url());
    return url.pathname === '/api/v1/auth/public/verify-email'
      && response.request().method() === 'POST';
  });
  await page.getByRole('button', { name: 'Confirmar correo' }).click();
  expect((await verification).status()).toBe(204);
  expect(verificationRequests).toEqual([{
    method: 'POST',
    path: '/api/v1/auth/public/verify-email',
    hasBody: false,
  }]);
  await expect(page.getByRole('status').first())
    .toContainText('Correo verificado. Tu cuenta quedó activada');
  await expect(page.getByRole('link', { name: 'Ir a iniciar sesión en Fixi' })).toBeVisible();

  const login = await request.post(`${monolithUrl}/api/v1/auth/login`, {
    data: { username: email, password },
    timeout: 15_000,
  });
  expect(login.status(), 'the verified account must be active and able to authenticate').toBe(200);
  expect(pageErrors, 'verification must finish without browser errors').toEqual([]);
});

test('email verification requires an explicit click and reports an invalid link', async ({ page }) => {
  const pageErrors: string[] = [];
  const verificationRequests: number[] = [];
  page.on('pageerror', error => pageErrors.push(error.message));
  page.on('request', request => {
    if (request.url().includes('/api/v1/auth/public/verify-email')) {
      verificationRequests.push(1);
    }
  });

  await page.goto(`${monolithUrl}/verify-email?token=ci-invalid-email-verification-probe`);
  await expect(page.getByRole('heading', { name: 'Confirma tu correo' })).toBeVisible();
  await expect(page.getByText('FIXI', { exact: true })).toBeVisible();
  await expect(page.getByText(/Spam o correo no deseado/)).toBeVisible();
  await expect(page).toHaveURL(`${monolithUrl}/verify-email`);
  expect(verificationRequests, 'opening the email link must not consume the token').toHaveLength(0);

  const verification = page.waitForResponse(response =>
    response.url().includes('/api/v1/auth/public/verify-email')
    && response.request().method() === 'POST');
  await page.getByRole('button', { name: 'Confirmar correo' }).click();
  expect((await verification).status()).toBe(404);
  await expect(page.getByRole('status').first()).toContainText('El enlace no es válido');
  await expect(page.getByRole('button', { name: 'Enviar un nuevo enlace' })).toBeVisible();

  const resend = page.waitForResponse(response =>
    response.url().includes('/api/v1/auth/public/resend-verification')
    && response.request().method() === 'POST');
  await page.getByLabel('Correo de tu cuenta').fill('verification-probe@example.invalid');
  await page.getByRole('button', { name: 'Enviar un nuevo enlace' }).click();
  expect((await resend).status()).toBe(202);
  await expect(page.getByText(/revisa también Spam o correo no deseado/i)).toBeVisible();
  expect(pageErrors, 'verification page scripts should run without browser errors').toEqual([]);
});

test('password reset and invitation pages render and submit through public APIs', async ({ page }) => {
  const pageErrors: string[] = [];
  page.on('pageerror', error => pageErrors.push(error.message));

  await page.goto(`${monolithUrl}/reset-password?token=ci-invalid-reset-page-probe`);
  await expect(page.getByRole('heading', { name: 'Restablece tu contraseña' })).toBeVisible();
  await expect(page.getByText('FIXI', { exact: true })).toBeVisible();
  const resetResponse = page.waitForResponse(response =>
    response.url().includes('/api/v1/auth/public/reset-password')
    && response.request().method() === 'POST');
  await page.getByLabel('Nueva contraseña').fill('NotARealPassword123');
  await page.getByRole('button', { name: 'Guardar contraseña' }).click();
  expect((await resetResponse).status()).toBe(404);
  await expect(page.getByRole('status')).toContainText(/enlace no es válido o venció/i);
  await expect(page.getByRole('link', { name: 'Solicitar otro enlace' })).toBeVisible();

  await page.goto(`${monolithUrl}/accept-invitation?token=ci-invalid-invitation-page-probe`);
  await expect(page.getByRole('heading', { name: 'Únete a Fixi' })).toBeVisible();
  await expect(page.getByText('FIXI', { exact: true })).toBeVisible();
  const invitationResponse = page.waitForResponse(response =>
    response.url().includes('/api/v1/auth/public/invitations/accept')
    && response.request().method() === 'POST');
  await page.getByLabel('Nombre completo').fill('Synthetic Invitee');
  await page.getByLabel('Usuario').fill('synthetic-invitee');
  await page.getByLabel('Contraseña de Fixi').fill('SyntheticInvite123');
  await page.getByRole('button', { name: 'Crear contraseña y aceptar invitación' }).click();
  expect((await invitationResponse).status()).toBe(404);
  await expect(page.getByRole('status')).toContainText(/no se pudo aceptar/i);
  expect(pageErrors, 'account email pages should run without browser errors').toEqual([]);
});
