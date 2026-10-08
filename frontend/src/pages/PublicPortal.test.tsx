/* eslint-disable react-perf/jsx-no-new-array-as-prop */
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PublicPortal } from './PublicPortal';
import api from '../services/api';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));

const mockedApi = vi.mocked(api);

describe('PublicPortal', () => {
  beforeEach(() => vi.clearAllMocks());

  it('loads a scoped operation and requests a simulated OTP', async () => {
    mockedApi.get.mockResolvedValueOnce({
      data: { resourceType: 'SERVICE_ORDER', resourceId: 'order-1', customerId: 'customer-1', expiresAt: '2099-01-01' },
    });
    mockedApi.post.mockResolvedValueOnce({ data: { testCode: '123456' } });

    render(<MemoryRouter initialEntries={['/public/portal/token-1']}>
      <Routes><Route path="/public/portal/:token" element={<PublicPortal />} /></Routes>
    </MemoryRouter>);

    await waitFor(() => expect(screen.getByText(/SERVICE_ORDER/)).toBeInTheDocument());
    screen.getByRole('button', { name: 'Request OTP' }).click();
    await waitFor(() => expect(screen.getByText(/123456/)).toBeInTheDocument());
    expect(mockedApi.get).toHaveBeenCalledWith('/api/public/portal/operations/token-1');
  });
});
