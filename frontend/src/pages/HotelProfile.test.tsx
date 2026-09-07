import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { HotelProfile } from './HotelProfile';
import { stayService } from '../services/stayService';

const mockAddToast = vi.fn();
const mockLoadHotelSettings = vi.fn().mockResolvedValue(undefined);

vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key }),
  initReactI18next: { type: '3rdParty', init: vi.fn() },
}));

vi.mock('../services/stayService');
vi.mock('../store/toastStore', () => ({
  useToastStore: () => ({ addToast: mockAddToast }),
}));
vi.mock('../store/settingsStore', () => ({
  useSettingsStore: (selector: (state: { loadHotelSettings: () => Promise<void> }) => unknown) =>
    selector({ loadHotelSettings: mockLoadHotelSettings }),
}));

const SETTINGS = {
  hotelId: 'h-001',
  hotelName: 'Hotel Palmas',
  address: 'Av. Principal 123',
  vatNumber: 'ABC123456EF7',
  fiscalCode: 'FISCAL-1',
  logoUrl: '',
  city: 'General Escobedo',
  state: 'Nuevo León',
  country: 'México',
  postalCode: '66050',
  currency: 'MXN',
  locale: 'es-MX',
  timezone: 'America/Monterrey',
  publicSlug: 'hotel-palmas',
};

describe('HotelProfile (México)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(stayService.getHotelSettings).mockResolvedValue(SETTINGS as never);
  });

  it('renders the México profile without Alloggiati fields', async () => {
    render(<HotelProfile />);
    await waitFor(() => expect(screen.getByLabelText(/label_hotel_name/i)).toHaveValue('Hotel Palmas'));
    expect(screen.getByLabelText(/label_vat_number/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/label_alloggiati_auto_send/i)).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/label_alloggiati_username/i)).not.toBeInTheDocument();
    expect(screen.queryByText('section_title_alloggiati_credentials')).not.toBeInTheDocument();
  });

  it('saves profile and refreshes branding without sending Alloggiati payload', async () => {
    vi.mocked(stayService.updateHotelSettings).mockResolvedValue(SETTINGS as never);
    render(<HotelProfile />);
    await waitFor(() => expect(screen.getByLabelText(/label_hotel_name/i)).toHaveValue('Hotel Palmas'));

    fireEvent.change(screen.getByLabelText(/label_hotel_name/i), { target: { value: 'Hotel Palmas Nuevo' } });
    fireEvent.click(screen.getByText('btn_save_profile'));

    await waitFor(() => expect(stayService.updateHotelSettings).toHaveBeenCalled());
    const request = vi.mocked(stayService.updateHotelSettings).mock.calls[0][0] as Record<string, unknown>;
    expect(request.hotelName).toBe('Hotel Palmas Nuevo');
    expect(request).not.toHaveProperty('alloggiatiAutoSend');
    expect(request).not.toHaveProperty('alloggiatiUsername');
    expect(mockLoadHotelSettings).toHaveBeenCalled();
  });

  it('blocks save with an invalid RFC', async () => {
    vi.mocked(stayService.updateHotelSettings).mockResolvedValue(SETTINGS as never);
    render(<HotelProfile />);
    await waitFor(() => expect(screen.getByLabelText(/label_vat_number/i)).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText(/label_vat_number/i), { target: { value: 'not-an-rfc' } });
    fireEvent.click(screen.getByText('btn_save_profile'));

    expect(await screen.findByText('common:err_invalid_vat')).toBeInTheDocument();
    expect(stayService.updateHotelSettings).not.toHaveBeenCalled();
  });
});
