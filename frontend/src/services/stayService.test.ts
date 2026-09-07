import { describe, it, expect, vi, beforeEach } from 'vitest';
import api from './api';
import { stayService } from './stayService';

vi.mock('./api');

describe('stayService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should fetch all stays paginated', async () => {
    const mockStays = [{ id: '1', status: 'CHECKED_IN' }];
    const mockPage = { content: mockStays, totalElements: 1, totalPages: 1, number: 0, size: 20,
      numberOfElements: 1, first: true, last: true, empty: false };
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockPage });

    const result = await stayService.getAllStays(0, 20);

    expect(api.get).toHaveBeenCalledWith('/api/v1/stays?page=0&size=20&sort=actualCheckInTime,desc');
    expect(result).toEqual(mockPage);
  });

  it('should fetch stay by id', async () => {
    const mockStay = { id: '1', status: 'CHECKED_IN' };
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockStay });

    const result = await stayService.getStayById('1');

    expect(api.get).toHaveBeenCalledWith('/api/v1/stays/1');
    expect(result).toEqual(mockStay);
  });

  it('should create a stay (check-in)', async () => {
    const request = { guestId: 'g1', reservationId: 'r1', roomId: 'rm1' };
    const mockResponse = { id: '1', ...request, status: 'CHECKED_IN' };
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockResponse });

    const result = await stayService.createStay(request as never);

    expect(api.post).toHaveBeenCalledWith('/api/v1/stays', request);
    expect(result).toEqual(mockResponse);
  });

  it('should check out', async () => {
    const mockResponse = { id: '1', status: 'CHECKED_OUT' };
    vi.mocked(api.put).mockResolvedValueOnce({ data: mockResponse });

    const result = await stayService.checkOut('1');

    expect(api.put).toHaveBeenCalledWith('/api/v1/stays/1/check-out', {});
    expect(result).toEqual(mockResponse);
  });

  it('should retry invoice creation', async () => {
    const mockResponse = { id: '1', invoiceCreationFailed: false };
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockResponse });

    const result = await stayService.retryInvoiceCreation('1');

    expect(api.post).toHaveBeenCalledWith('/api/v1/stays/1/invoice/retry', {});
    expect(result).toEqual(mockResponse);
  });

  it('should retry checkout email', async () => {
    const mockResponse = { id: '1', checkoutEmailFailed: false };
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockResponse });

    const result = await stayService.retryCheckoutEmail('1');

    expect(api.post).toHaveBeenCalledWith('/api/v1/stays/1/checkout-email/retry', {});
    expect(result).toEqual(mockResponse);
  });
});

describe('stayService — settings, lookups, downloads', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should fetch hotel settings', async () => {
    const mockSettings = { hotelId: 'h1', alloggiatiAutoSend: true, hotelName: 'Hotel Test' };
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockSettings });

    const result = await stayService.getHotelSettings();

    expect(api.get).toHaveBeenCalledWith('/api/v1/stays/settings');
    expect(result).toEqual(mockSettings);
  });

  it('should update hotel settings', async () => {
    const request = { alloggiatiAutoSend: false, hotelName: 'Hotel Updated' };
    const mockResponse = { hotelId: 'h1', ...request };
    vi.mocked(api.put).mockResolvedValueOnce({ data: mockResponse });

    const result = await stayService.updateHotelSettings(request);

    expect(api.put).toHaveBeenCalledWith('/api/v1/stays/settings', request);
    expect(result).toEqual(mockResponse);
  });

  it('should fetch the last completed stay for a guest', async () => {
    const mockStay = { id: '1', status: 'CHECKED_OUT' };
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockStay, status: 200 });

    const result = await stayService.getLastCompletedStayForGuest('g1');

    expect(api.get).toHaveBeenCalledWith('/api/v1/stays/guest/g1/latest', {
      validateStatus: expect.any(Function),
    });
    expect(result).toEqual(mockStay);
  });

  it('should return null when no completed stay exists for a guest (204)', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: null, status: 204 });

    const result = await stayService.getLastCompletedStayForGuest('g1');

    expect(result).toBeNull();
  });

  it('accepts 200 and 204 as valid statuses but rejects others in validateStatus', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: null, status: 200 });

    await stayService.getLastCompletedStayForGuest('g1');

    const { validateStatus } = vi.mocked(api.get).mock.calls[0][1] as {
      validateStatus: (status: number) => boolean;
    };
    expect(validateStatus(200)).toBe(true);
    expect(validateStatus(204)).toBe(true);
    expect(validateStatus(404)).toBe(false);
  });

  it('should fetch available (CLEAN) rooms only', async () => {
    const mockRooms = [
      { id: 'r1', roomNumber: '101', status: 'CLEAN' },
      { id: 'r2', roomNumber: '102', status: 'DIRTY' },
    ];
    vi.mocked(api.get).mockResolvedValueOnce({ data: { content: mockRooms } });

    const result = await stayService.getAvailableRooms();

    expect(api.get).toHaveBeenCalledWith('/api/v1/rooms', { params: { size: 200 } });
    expect(result).toEqual([mockRooms[0]]);
  });

  it('should return an empty array when rooms response has no content', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: {} });

    const result = await stayService.getAvailableRooms();

    expect(result).toEqual([]);
  });

});
