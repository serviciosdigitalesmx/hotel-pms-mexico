import { expect, test } from '@playwright/test';

const platformAdmin = {
  username: process.env.PLATFORM_E2E_ADMIN_USERNAME ?? 'e2e-live-other-hotel-admin',
  password: process.env.PLATFORM_E2E_ADMIN_PASSWORD ?? 'password',
};
const tenantAdmin = {
  username: process.env.PLATFORM_E2E_TENANT_USERNAME ?? 'admin',
  password: process.env.PLATFORM_E2E_TENANT_PASSWORD ?? 'password',
  newPassword: process.env.PLATFORM_E2E_TENANT_NEW_PASSWORD ?? 'FixiTenant123',
};

test('Super Admin uses the real monolith platform console; tenant admin is denied', async ({ browser, page }) => {
  const runtimeErrors: string[] = [];
  const dashboardStatuses: number[] = [];
  page.on('pageerror', error => runtimeErrors.push(error.message));
  page.on('response', response => {
    if (response.url().includes('/api/v1/auth/platform/dashboard')) {
      dashboardStatuses.push(response.status());
    }
  });

  await page.goto('/login');
  await page.getByLabel('Correo o usuario').fill(platformAdmin.username);
  await page.getByLabel('Contraseña').fill(platformAdmin.password);
  await page.getByTestId('login-submit').click();
  await expect(page.getByRole('heading', { name: 'Dashboard Fixi' })).toBeVisible();

  await page.goto('/platform');
  await expect(page.getByText('Consola Super Admin')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Resumen de plataforma' })).toBeVisible();
  await expect(page.getByText('Talleres registrados')).toBeVisible();
  await expect.poll(() => dashboardStatuses.length).toBeGreaterThanOrEqual(2);
  expect(dashboardStatuses.every(status => status === 200), 'platform dashboard requests should succeed').toBe(true);

  const sections = [
    { link: 'Talleres', heading: 'Talleres', api: '/api/v1/auth/platform/tenants' },
    { link: 'Usuarios', heading: 'Usuarios globales', api: '/api/v1/auth/platform/users' },
    { link: 'Planes', heading: 'Planes', api: '/api/v1/auth/platform/plans' },
    { link: 'Auditoría', heading: 'Auditoría de plataforma', api: '/api/v1/auth/platform/audit' },
  ];

  for (const section of sections) {
    // Respect the actual platform endpoint token-bucket refill between user navigations.
    await page.waitForTimeout(650);
    const response = page.waitForResponse(result =>
      result.url().includes(section.api) && result.request().method() === 'GET');
    await page.getByRole('link', { name: section.link, exact: true }).click();
    await expect(page.getByRole('heading', { name: section.heading })).toBeVisible();
    expect((await response).status(), `${section.api} should succeed for SUPER_ADMIN`).toBe(200);
  }

  const tenantContext = await browser.newContext();
  const tenantPage = await tenantContext.newPage();
  const tenantRuntimeErrors: string[] = [];
  tenantPage.on('pageerror', error => tenantRuntimeErrors.push(error.message));

  await tenantPage.goto('/login');
  await tenantPage.getByLabel('Correo o usuario').fill(tenantAdmin.username);
  await tenantPage.getByLabel('Contraseña').fill(tenantAdmin.password);
  await tenantPage.getByTestId('login-submit').click();
  await expect(tenantPage).toHaveURL(/\/change-password$/);
  await tenantPage.getByLabel('Contraseña actual o temporal').fill(tenantAdmin.password);
  await tenantPage.getByLabel('Nueva contraseña', { exact: true }).fill(tenantAdmin.newPassword);
  await tenantPage.getByLabel('Confirma la nueva contraseña', { exact: true }).fill(tenantAdmin.newPassword);
  await tenantPage.getByRole('button', { name: 'Actualizar contraseña' }).click();
  await expect(tenantPage.getByRole('heading', { name: 'Dashboard Fixi' })).toBeVisible();

  await tenantPage.goto('/platform');
  const deniedResponse = tenantPage.waitForResponse(result =>
    result.url().includes('/api/v1/auth/platform/dashboard') && result.request().method() === 'GET');
  await expect(tenantPage.getByRole('alert')).toContainText('HTTP 403');
  expect((await deniedResponse).status(), 'tenant administrator must be rejected by the backend').toBe(403);

  expect(runtimeErrors, 'Super Admin browser should have no uncaught runtime errors').toEqual([]);
  expect(tenantRuntimeErrors, 'tenant browser should have no uncaught runtime errors').toEqual([]);
  await tenantContext.close();
});
