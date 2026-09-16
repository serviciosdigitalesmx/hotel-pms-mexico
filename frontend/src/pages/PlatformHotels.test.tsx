import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import api from '../services/api';
import { PlatformHotels } from './PlatformHotels';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));

const translate = vi.hoisted(() => (key: string) => key);
vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: translate }),
}));

describe('PlatformHotels', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('lists existing hotels and creates a new tenant with its owner', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [{ id: 'a', name: 'Hotel A', slug: 'hotel-a' }] });
    vi.mocked(api.post).mockResolvedValue({ data: { id: 'b', name: 'Hotel B', slug: 'hotel-b' } });
    render(<PlatformHotels />);

    expect(await screen.findByText('Hotel A')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('platform_name'), { target: { value: 'Hotel B' } });
    fireEvent.change(screen.getByLabelText('platform_slug'), { target: { value: 'hotel-b' } });
    fireEvent.change(screen.getByLabelText('platform_ownerUsername'), { target: { value: 'owner-b' } });
    fireEvent.change(screen.getByLabelText('platform_ownerEmail'), { target: { value: 'owner-b@example.test' } });
    fireEvent.change(screen.getByLabelText('platform_initialPassword'), { target: { value: 'Temporary1A' } });
    fireEvent.submit(screen.getByRole('button', { name: 'platform_create' }).closest('form')!);

    await waitFor(() => expect(api.post).toHaveBeenCalledWith('/api/v1/auth/platform/hotels', {
      name: 'Hotel B', slug: 'hotel-b', ownerUsername: 'owner-b',
      ownerEmail: 'owner-b@example.test', initialPassword: 'Temporary1A',
    }));
    expect(await screen.findByText('Hotel B')).toBeInTheDocument();
    expect(screen.getByLabelText('platform_initialPassword')).toHaveValue('');
  });

  it('shows an error when loading fails', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('Unavailable'));
    render(<PlatformHotels />);
    expect(await screen.findByRole('alert')).toHaveTextContent('platform_load_error');
  });

  it('shows an error when creation fails and does not add a hotel', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });
    vi.mocked(api.post).mockRejectedValue(new Error('Conflict'));
    render(<PlatformHotels />);
    fireEvent.change(screen.getByLabelText('platform_name'), { target: { value: 'Hotel B' } });
    fireEvent.submit(screen.getByRole('button', { name: 'platform_create' }).closest('form')!);
    expect(await screen.findByRole('alert')).toHaveTextContent('platform_create_error');
    expect(screen.queryByText('Hotel B')).not.toBeInTheDocument();
  });
});
