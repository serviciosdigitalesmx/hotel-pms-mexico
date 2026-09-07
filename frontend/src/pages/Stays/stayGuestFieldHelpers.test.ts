import { describe, expect, it } from 'vitest';
import { emptyGuest, validateCheckInGuests, type IdentifiableGuest } from './stayGuestFieldHelpers';

const t = (key: string): string => key;

function validGuest(overrides: Partial<IdentifiableGuest> = {}): IdentifiableGuest {
  return {
    ...emptyGuest(true),
    firstName: 'Mario',
    lastName: 'Rossi',
    gender: '1',
    dateOfBirth: '1990-01-01',
    placeOfBirth: 'Monterrey, México',
    citizenship: 'México',
    ...overrides,
  };
}

describe('validateCheckInGuests', () => {
  it('accepts a fully valid single-guest list', () => {
    expect(validateCheckInGuests([validGuest()], t)).toBeNull();
  });

  it('rejects when no guest is marked primary', () => {
    const guest = validGuest({ isPrimaryGuest: false });
    expect(validateCheckInGuests([guest], t)).toBe('err_primary_guest_required');
  });

  it('requires the basic Mexico guest fields', () => {
    expect(validateCheckInGuests([emptyGuest(true)], t)).toBe('err_first_name_required');
    expect(validateCheckInGuests([validGuest({ firstName: '' })], t)).toBe('err_first_name_required');
    expect(validateCheckInGuests([validGuest({ lastName: '' })], t)).toBe('err_last_name_required');
    expect(validateCheckInGuests([validGuest({ gender: '' })], t)).toBe('err_gender_required');
    expect(validateCheckInGuests([validGuest({ dateOfBirth: '' })], t)).toBe('err_date_of_birth_required');
    expect(validateCheckInGuests([validGuest({ placeOfBirth: '  ' })], t)).toBe('err_place_of_birth_required');
    expect(validateCheckInGuests([validGuest({ citizenship: '  ' })], t)).toBe('err_citizenship_required');
  });
});
