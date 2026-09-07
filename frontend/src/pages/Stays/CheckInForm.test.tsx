import { render, screen, waitFor, fireEvent } from '@testing-library/react';
/* eslint-disable react-perf/jsx-no-new-array-as-prop */
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import userEvent from '@testing-library/user-event';
import { CheckInForm } from './CheckInForm';
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

const mockStayResponse = (overrides: Partial<StayResponse> = {}): StayResponse => ({
  id: 'stay1',
  reservationId: 'res123',
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

describe('CheckInForm (México)', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(stayService.getLastCompletedStayForGuest).mockResolvedValue(null);
    vi.mocked(guestService.getGuestById).mockResolvedValue({ id: 'g1' } as never);
  });

  const renderComponent = () =>
    render(
      <MemoryRouter
        initialEntries={[{
          pathname: '/stays/check-in/res123',
          state: { guestId: 'g1', roomId: 'r1', expectedGuests: 1, maxOccupancy: 4 },
        }]}
      >
        <Routes>
          <Route path="/stays/check-in/:reservationId" element={<CheckInForm />} />
        </Routes>
      </MemoryRouter>,
    );

  it('renders Mexican guest fields and never requests Alloggiati lookups', async () => {
    renderComponent();
    await waitFor(() => expect(screen.getByText('checkin_title')).toBeInTheDocument());
    expect(screen.getByLabelText(/label_place_of_birth/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/label_citizenship/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/label_guest_type/i)).not.toBeInTheDocument();
    const svc = stayService as unknown as Record<string, unknown>;
    expect(svc.getLookupStati).toBeUndefined();
    expect(svc.getLookupTipdoc).toBeUndefined();
  });

  it('blocks submit when required Mexico fields are missing', async () => {
    vi.mocked(stayService.createStay).mockResolvedValue(mockStayResponse());
    const { container } = renderComponent();
    await waitFor(() => expect(screen.getByText('checkin_title')).toBeInTheDocument());

    fireEvent.submit(container.querySelector('form')!);

    await waitFor(() => {
      expect(screen.getByText('err_first_name_required')).toBeInTheDocument();
    });
    expect(stayService.createStay).not.toHaveBeenCalled();
  });

  it('submits a basic Mexican guest payload', async () => {
    vi.mocked(stayService.createStay).mockResolvedValue(mockStayResponse());
    const { container } = renderComponent();
    await waitFor(() => expect(screen.getByText('checkin_title')).toBeInTheDocument());

    const user = userEvent.setup();
    fireEvent.change(container.querySelector('input[name="firstName"]')!, { target: { value: 'Mario' } });
    fireEvent.change(container.querySelector('input[name="lastName"]')!, { target: { value: 'Rossi' } });
    await user.selectOptions(container.querySelector('select[name="gender"]')!, '1');
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
      placeOfBirth: 'Monterrey',
      citizenship: 'México',
      isPrimaryGuest: true,
    });
    expect(payload.guests[0]).not.toHaveProperty('travellerType');
    expect(payload.guests[0]).not.toHaveProperty('documentType');
  });
});
