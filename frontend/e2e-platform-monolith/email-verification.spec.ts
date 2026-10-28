import { expect, test } from '@playwright/test';

const monolithUrl = 'http://localhost:8080';

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
