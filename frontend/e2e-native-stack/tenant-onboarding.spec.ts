import { test, expect } from '@playwright/test';
import type { GuestResponseDTO } from '../src/types/guest.types';
import type { RoomResponse, RoomTypeResponse } from '../src/types/inventory.types';
import type { SpringPage } from '../src/types/page.types';
import { baseURL, json, otherCredentials, PmsApi, status, uniqueTag } from './support';

type Hotel = { id: string; name: string; slug: string };
type Branch = { id: string; hotelId: string; name: string; active: boolean };

test('platform provisions two hotels and keeps owners, guests and settings isolated', async ({ browser }) => {
  test.setTimeout(180_000);
  const tag = uniqueTag();
  const platformContext = await browser.newContext({ baseURL });
  const firstContext = await browser.newContext({ baseURL });
  const secondContext = await browser.newContext({ baseURL });
  try {
    const platform = new PmsApi(await platformContext.newPage());
    const platformUser = await platform.login(otherCredentials());
    expect(platformUser).toMatchObject({ role: 'ADMIN', hotelId: '00000000-0000-0000-0000-000000000001' });

    const firstCredentials = { username: `${tag}-owner-a`, password: 'TempOwner1A', newPassword: 'NewOwner2A' };
    const secondCredentials = { username: `${tag}-owner-b`, password: 'TempOwner1B', newPassword: 'NewOwner2B' };
    const firstHotel = await json<Hotel>(await platform.mutate('POST', '/api/v1/auth/platform/hotels', {
      name: `${tag} Hotel A`, slug: `${tag}-a`, ownerUsername: firstCredentials.username,
      ownerEmail: `${tag}-a@tenant.test`, initialPassword: firstCredentials.password,
    }), 201);
    const secondHotel = await json<Hotel>(await platform.mutate('POST', '/api/v1/auth/platform/hotels', {
      name: `${tag} Hotel B`, slug: `${tag}-b`, ownerUsername: secondCredentials.username,
      ownerEmail: `${tag}-b@tenant.test`, initialPassword: secondCredentials.password,
    }), 201);
    expect(firstHotel.id).not.toBe(secondHotel.id);
    expect(firstHotel.id).not.toBe(platformUser.hotelId);
    const registered = await json<Hotel[]>(await platform.get('/api/v1/auth/platform/hotels'));
    expect(registered.map(hotel => hotel.id)).toEqual(expect.arrayContaining([firstHotel.id, secondHotel.id]));

    const first = new PmsApi(await firstContext.newPage());
    const second = new PmsApi(await secondContext.newPage());
    expect(await first.login(firstCredentials)).toMatchObject({ role: 'OWNER', hotelId: firstHotel.id });
    expect(await second.login(secondCredentials)).toMatchObject({ role: 'OWNER', hotelId: secondHotel.id });
    await status(await first.get('/api/v1/auth/platform/hotels'), 403);
    await status(await second.get('/api/v1/auth/platform/hotels', {
      'X-Auth-Role': 'ADMIN', 'X-Auth-Hotel': platformUser.hotelId!,
    }), 403);

    const firstSettings = await first.settings();
    const secondSettings = await second.settings();
    expect(firstSettings.hotelId).toBe(firstHotel.id);
    expect(secondSettings.hotelId).toBe(secondHotel.id);

    const firstIndustry = await json<{ tenantId: string; industryKey: string }>(
      await first.mutate('PUT', '/api/v1/auth/industry-profile', {
        industryKey: 'repair', enabledModulesJson: '["orders","inventory"]',
        dynamicFieldsJson: '{}', formsLabelsJson: '{}', catalogsTemplatesJson: '{}',
      }), 200);
    expect(firstIndustry).toMatchObject({ tenantId: firstHotel.id, industryKey: 'repair' });
    const secondIndustry = await json<{ tenantId: string; industryKey: string }>(
      await second.get('/api/v1/auth/industry-profile'));
    expect(secondIndustry.tenantId).toBe(secondHotel.id);
    expect(secondIndustry.tenantId).not.toBe(firstIndustry.tenantId);
    const spoofedIndustry = await json<{ tenantId: string }>(
      await second.get('/api/v1/auth/industry-profile', { 'X-Auth-Hotel': firstHotel.id }));
    expect(spoofedIndustry.tenantId).toBe(secondHotel.id);

    const firstBranch = await json<Branch>(await first.mutate('POST', '/api/v1/auth/branches', {
      name: `${tag} Branch A`,
    }), 201);
    expect(firstBranch.hotelId).toBe(firstHotel.id);
    expect((await json<Branch[]>(await first.get('/api/v1/auth/branches')))
      .map(branch => branch.id)).toContain(firstBranch.id);
    expect((await json<Branch[]>(await second.get('/api/v1/auth/branches')))
      .map(branch => branch.id)).not.toContain(firstBranch.id);
    await status(await second.get(`/api/v1/auth/branches/${firstBranch.id}`), 404);
    await status(await second.get(`/api/v1/auth/branches/${firstBranch.id}`, {
      'X-Auth-Hotel': firstHotel.id,
    }), 404);

    const roomType = await json<RoomTypeResponse>(await first.mutate('POST', '/api/v1/room-types', {
      name: `${tag} suite`, description: 'Tenant isolation fixture', maxOccupancy: 2, basePrice: 100,
    }), 201);
    const room = await json<RoomResponse>(await first.mutate('POST', '/api/v1/rooms', {
      roomNumber: `T${tag.slice(-12)}`, roomTypeId: roomType.id, status: 'CLEAN',
    }), 201);
    expect(room.hotelId).toBe(firstHotel.id);
    await status(await first.get(`/api/v1/rooms/${room.id}`), 200);
    await status(await second.get(`/api/v1/rooms/${room.id}`), 404);
    await status(await second.get(`/api/v1/rooms/${room.id}`, { 'X-Auth-Hotel': firstHotel.id }), 404);

    const firstGuest = await json<GuestResponseDTO>(await first.mutate('POST', '/api/v1/guests', {
      firstName: 'First', lastName: tag, email: `${tag}-first@guest.test`,
    }), 201);
    const secondGuest = await json<GuestResponseDTO>(await second.mutate('POST', '/api/v1/guests', {
      firstName: 'Second', lastName: tag, email: `${tag}-second@guest.test`,
    }), 201);
    await status(await first.get(`/api/v1/guests/${firstGuest.id}`), 200);
    await status(await second.get(`/api/v1/guests/${secondGuest.id}`), 200);
    await status(await first.get(`/api/v1/guests/${secondGuest.id}`), 404);
    await status(await second.get(`/api/v1/guests/${firstGuest.id}`), 404);
    await status(await second.get(`/api/v1/guests/${firstGuest.id}`, {
      'X-Auth-Hotel': firstHotel.id, 'X-Auth-Role': 'ADMIN',
    }), 404);
    const firstSearch = await json<SpringPage<GuestResponseDTO>>(await first.get(
      `/api/v1/guests/search?query=${tag}`));
    const secondSearch = await json<SpringPage<GuestResponseDTO>>(await second.get(
      `/api/v1/guests/search?query=${tag}`));
    expect(firstSearch.content.map(guest => guest.id)).toContain(firstGuest.id);
    expect(firstSearch.content.map(guest => guest.id)).not.toContain(secondGuest.id);
    expect(secondSearch.content.map(guest => guest.id)).toContain(secondGuest.id);
    expect(secondSearch.content.map(guest => guest.id)).not.toContain(firstGuest.id);
    const secondRooms = await json<SpringPage<RoomResponse>>(await second.get('/api/v1/rooms?page=0&size=200'));
    expect(secondRooms.content.map(candidate => candidate.id)).not.toContain(room.id);

    const publicSlug = `${tag}-booking`;
    await status(await first.mutate('PUT', '/api/v1/stays/settings', { publicSlug }), 200);
    const publicContext = await browser.newContext({ baseURL });
    try {
      const publicResponse = await publicContext.request.post(
        `${baseURL}/api/public/hotels/${publicSlug}/reservations`, {
          headers: { 'Content-Type': 'application/json' },
          data: JSON.stringify({
            guestId: firstGuest.id,
            expectedGuests: 1,
            checkInDate: '2099-01-10',
            checkOutDate: '2099-01-12',
            status: 'PENDING',
            lineItems: [{ roomId: room.id }],
          }),
        });
      const publicBody = await publicResponse.text();
      expect(publicResponse.status(), publicBody).toBe(201);
      expect((JSON.parse(publicBody) as { guestId: string; status: string })).toMatchObject({
        guestId: firstGuest.id,
        status: 'PENDING',
      });
    } finally {
      await publicContext.close();
    }

  } finally {
    await platformContext.close();
    await firstContext.close();
    await secondContext.close();
  }
});
