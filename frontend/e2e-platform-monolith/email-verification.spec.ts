import { expect, test } from '@playwright/test';
import { randomUUID } from 'node:crypto';

const monolithUrl = 'http://localhost:8080';
const mailpitUrl = 'http://127.0.0.1:8025';
const frontendUrl = process.env.PLAYWRIGHT_PLATFORM_BASE_URL ?? 'http://localhost:5173';

test('signup, verify, invitation, and reset stay on the configured frontend', async ({ page, request }) => {
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
  const verificationLink = `${message.Text}\n${message.HTML}`
    .match(/https?:\/\/[^\s"'<>]+\/verify-email\?token=[A-Za-z0-9_-]+/)?.[0];
  const parsedVerificationLink = verificationLink ? new URL(verificationLink) : undefined;
  expect(parsedVerificationLink?.origin, 'signup emails must target the configured frontend')
    .toBe(frontendUrl);
  expect(parsedVerificationLink?.pathname).toBe('/verify-email');
  const verificationToken = parsedVerificationLink?.searchParams.get('token');
  expect(
    verificationToken,
    'the delivered verification message must contain its one-time token',
  ).toBeTruthy();

  await page.goto(verificationLink!);
  await expect(page.getByText('Verificación de correo', { exact: true })).toBeVisible();
  await expect(page.getByText(/Spam o correo no deseado/i)).toHaveCount(0);
  await expect(page).toHaveURL(verificationLink!);
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
  await expect(page.getByText('Correo verificado. Ya puedes iniciar sesión.', { exact: true }))
    .toBeVisible();
  await expect(page.getByText(/Spam o correo no deseado/i)).toHaveCount(0);
  const continueToLogin = page.getByRole('link', { name: 'Continuar' });
  await expect(continueToLogin).toBeVisible();
  await continueToLogin.click();
  await expect(page).toHaveURL(`${frontendUrl}/login`);
  await expect(page.getByRole('heading', { name: 'Bienvenido a Fixi' })).toBeVisible();

  const login = await request.post(`${monolithUrl}/api/v1/auth/login`, {
    data: { username: email, password },
    timeout: 15_000,
  });
  expect(login.status(), 'the verified account must be active and able to authenticate').toBe(200);

  const cookies = (await login.headersArray())
    .filter(header => header.name.toLowerCase() === 'set-cookie')
    .map(header => header.value.split(';', 1)[0]);
  const cookieHeader = cookies.join('; ');
  const csrfToken = cookieHeader.match(/(?:^|;\s*)csrf_token=([^;]+)/)?.[1];
  expect(cookieHeader).toContain('jwt=');
  expect(csrfToken).toBeTruthy();
  const currentUser = await request.get(`${monolithUrl}/api/v1/auth/me`, {
    headers: { Cookie: cookieHeader },
  });
  expect(currentUser.status()).toBe(200);
  const owner = await currentUser.json() as { hotelId: string };

  const inviteEmail = `invite-${suffix}@example.test`;
  const invitation = await request.post(`${monolithUrl}/api/v1/auth/invitations`, {
    headers: {
      Cookie: cookieHeader,
      'X-CSRF-Token': csrfToken!,
      'X-Auth-Hotel': owner.hotelId,
    },
    data: { email: inviteEmail, role: 'TECHNICIAN' },
    timeout: 15_000,
  });
  expect(invitation.status(), 'the synthetic owner must be able to invite a technician').toBe(202);

  let invitationMessageId: string | undefined;
  await expect.poll(async () => {
    const search = await request.get(`${mailpitUrl}/api/v1/search`, {
      params: { query: `to:${inviteEmail}`, limit: 20 },
      timeout: 5_000,
    });
    expect(search.status()).toBe(200);
    const result = await search.json() as {
      messages: { ID: string; Subject: string; To: { Address: string }[] }[];
    };
    invitationMessageId = result.messages.find(item => item.Subject === 'Te invitaron a Fixi'
      && item.To.some(recipient => recipient.Address === inviteEmail))?.ID;
    return Boolean(invitationMessageId);
  }, { timeout: 30_000, intervals: [250, 500, 1000, 2000] }).toBe(true);
  const invitationMessageResponse = await request.get(
    `${mailpitUrl}/api/v1/message/${encodeURIComponent(invitationMessageId!)}`,
    { timeout: 5_000 },
  );
  expect(invitationMessageResponse.status()).toBe(200);
  const invitationMessage = await invitationMessageResponse.json() as { Text: string; HTML: string };
  const invitationLink = `${invitationMessage.Text}\n${invitationMessage.HTML}`
    .match(/https?:\/\/[^\s"'<>]+\/accept-invitation\?token=[A-Za-z0-9_-]+/)?.[0];
  const parsedInvitationLink = invitationLink ? new URL(invitationLink) : undefined;
  expect(parsedInvitationLink?.origin, 'invitation emails must target the configured frontend')
    .toBe(frontendUrl);
  expect(parsedInvitationLink?.pathname).toBe('/accept-invitation');
  await page.goto(invitationLink!);
  await expect(page.getByRole('heading', { name: 'Únete al equipo Fixi' })).toBeVisible();
  await page.getByLabel('Nombre completo').fill('Synthetic Invitation E2E');
  await page.getByLabel('Usuario').fill(`tech-${suffix}`);
  await page.getByLabel('Contraseña de Fixi').fill('SyntheticTech1234');
  const acceptInvitation = page.waitForResponse(response =>
    new URL(response.url()).pathname === '/api/v1/auth/public/invitations/accept'
    && response.request().method() === 'POST');
  await page.getByRole('button', { name: 'Crear contraseña y aceptar invitación' }).click();
  expect((await acceptInvitation).status()).toBe(204);
  await expect(page.getByRole('status')).toContainText('Invitación aceptada');
  await page.getByRole('link', { name: 'Iniciar sesión' }).click();
  await expect(page).toHaveURL(`${frontendUrl}/login`);

  const forgot = await request.post(`${monolithUrl}/api/v1/auth/public/forgot-password`, {
    data: { email },
    timeout: 15_000,
  });
  expect(forgot.status()).toBe(202);
  let resetMessageId: string | undefined;
  await expect.poll(async () => {
    const search = await request.get(`${mailpitUrl}/api/v1/search`, {
      params: { query: `to:${email}`, limit: 20 },
      timeout: 5_000,
    });
    expect(search.status()).toBe(200);
    const result = await search.json() as {
      messages: { ID: string; Subject: string; To: { Address: string }[] }[];
    };
    resetMessageId = result.messages.find(item => item.Subject === 'Restablece tu contraseña Fixi'
      && item.To.some(recipient => recipient.Address === email))?.ID;
    return Boolean(resetMessageId);
  }, { timeout: 30_000, intervals: [250, 500, 1000, 2000] }).toBe(true);
  const resetMessageResponse = await request.get(
    `${mailpitUrl}/api/v1/message/${encodeURIComponent(resetMessageId!)}`,
    { timeout: 5_000 },
  );
  expect(resetMessageResponse.status()).toBe(200);
  const resetMessage = await resetMessageResponse.json() as { Text: string; HTML: string };
  const resetLink = `${resetMessage.Text}\n${resetMessage.HTML}`
    .match(/https?:\/\/[^\s"'<>]+\/reset-password\?token=[A-Za-z0-9_-]+/)?.[0];
  const parsedResetLink = resetLink ? new URL(resetLink) : undefined;
  expect(parsedResetLink?.origin, 'password-reset emails must target the configured frontend')
    .toBe(frontendUrl);
  expect(parsedResetLink?.pathname).toBe('/reset-password');
  await page.goto(resetLink!);
  await expect(page.getByRole('heading', { name: 'Nueva contraseña' })).toBeVisible();
  await page.getByPlaceholder('Al menos 6 caracteres y una mayúscula').fill('UpdatedPassword123');
  const resetPassword = page.waitForResponse(response =>
    new URL(response.url()).pathname === '/api/v1/auth/public/reset-password'
    && response.request().method() === 'POST');
  await page.getByRole('button', { name: 'Guardar contraseña' }).click();
  expect((await resetPassword).status()).toBe(204);
  await expect(
    page.getByText('Contraseña actualizada correctamente. Ya puedes iniciar sesión en Fixi.'),
  ).toBeVisible();
  await page.getByRole('link', { name: 'Iniciar sesión' }).click();
  await expect(page).toHaveURL(`${frontendUrl}/login`);

  const resetLogin = await request.post(`${monolithUrl}/api/v1/auth/login`, {
    data: { username: email, password: 'UpdatedPassword123' },
    timeout: 15_000,
  });
  expect(resetLogin.status(), 'the reset account must authenticate with the new password').toBe(200);
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
  await expect(page.getByText(/Spam o correo no deseado/i)).toHaveCount(0);
  await expect(page).toHaveURL(`${monolithUrl}/verify-email`);
  expect(verificationRequests, 'opening the email link must not consume the token').toHaveLength(0);

  const verification = page.waitForResponse(response =>
    response.url().includes('/api/v1/auth/public/verify-email')
    && response.request().method() === 'POST');
  await page.getByRole('button', { name: 'Confirmar correo' }).click();
  expect((await verification).status()).toBe(404);
  await expect(page.getByRole('status').first()).toContainText('El enlace no es válido');
  await expect(page.getByText(/Spam o correo no deseado/i)).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Enviar un nuevo enlace' })).toBeVisible();

  const resend = page.waitForResponse(response =>
    response.url().includes('/api/v1/auth/public/resend-verification')
    && response.request().method() === 'POST');
  await page.getByLabel('Correo de tu cuenta').fill('verification-probe@example.invalid');
  await page.getByRole('button', { name: 'Enviar un nuevo enlace' }).click();
  expect((await resend).status()).toBe(202);
  await expect(page.getByText(/Revisa tu bandeja de entrada.*Spam o correo no deseado/i)).toBeVisible();
  expect(pageErrors, 'verification page scripts should run without browser errors').toEqual([]);
});

test('password reset and invitation pages render and submit through public APIs', async ({ page }) => {
  const pageErrors: string[] = [];
  page.on('pageerror', error => pageErrors.push(error.message));

  await page.goto(`${monolithUrl}/reset-password?token=ci-invalid-reset-page-probe`);
  await expect(page.getByRole('heading', { name: 'Restablece tu contraseña' })).toBeVisible();
  await expect(page.getByText('FIXI', { exact: true })).toBeVisible();
  await expect(page.getByText(/Spam o correo no deseado/i)).toHaveCount(0);
  const resetResponse = page.waitForResponse(response =>
    response.url().includes('/api/v1/auth/public/reset-password')
    && response.request().method() === 'POST');
  await page.getByLabel('Nueva contraseña').fill('NotARealPassword123');
  await page.getByRole('button', { name: 'Guardar contraseña' }).click();
  expect((await resetResponse).status()).toBe(404);
  await expect(page.getByRole('status')).toContainText(/enlace no es válido o venció/i);
  await expect(page.getByRole('link', { name: 'Solicitar otro enlace' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Solicitar otro enlace' }))
    .toHaveAttribute('href', `${frontendUrl}/forgot-password`);
  await expect(page.locator('#login-link')).toHaveAttribute('href', `${frontendUrl}/login`);

  await page.goto(`${monolithUrl}/accept-invitation?token=ci-invalid-invitation-page-probe`);
  await expect(page.getByRole('heading', { name: 'Únete a Fixi' })).toBeVisible();
  await expect(page.getByText('FIXI', { exact: true })).toBeVisible();
  await expect(page.getByText(/Spam o correo no deseado/i)).toHaveCount(0);
  await expect(page.locator('#login-link')).toHaveAttribute('href', `${frontendUrl}/login`);
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
