import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import userEvent from '@testing-library/user-event';
import { WalkInCheckInForm } from './WalkInCheckInForm';
import { stayService } from '../../services/stayService';
import { guestService } from '../../services/guestService';
import type { StayResponse } from '../../types/stay.types';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
    i18n: { language: 'en' },
  }),
  initReactI18next: { type: '3rdParty', init: vi.fn() },
}));

vi.mock('../../services/stayService');
vi.mock('../../services/guestService');
vi.mock('../../store/toastStore', () => ({
  useToastStore: () => ({ addToast: vi.fn() }),
}));

const mockStayResponse = (overrides: Partial<StayResponse> = {}): StayResponse => ({
  id: 'stay1',
  reservationId: '',
  guestId: 'g1',
  roomId: 'r1',
  status: 'CHECKED_IN',
  alloggiatiSent: false,
  alloggiatiSendFailed: false,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
  invoiceCreationFailed: false,
  checkoutEmailFailed: false,
  ...overrides,
});

const renderComponent = () =>
  render(
    <MemoryRouter>
      <WalkInCheckInForm />
    </MemoryRouter>,
  );

describe('WalkInCheckInForm (México)', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(stayService.getAvailableRooms).mockResolvedValue([
      {
        id: 'r1',
        roomNumber: '101',
        status: 'CLEAN',
        roomType: { name: 'Standard', maxOccupancy: 3 },
      },
    ]);
    vi.mocked(guestService.searchGuests).mockResolvedValue([]);
  });

  it('renders room, guest search, checkout date and Mexican guest fields only', async () => {
    renderComponent();
    await waitFor(() => expect(screen.getByLabelText(/walkin_label_room/i)).toBeInTheDocument());
    expect(screen.getByLabelText(/walkin_label_guest/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/walkin_label_checkout_date/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/label_place_of_birth/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/label_guest_type/i)).not.toBeInTheDocument();
    const svc = stayService as unknown as Record<string, unknown>;
    expect(svc.getLookupStati).toBeUndefined();
    expect(svc.getLookupTipdoc).toBeUndefined();
  });

  it('blocks submit when no room is selected', async () => {
    vi.mocked(stayService.createStay).mockResolvedValue(mockStayResponse());
    const { container } = renderComponent();
    await waitFor(() => expect(screen.getByLabelText(/walkin_label_room/i)).toBeInTheDocument());

    fireEvent.submit(container.querySelector('form')!);

    await waitFor(() => {
      expect(screen.getByText('walkin_err_room_required')).toBeInTheDocument();
    });
    expect(stayService.createStay).not.toHaveBeenCalled();
  });

  it('submits basic Mexican guest data without Alloggiati fields', async () => {
    vi.mocked(stayService.createStay).mockResolvedValue(mockStayResponse());
    vi.mocked(guestService.searchGuests).mockResolvedValue([
      {
        id: 'g1',
        firstName: 'Mario',
        lastName: 'Rossi',
        email: 'mario@test.com',
        createdAt: '2026-01-01T00:00:00',
        updatedAt: '2026-01-01T00:00:00',
        active: true,
      },
    ]);
    const { container } = renderComponent();
    await waitFor(() => expect(screen.getByLabelText(/walkin_label_room/i)).toBeInTheDocument());

    const user = userEvent.setup();
    await user.selectOptions(screen.getByLabelText(/walkin_label_room/i), 'r1');

    const guestInput = screen.getByPlaceholderText('walkin_placeholder_guest');
    await user.type(guestInput, 'Ma');
    await waitFor(() => expect(screen.getByText(/Mario/)).toBeInTheDocument(), { timeout: 5000 });
    await user.click(screen.getByRole('button', { name: /Mario/ }));

    await user.type(screen.getByLabelText(/walkin_label_checkout_date/i), '2026-12-31');
    await user.selectOptions(container.querySelector('select[name="gender"]')!, '1');
    fireEvent.change(container.querySelector('input[name="firstName"]')!, { target: { value: 'Mario' } });
    fireEvent.change(container.querySelector('input[name="lastName"]')!, { target: { value: 'Rossi' } });
    fireEvent.change(container.querySelector('input[name="dateOfBirth"]')!, { target: { value: '1980-01-01' } });
    fireEvent.change(container.querySelector('input[name="placeOfBirth"]')!, { target: { value: 'Monterrey' } });
    fireEvent.change(container.querySelector('input[name="citizenship"]')!, { target: { value: 'México' } });

    fireEvent.submit(container.querySelector('form')!);

    await waitFor(() => expect(stayService.createStay).toHaveBeenCalled());
    const payload = vi.mocked(stayService.createStay).mock.calls[0][0];
    expect(payload.guests[0]).toMatchObject({
      firstName: 'Mario',
      lastName: 'Rossi',
      gender: '1',
      dateOfBirth: '1980-01-01',
      placeOfBirth: 'Monterrey',
      citizenship: 'México',
      isPrimaryGuest: true,
    });
    expect(payload.guests[0]).not.toHaveProperty('travellerType');
    expect(payload.guests[0]).not.toHaveProperty('documentType');
  });
});
