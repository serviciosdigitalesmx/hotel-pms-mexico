export type StayStatus = 'EXPECTED' | 'CHECKED_IN' | 'CHECKED_OUT';

export interface AvailableRoom {
  id: string;
  roomNumber: string;
  status: string;
  roomType?: { name: string; basePrice?: number; maxOccupancy?: number } | null;
}

export type TravellerType =
  | 'OSPITE_SINGOLO'
  | 'CAPOFAMIGLIA'
  | 'CAPOGRUPPO'
  | 'FAMILIARE'
  | 'MEMBRO_GRUPPO';

export interface AlloggiatiStato {
  codice: string;
  descrizione: string;
  dataFineVal?: string | null;
}

export interface AlloggiatiComune {
  codice: string;
  descrizione: string;
  provincia: string;
  dataFineVal?: string | null;
}

export interface AlloggiatiTipdoc {
  codice: string;
  descrizione: string;
}

export interface StayGuestResponse {
  id: string;
  firstName: string;
  lastName: string;
  /** "1" = Maschio, "2" = Femmina */
  gender: string;
  dateOfBirth: string;
  /** 9-char comune code (Italian-born) or 9-char stato code (foreign-born) */
  placeOfBirth: string;
  /** 9-char stato code from the Portale Alloggiati Web lookup */
  citizenship: string;
  /** 5-char tipdoc code — null for FAMILIARE/MEMBRO_GRUPPO */
  documentType?: string | null;
  documentNumber?: string | null;
  /** 9-char comune or stato code — null for FAMILIARE/MEMBRO_GRUPPO */
  documentPlaceOfIssue?: string | null;
  isPrimaryGuest: boolean;
  travellerType?: TravellerType;
  travelPurpose?: string;
}

export interface StayGuestRequest {
  firstName: string;
  lastName: string;
  /** "1" = Maschio, "2" = Femmina */
  gender: string;
  dateOfBirth: string;
  /** 9-char comune code (Italian-born) or 9-char stato code (foreign-born) */
  placeOfBirth: string;
  /** 9-char stato code */
  citizenship: string;
  /** 5-char tipdoc code — omit for FAMILIARE/MEMBRO_GRUPPO */
  documentType?: string;
  documentNumber?: string;
  /** 9-char comune or stato code — omit for FAMILIARE/MEMBRO_GRUPPO */
  documentPlaceOfIssue?: string;
  isPrimaryGuest: boolean;
  travellerType?: TravellerType;
  travelPurpose?: string;
}

export interface StayRequest {
  hotelId?: string;
  /** Null for walk-in check-ins (no reservation). */
  reservationId?: string;
  guestId: string;
  roomId: string;
  status: StayStatus;
  /** Required for walk-in check-ins (ISO date string YYYY-MM-DD). */
  expectedCheckOutDate?: string;
  actualCheckInTime?: string;
  actualCheckOutTime?: string;
  occupantCount: number;
  guests: StayGuestRequest[];
}

export interface StayResponse {
  id: string; // UUID
  reservationId: string;
  guestId: string;
  roomId: string;
  status: StayStatus;
  actualCheckInTime?: string;
  actualCheckOutTime?: string;
  createdAt: string;
  updatedAt: string;
  alloggiatiSent: boolean;
  /** Whether the most recent Alloggiati Web submission attempt for this stay failed. */
  alloggiatiSendFailed: boolean;
  /** Error message from the most recent failed attempt; null once resolved. */
  alloggiatiFailureReason?: string | null;
  guests?: StayGuestResponse[];
  /** Denormalized "Cognome Nome" set at check-in; null for legacy stays. */
  guestDisplayName?: string | null;
  /** Denormalized room number set at check-in; null for legacy stays. */
  roomNumber?: string | null;
  /** Expected check-out date sourced from the reservation (or walk-in request) at check-in; null for legacy stays. */
  expectedCheckOutDate?: string | null;
  /** Total room occupants; companion personal data is optional. */
  occupantCount?: number;
  /** Whether the most recent billing-invoice-creation attempt at check-in failed. */
  invoiceCreationFailed: boolean;
  /** Error message from the most recent failed invoice-creation attempt; null once resolved. */
  invoiceCreationFailureReason?: string | null;
  /** Whether the most recent checkout summary email attempt failed. */
  checkoutEmailFailed: boolean;
  /** Error message from the most recent failed checkout email attempt; null once resolved. */
  checkoutEmailFailureReason?: string | null;
}

/** Summary of unresolved Alloggiati Web submission failures for the caller's hotel. */
export interface AlloggiatiFailureSummaryResponse {
  failedCount: number;
  mostRecentFailureAt?: string | null;
  mostRecentFailureReason?: string | null;
}

export interface HotelSettingsRequest {
  /** Undefined = leave unchanged (partial-patch semantics on every field here). */
  alloggiatiAutoSend?: boolean;
  hotelName?: string;
  address?: string;
  vatNumber?: string;
  fiscalCode?: string;
  logoUrl?: string;
  alloggiatiUsername?: string;
  /** Write-only: blank/undefined leaves the currently stored password unchanged. */
  alloggiatiPassword?: string;
  /** Write-only: blank/undefined leaves the currently stored WsKey unchanged. */
  alloggiatiWsKey?: string;
  /** Undefined = leave unchanged. Whether the guest is emailed on reservation creation. */
  sendReservationConfirmedEmail?: boolean;
  /** Undefined = leave unchanged. Whether the guest is emailed a summary at check-out. */
  sendCheckoutEmail?: boolean;
  /** Custom subject line for the reservation-confirmed email; blank/undefined = default. */
  emailSubjectReservationConfirmed?: string;
  /** Custom subject line for the checkout email; blank/undefined = default. */
  emailSubjectCheckout?: string;
  /** Greeting/signature line appended to every transactional email footer. */
  emailGreetingText?: string;
  /** CAP — Italian 5-digit postal code. Required only to export a valid FatturaPA XML. */
  cap?: string;
  /** Comune — municipality name, validated together with provincia. */
  comune?: string;
  /** Provincia — 2-letter province code, e.g. "RM". */
  provincia?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
  currency?: string;
  locale?: string;
  timezone?: string;
  publicSlug?: string;
  aiEnabled?: boolean;
  aiModel?: string;
  /** Write-only DeepSeek API key. Blank/undefined preserves the stored key. */
  aiApiKey?: string;
  aiInstructions?: string;
}

export interface HotelSettingsResponse {
  hotelId: string;
  alloggiatiAutoSend: boolean;
  hotelName?: string | null;
  address?: string | null;
  vatNumber?: string | null;
  fiscalCode?: string | null;
  logoUrl?: string | null;
  alloggiatiUsername?: string | null;
  alloggiatiCredentialsConfigured: boolean;
  sendReservationConfirmedEmail: boolean;
  sendCheckoutEmail: boolean;
  emailSubjectReservationConfirmed?: string | null;
  emailSubjectCheckout?: string | null;
  emailGreetingText?: string | null;
  cap?: string | null;
  comune?: string | null;
  provincia?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  postalCode?: string | null;
  currency?: string | null;
  locale?: string | null;
  timezone?: string | null;
  publicSlug?: string | null;
  aiEnabled: boolean;
  aiModel: string;
  aiApiKeyConfigured: boolean;
  aiInstructions?: string | null;
}
