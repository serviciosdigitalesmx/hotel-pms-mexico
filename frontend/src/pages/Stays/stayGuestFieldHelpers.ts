import { z } from 'zod';

export interface IdentifiableGuest {
  _id: string;
  firstName: string;
  lastName: string;
  /** "1" = male, "2" = female */
  gender: string;
  dateOfBirth: string;
  placeOfBirth: string;
  citizenship: string;
  isPrimaryGuest: boolean;
  travelPurpose: string;
}

export const emptyGuest = (isPrimary: boolean): IdentifiableGuest => ({
  _id: Math.random().toString(36).substring(2, 11),
  firstName: '',
  lastName: '',
  gender: '',
  dateOfBirth: '',
  placeOfBirth: '',
  citizenship: '',
  isPrimaryGuest: isPrimary,
  travelPurpose: '',
});

type GuestErrorTranslator = (key: string, options?: Record<string, unknown>) => string;

const buildGuestsSchema = (t: GuestErrorTranslator) =>
  z.array(z.custom<IdentifiableGuest>()).superRefine((guests, ctx) => {
    if (!guests.some((g) => g.isPrimaryGuest)) {
      ctx.addIssue({ code: 'custom', path: [], message: t('err_primary_guest_required') });
    }

    guests.forEach((g, idx) => {
      const number = idx + 1;
      if (!g.firstName.trim()) {
        ctx.addIssue({ code: 'custom', path: [idx, 'firstName'], message: t('err_first_name_required', { number }) });
      }
      if (!g.lastName.trim()) {
        ctx.addIssue({ code: 'custom', path: [idx, 'lastName'], message: t('err_last_name_required', { number }) });
      }
      if (!g.gender) {
        ctx.addIssue({ code: 'custom', path: [idx, 'gender'], message: t('err_gender_required', { number }) });
      }
      if (!g.dateOfBirth) {
        ctx.addIssue({ code: 'custom', path: [idx, 'dateOfBirth'], message: t('err_date_of_birth_required', { number }) });
      }
      if (!g.placeOfBirth.trim()) {
        ctx.addIssue({ code: 'custom', path: [idx, 'placeOfBirth'], message: t('err_place_of_birth_required', { number }) });
      }
      if (!g.citizenship.trim()) {
        ctx.addIssue({ code: 'custom', path: [idx, 'citizenship'], message: t('err_citizenship_required', { number }) });
      }
    });
  });

/**
 * Validates the guest list against the México check-in contract.
 * Returns the first violation message, or null when the list is valid.
 */
export const validateCheckInGuests = (
  guests: IdentifiableGuest[],
  t: GuestErrorTranslator,
): string | null => {
  const result = buildGuestsSchema(t).safeParse(guests);
  return result.success ? null : (result.error.issues[0]?.message ?? null);
};
