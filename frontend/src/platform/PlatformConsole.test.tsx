import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { AxiosHeaders, type AxiosResponse } from 'axios';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import api from '../services/api';
import {
  PlatformDashboardPage,
  PlatformAuditPage,
  PlatformPlansPage,
  PlatformShell,
  PlatformSubscriptionsPage,
  PlatformTenantDetailPage,
  PlatformTenantsPage,
  PlatformUsersPage,
} from './PlatformConsole';

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
  },
}));

const metrics = {
  tenantsTotal: 4,
  tenantsActive: 3,
  tenantsSuspended: 1,
  usersTotal: 8,
  ownersTotal: 4,
  serviceOrdersTotal: 12,
};

const tenant = {
  id: 'tenant-a',
  name: 'Taller A',
  slug: 'taller-a',
  status: 'ACTIVE',
  createdAt: '2026-01-10T00:00:00Z',
  ownerUsername: 'owner-a',
  ownerEmail: 'owner-a@example.test',
  planKey: 'starter',
  planActive: true,
  onboardingCompleted: true,
};

function axiosResponse<T>(data: T): AxiosResponse<T> {
  return {
    data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config: { headers: new AxiosHeaders() },
  };
}

function renderConsole(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/platform" element={<PlatformShell />}>
          <Route index element={<PlatformDashboardPage />} />
          <Route path="tenants" element={<PlatformTenantsPage />} />
          <Route path="tenants/:tenantId" element={<PlatformTenantDetailPage />} />
          <Route path="users" element={<PlatformUsersPage />} />
          <Route path="plans" element={<PlatformPlansPage />} />
          <Route path="subscriptions" element={<PlatformSubscriptionsPage />} />
          <Route path="audit" element={<PlatformAuditPage />} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('Super Admin console', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.clearAllMocks();
    vi.mocked(api.get).mockResolvedValue(axiosResponse(metrics));
  });

  it('does not render the platform shell when the backend denies access', async () => {
    vi.mocked(api.get).mockRejectedValue({
      response: { status: 403, data: { message: 'forbidden' } },
    });

    renderConsole('/platform');

    expect(await screen.findByRole('alert')).toHaveTextContent('forbidden');
    expect(screen.queryByRole('navigation', { name: 'Navegación de plataforma' }))
      .not.toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/api/v1/auth/platform/dashboard');
  });

  it('loads database-backed dashboard metrics only after platform authorization', async () => {
    renderConsole('/platform');

    expect(await screen.findByRole('heading', { name: 'Resumen de plataforma' }))
      .toBeInTheDocument();
    expect(await screen.findAllByText('4')).toHaveLength(2);
    expect(await screen.findByText('12')).toBeInTheDocument();
    expect(screen.getByRole('navigation', { name: 'Navegación de plataforma' }))
      .toBeInTheDocument();
  });

  it('submits tenant onboarding data through the platform endpoint and clears the temporary password', async () => {
    vi.mocked(api.get).mockResolvedValue(axiosResponse({ content: [], page: 0, size: 50, totalElements: 0, totalPages: 0 }));
    vi.mocked(api.post).mockResolvedValue(axiosResponse(tenant));

    renderConsole('/platform/tenants');
    fireEvent.click(await screen.findByRole('button', { name: 'Crear taller' }));
    fireEvent.change(screen.getByLabelText('Nombre del taller'), { target: { value: 'Taller B' } });
    fireEvent.change(screen.getByLabelText('Slug'), { target: { value: 'taller-b' } });
    fireEvent.change(screen.getByLabelText('Usuario owner'), { target: { value: 'owner-b' } });
    fireEvent.change(screen.getByLabelText('Correo owner'), { target: { value: 'owner-b@example.test' } });
    fireEvent.change(screen.getByLabelText('Contraseña temporal'), { target: { value: 'TempPass123' } });
    fireEvent.submit(screen.getByRole('button', { name: 'Crear tenant' }).closest('form')!);

    await waitFor(() => expect(api.post).toHaveBeenCalledWith('/api/v1/auth/platform/hotels', {
      name: 'Taller B',
      slug: 'taller-b',
      ownerUsername: 'owner-b',
      ownerEmail: 'owner-b@example.test',
      initialPassword: 'TempPass123',
    }));
    await waitFor(() => expect(screen.queryByLabelText('Contraseña temporal')).not.toBeInTheDocument());
  });

  it('applies tenant search and lifecycle filters and follows bounded pagination', async () => {
    vi.mocked(api.get).mockImplementation(async (url: string) => axiosResponse(
      url.includes('/tenants?')
        ? {
            content: [{ ...tenant, status: 'SUSPENDED' }],
            page: Number(new URLSearchParams(url.split('?')[1]).get('page')),
            size: 50,
            totalElements: 51,
            totalPages: 2,
          }
        : metrics,
    ));

    renderConsole('/platform/tenants');
    expect(await screen.findByText('Taller A')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('Buscar talleres'), { target: { value: 'taller-a' } });
    fireEvent.change(screen.getByLabelText('Filtrar estado'), { target: { value: 'SUSPENDED' } });

    await waitFor(() => expect(api.get).toHaveBeenLastCalledWith(
      expect.stringContaining('search=taller-a'),
    ));
    expect(api.get).toHaveBeenLastCalledWith(
      expect.stringContaining('status=SUSPENDED'),
    );
    fireEvent.click(await screen.findByRole('button', { name: 'Siguiente' }));
    await waitFor(() => expect(api.get).toHaveBeenLastCalledWith(
      expect.stringContaining('page=1'),
    ));
  });

  it('requires confirmation for suspension and reactivation and persists both transitions', async () => {
    const suspendedTenant = { ...tenant, status: 'SUSPENDED' };
    vi.mocked(api.get).mockImplementation(async (url: string) => axiosResponse(
      url.endsWith('/subscriptions?page=0&size=20')
        ? { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }
        : tenant,
    ));
    vi.mocked(api.patch)
      .mockResolvedValueOnce(axiosResponse(suspendedTenant))
      .mockResolvedValueOnce(axiosResponse(tenant));
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    renderConsole('/platform/tenants/tenant-a');
    fireEvent.click(await screen.findByRole('button', { name: 'Suspender taller' }));

    await waitFor(() => expect(api.patch).toHaveBeenNthCalledWith(
      1,
      '/api/v1/auth/platform/tenants/tenant-a/status',
      { status: 'SUSPENDED' },
    ));
    expect(window.confirm).toHaveBeenCalledWith(
      'Suspender este taller? Sus usuarios dejarán de operar, sin borrar datos.',
    );
    fireEvent.click(await screen.findByRole('button', { name: 'Reactivar taller' }));

    await waitFor(() => expect(api.patch).toHaveBeenNthCalledWith(
      2,
      '/api/v1/auth/platform/tenants/tenant-a/status',
      { status: 'ACTIVE' },
    ));
    expect(window.confirm).toHaveBeenLastCalledWith('Reactivar este taller?');
  });

  it('renders safe global user fields without exposing credential or token fields', async () => {
    vi.mocked(api.get).mockImplementation(async (url: string) => axiosResponse(
      url.includes('/users?')
        ? {
            content: [{
              id: 'user-a', username: 'owner-a', email: 'owner-a@example.test', role: 'OWNER',
              active: true, tenantId: 'tenant-a', tenantName: 'Taller A', createdAt: '2026-01-10T00:00:00Z',
            }],
            page: 0, size: 100, totalElements: 1, totalPages: 1,
          }
        : metrics,
    ));

    renderConsole('/platform/users');

    expect(await screen.findByText(/owner-a@example\.test/)).toBeInTheDocument();
    expect(screen.queryByText(/passwordHash|refreshToken|tokenVersion/i)).not.toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/api/v1/auth/platform/users?'));
  });

  it('saves an administrative plan assignment without implying payment processing', async () => {
    vi.mocked(api.get).mockImplementation(async (url: string) => axiosResponse(
      url.includes('/subscriptions')
        ? { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }
        : tenant,
    ));
    vi.mocked(api.put).mockResolvedValue(axiosResponse({ ...tenant, planKey: 'growth', planActive: true }));

    renderConsole('/platform/tenants/tenant-a');
    fireEvent.change(await screen.findByLabelText('Plan key'), { target: { value: 'growth' } });
    fireEvent.submit(screen.getByRole('button', { name: 'Guardar plan' }).closest('form')!);

    await waitFor(() => expect(api.put).toHaveBeenCalledWith(
      '/api/v1/auth/platform/tenants/tenant-a/plan',
      { planKey: 'growth' },
    ));
    expect(screen.getByText(/no cobra ni crea una suscripción con proveedor externo/i))
      .toBeInTheDocument();
  });

  it('renders persisted plans, subscription history, and audit events from their platform APIs', async () => {
    vi.mocked(api.get).mockImplementation(async (url: string) => {
      if (url.includes('/dashboard')) return axiosResponse(metrics);
      if (url.includes('/plans')) return axiosResponse([{ planKey: 'growth', tenantsAssigned: 1 }]);
      if (url.includes('/subscriptions')) {
        return axiosResponse({ content: [{
          id: 'subscription-a', tenantId: 'tenant-a', tenantName: 'Taller A', planKey: 'growth',
          status: 'ACTIVE', startsAt: '2026-01-10T00:00:00Z', createdBy: 'platform-operator',
        }], page: 0, size: 100, totalElements: 1, totalPages: 1 });
      }
      return axiosResponse({ content: [{
        id: 'audit-a', actorUsername: 'platform-operator', action: 'TENANT_SUSPENDED',
        targetTenantId: 'tenant-a', occurredAt: '2026-01-10T00:00:00Z',
        metadataJson: '{"previousStatus":"ACTIVE","status":"SUSPENDED"}',
      }], page: 0, size: 100, totalElements: 1, totalPages: 1 });
    });

    const plans = renderConsole('/platform/plans');
    expect(await screen.findByText('growth')).toBeInTheDocument();
    plans.unmount();

    const subscriptions = renderConsole('/platform/subscriptions');
    expect(await screen.findByText('platform-operator')).toBeInTheDocument();
    subscriptions.unmount();

    renderConsole('/platform/audit');
    expect(await screen.findByText('TENANT_SUSPENDED')).toBeInTheDocument();
    expect(screen.getByText('{"previousStatus":"ACTIVE","status":"SUSPENDED"}'))
      .toBeInTheDocument();
  });
});
