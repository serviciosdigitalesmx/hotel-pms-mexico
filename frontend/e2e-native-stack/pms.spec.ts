import { readFile, writeFile } from 'node:fs/promises';
import { test, expect, type Page } from '@playwright/test';
import common from '../src/locales/es/common.json' with { type: 'json' };
import guestLabels from '../src/locales/es/guests.json' with { type: 'json' };
import billingLabels from '../src/locales/es/billing.json' with { type: 'json' };
import type { GuestResponseDTO } from '../src/types/guest.types';
import type { RoomResponse, RoomTypeResponse } from '../src/types/inventory.types';
import type { ReservationResponse } from '../src/types/reservation.types';
import type { HotelSettingsRequest } from '../src/types/stay.types';
import type { InvoiceResponse, InvoiceSearchResult, PaymentResponse } from '../src/types/billing.types';
import type { MenuItemResponse, RestaurantOrderResponse } from '../src/types/fb.types';
import type { SpringPage } from '../src/types/page.types';
import { receivedMail } from './mailpit';
import { assertPdf, baseURL, json, loginUI, mailpitURL, type NativeStay, otherCredentials, PmsApi, status, stayDates, uniqueTag, waitForApi } from './support';

async function searchGuestsUI(page: Page, email: string): Promise<SpringPage<GuestResponseDTO>> {
  await page.goto('/guests');
  const response = waitForApi(page, 'GET', '/api/v1/guests/search', email);
  await page.getByRole('searchbox').fill(email);
  return json<SpringPage<GuestResponseDTO>>(await response);
}

test('real PMS journey: guest/reservation UI, check-in, F&B, tenant isolation, invoice PDF/payment, checkout and SMTP',
  async ({ page, browser, playwright }, info) => {
    test.setTimeout(300_000);
    const tag = uniqueTag();
    const email = `${tag}@guest.test`;
    const dates = stayDates();
    const evidence: Record<string, unknown> = { tag, email, dates };
    const api = await loginUI(page);
    const original = await api.settings();
    // GET/PUT settings is a real partial-update contract. Restore exactly the
    // modified flags/subjects afterwards, including if an assertion fails.
    const restore: HotelSettingsRequest = {
      alloggiatiAutoSend: original.alloggiatiAutoSend,
      sendReservationConfirmedEmail: original.sendReservationConfirmedEmail,
      sendCheckoutEmail: original.sendCheckoutEmail,
      emailSubjectReservationConfirmed: original.emailSubjectReservationConfirmed ?? '',
      emailSubjectCheckout: original.emailSubjectCheckout ?? '',
      locale: original.locale ?? '',
    };
    const reservationSubject = `${tag} reservation`;
    const checkoutSubject = `${tag} checkout`;
    const mailbox = await playwright.request.newContext();
    try {
      await status(await api.mutate('PUT', '/api/v1/stays/settings', {
        alloggiatiAutoSend: false, sendReservationConfirmedEmail: true, sendCheckoutEmail: true,
        emailSubjectReservationConfirmed: reservationSubject, emailSubjectCheckout: checkoutSubject,
        locale: 'es-MX',
      } satisfies HotelSettingsRequest), 200);
      // Fail early if Mailpit was not started; notification coverage never skips.
      await status(await mailbox.get(`${mailpitURL}/api/v1/messages`, {
        timeout: 5_000, maxRedirects: 0,
      }), 200);

      const guest = await test.step('create guest through UI and match the persisted search result', async () => {
        await page.goto('/guests');
        await page.getByRole('button', { name: common.add_guest, exact: true }).click();
        const dialog = page.getByRole('dialog');
        await dialog.locator('input[name="firstName"]').fill('Native');
        await dialog.locator('input[name="lastName"]').fill(tag);
        await dialog.locator('input[name="email"]').fill(email);
        const response = waitForApi(page, 'POST', '/api/v1/guests');
        await dialog.getByRole('button', { name: common.save, exact: true }).click();
        const created = await json<GuestResponseDTO>(await response, 201);
        expect(created).toMatchObject({ firstName: 'Native', lastName: tag, email });
        await expect(dialog).toBeHidden();
        const list = await searchGuestsUI(page, email);
        expect(list.content.map(g => g.id)).toEqual([created.id]);
        await expect(page.getByRole('row').filter({ hasText: email })).toContainText(`Native ${tag}`);
        expect(await json(await api.get(`/api/v1/guests/${created.id}`))).toMatchObject({ ...created });
        evidence.guestId = created.id;
        return created;
      });

      const room = await test.step('create a room type and clean room using authenticated real APIs', async () => {
        const roomType = await json<RoomTypeResponse>(await api.mutate('POST', '/api/v1/room-types', {
          name: `${tag} room type`, description: 'Native stack E2E fixture', maxOccupancy: 2, basePrice: 100,
        }), 201);
        const created = await json<RoomResponse>(await api.mutate('POST', '/api/v1/rooms', {
          roomNumber: `N${tag.slice(-12)}`, roomTypeId: roomType.id, status: 'CLEAN',
        }), 201);
        expect(created).toMatchObject({ hotelId: original.hotelId, status: 'CLEAN', roomType: { id: roomType.id } });
        expect((await json<RoomTypeResponse[]>(await api.get('/api/v1/room-types'))).map(t => t.id)).toContain(roomType.id);
        expect(await json(await api.get(`/api/v1/rooms/${created.id}`))).toMatchObject({ id: created.id, status: 'CLEAN' });
        evidence.roomTypeId = roomType.id;
        evidence.roomId = created.id;
        evidence.hotelId = original.hotelId;
        return created;
      });

      const reservation = await test.step('reserve the actual guest and room through UI, then verify the list and SMTP', async () => {
        await page.goto('/reservations/new');
        const guests = waitForApi(page, 'GET', '/api/v1/guests/search', email);
        await page.getByLabel(guestLabels.search_guest_placeholder, { exact: true }).fill(email);
        expect((await json<SpringPage<GuestResponseDTO>>(await guests)).content.map(g => g.id)).toContain(guest.id);
        await page.getByRole('button').filter({ hasText: email }).click();
        await page.locator('input[type="date"]').nth(0).fill(dates.checkInDate);
        const availability = waitForApi(page, 'GET', '/api/v1/rooms/availability');
        await page.locator('input[type="date"]').nth(1).fill(dates.checkOutDate);
        expect((await json<RoomResponse[]>(await availability)).map(r => r.id)).toContain(room.id);
        await page.getByRole('button').filter({ hasText: room.roomNumber }).click();
        const response = waitForApi(page, 'POST', '/api/v1/reservations');
        await page.locator('form button[type="submit"]').click();
        const created = await json<ReservationResponse>(await response, 201);
        expect(created).toMatchObject({ guestId: guest.id, ...dates, status: 'CONFIRMED', expectedGuests: 1, confirmationEmailFailed: false });
        expect(created.lineItems).toHaveLength(1);
        expect(created.lineItems[0]).toMatchObject({ roomId: room.id, price: 200 });
        await expect(page).toHaveURL(/\/reservations$/);
        const listResponse = waitForApi(page, 'GET', '/api/v1/reservations/search', email);
        await page.getByRole('searchbox').fill(email);
        expect((await json<SpringPage<ReservationResponse>>(await listResponse)).content.map(r => r.id)).toEqual([created.id]);
        await expect(page.getByRole('row').filter({ hasText: tag })).toContainText(room.roomNumber);
        await receivedMail(mailbox, info, email, reservationSubject, [`Native ${tag}`, created.id]);
        evidence.reservationId = created.id;
        return created;
      });

      const stay = await test.step('check in through frontdesk and prove guest/billing integrations plus stay UI', async () => {
        // Actual StayGuestRequest contract from verify-frontdesk-native-runtime.sh.
        const created = await json<NativeStay>(await api.mutate('POST', '/api/v1/stays', {
          reservationId: reservation.id, guestId: guest.id, roomId: room.id, status: 'EXPECTED', occupantCount: 1,
          guests: [{ firstName: guest.firstName, lastName: guest.lastName, gender: 'M', dateOfBirth: '1990-01-01',
            placeOfBirth: 'Monterrey', citizenship: 'MX', documentType: 'PASSPORT', documentNumber: tag,
            documentPlaceOfIssue: 'MX', isPrimaryGuest: true, travellerType: 'OSPITE_SINGOLO', travelPurpose: 'BUSINESS' }],
        }), 201);
        expect(created).toMatchObject({ reservationId: reservation.id, guestId: guest.id, roomId: room.id,
          hotelId: original.hotelId, status: 'CHECKED_IN', invoiceCreationFailed: false, roomNumber: room.roomNumber });
        expect(created.invoiceId).toMatch(/^[0-9a-f-]{36}$/i);
        expect(await json(await api.get(`/api/v1/reservations/${reservation.id}`))).toMatchObject({ status: 'CHECKED_IN', actualGuests: 1 });
        expect(await json(await api.get(`/api/v1/rooms/${room.id}`))).toMatchObject({ status: 'OCCUPIED' });
        const list = waitForApi(page, 'GET', '/api/v1/stays');
        await page.goto('/stays');
        expect((await json<SpringPage<NativeStay>>(await list)).content.map(s => s.id)).toContain(created.id);
        const row = page.getByRole('row').filter({ has: page.locator(`[id="checkout-btn-${created.id}"]`) });
        await expect(row).toContainText(tag);
        await expect(row).toContainText(room.roomNumber);
        await expect(row).toContainText(common.status_checked_in);
        const invoice = await json<InvoiceResponse>(await api.get(`/api/v1/invoices/${created.invoiceId}`));
        expect(invoice).toMatchObject({ stayId: created.id, guestId: guest.id, reservationId: reservation.id, status: 'ISSUED', totalAmount: 200 });
        expect(invoice.charges?.filter(charge => charge.type === 'ROOM_NIGHT')).toHaveLength(1);
        await status(await api.mutate('PUT', `/api/v1/stays/${created.id}/check-out`, {}), 409);
        expect(await json(await api.get(`/api/v1/stays/${created.id}`))).toMatchObject({ status: 'CHECKED_IN' });
        expect(await json(await api.get(`/api/v1/rooms/${room.id}`))).toMatchObject({ status: 'OCCUPIED' });
        evidence.stayId = created.id;
        evidence.invoiceId = created.invoiceId;
        return created;
      });

      const { order, invoice } = await test.step('confirm F&B in UI and require the actual charge in the original folio', async () => {
        const menu = await json<MenuItemResponse>(await api.mutate('POST', '/api/v1/fb/menu-items', {
          name: `${tag} breakfast`, price: 12.5, category: 'Breakfast', description: 'E2E fixture', available: true,
        }), 201);
        const order = await json<RestaurantOrderResponse>(await api.mutate('POST', '/api/v1/fb/orders', {
          stayId: stay.id, items: [{ menuItemId: menu.id, quantity: 2 }],
        }), 201);
        expect(order).toMatchObject({ stayId: stay.id, totalAmount: 25, status: 'PENDING', roomNumber: room.roomNumber });
        expect(order.items).toEqual([expect.objectContaining({ itemName: menu.name, quantity: 2, unitPrice: 12.5 })]);
        const list = waitForApi(page, 'GET', '/api/v1/fb/orders');
        await page.goto('/restaurant');
        expect((await json<SpringPage<RestaurantOrderResponse>>(await list)).content.map(o => o.id)).toContain(order.id);
        const button = page.getByRole('button', { name: `${common.confirm_order} ${order.id}`, exact: true });
        await expect(page.getByRole('row').filter({ has: button })).toContainText(room.roomNumber);
        const confirm = waitForApi(page, 'POST', `/api/v1/fb/orders/${order.id}/confirm`);
        await button.click();
        expect(await json(await confirm)).toMatchObject({ id: order.id, status: 'BILLED_TO_ROOM' });
        expect(await json(await api.get(`/api/v1/fb/orders/${order.id}`))).toMatchObject({ status: 'BILLED_TO_ROOM' });
        const invoice = await json<InvoiceResponse>(await api.get(`/api/v1/invoices/${stay.invoiceId}`));
        const charges = invoice.charges?.filter(charge => charge.type === 'FB_ORDER' && charge.referenceId === order.id);
        expect(charges, 'A confirmed order alone is insufficient: billing fallback can swallow a failed charge').toHaveLength(1);
        expect(charges![0]).toMatchObject({ amount: 25, description: `F&B: 2x ${menu.name}` });
        expect(invoice.totalAmount).toBe(225);
        evidence.menuItemId = menu.id;
        evidence.orderId = order.id;
        evidence.invoiceTotal = invoice.totalAmount;
        evidence.pdfExpectedText = ['FACTURA', `Native ${tag}`, room.roomNumber, `${tag} breakfast`, '225.00'];
        return { order, invoice };
      });

      await test.step('second hotel cannot list, read or mutate the created fixtures, even with spoofed tenant headers', async () => {
        const otherContext = await browser.newContext({ baseURL });
        try {
          const other = new PmsApi(otherContext.request);
          expect((await other.login(otherCredentials())).role).toBe('ADMIN');
          const settings = await other.settings();
          expect(settings.hotelId).not.toBe(original.hotelId);
          evidence.otherHotelId = settings.hotelId;
          const otherEmail = `${tag}@other.test`;
          const otherGuest = await json<GuestResponseDTO>(await other.mutate('POST', '/api/v1/guests', {
            firstName: 'Other', lastName: tag, email: otherEmail,
          }), 201);
          await status(await other.get(`/api/v1/guests/${otherGuest.id}`), 200);
          await status(await api.get(`/api/v1/guests/${otherGuest.id}`), 404);
          evidence.otherGuestId = otherGuest.id;
          const spoof = { 'X-Auth-Hotel': original.hotelId, 'X-Auth-Role': 'ADMIN', 'X-Auth-User': 'admin' };
          // Controls on exactly the same resources separate isolation from a
          // missing fixture, absent route or unrelated downstream outage.
          for (const path of [`/api/v1/guests/${guest.id}`, `/api/v1/rooms/${room.id}`,
            `/api/v1/reservations/${reservation.id}`, `/api/v1/stays/${stay.id}`,
            `/api/v1/invoices/${invoice.id}`, `/api/v1/invoices/${invoice.id}/pdf`, `/api/v1/fb/orders/${order.id}`]) {
            await status(await api.get(path), 200);
            await status(await other.get(path), 404);
            await status(await other.get(path, spoof), 404);
          }
          for (const path of ['/api/v1/rooms?size=500', '/api/v1/reservations?size=500', '/api/v1/stays?size=500',
            '/api/v1/invoices?size=500', '/api/v1/fb/orders?size=500']) {
            const result = await json<SpringPage<{ id: string }>>(await other.get(path, spoof));
            const ids = result.content.map(item => item.id);
            for (const id of [room.id, reservation.id, stay.id, invoice.id, order.id]) expect(ids).not.toContain(id);
          }
          expect((await json<MenuItemResponse[]>(await other.get('/api/v1/fb/menu-items', spoof)))
            .some(item => item.id === evidence.menuItemId)).toBe(false);
          const otherMenu = await json<MenuItemResponse>(await other.mutate('POST', '/api/v1/fb/menu-items', {
            name: `${tag} other breakfast`, price: 12.5, category: 'Breakfast', available: true,
          }), 201);
          // Own menu + foreign stay tests frontdesk's tenant boundary through
          // F&B's real Feign client instead of failing on a foreign menu first.
          await status(await other.mutate('POST', '/api/v1/fb/orders', {
            stayId: stay.id, items: [{ menuItemId: otherMenu.id, quantity: 1 }],
          }, spoof), 404);
          expect(await json(await other.get(`/api/v1/fb/orders/stay/${stay.id}`))).toEqual([]);
          await status(await other.mutate('PUT', `/api/v1/guests/${guest.id}`, {
            firstName: 'Forbidden', lastName: tag, email,
          }, spoof), 404);
          await status(await other.mutate('PUT', `/api/v1/stays/${stay.id}/check-out`, {}, spoof), 404);
          await status(await other.mutate('POST', `/api/v1/invoices/${invoice.id}/payments`, {
            amount: invoice.totalAmount, paymentMethod: 'CASH',
          }, spoof), 404);
          await status(await other.mutate('POST', `/api/v1/fb/orders/${order.id}/confirm`, undefined, spoof), 404);
          const otherPage = await otherContext.newPage();
          expect((await searchGuestsUI(otherPage, otherEmail)).content.map(g => g.id)).toEqual([otherGuest.id]);
          await expect(otherPage.getByRole('row').filter({ hasText: otherEmail })).toContainText('Other');
          const visibleGuests = await searchGuestsUI(otherPage, email);
          expect(visibleGuests.content).toEqual([]);
          await expect(otherPage.getByRole('row').filter({ hasText: email })).toHaveCount(0);
          expect(await json(await api.get(`/api/v1/guests/${guest.id}`))).toMatchObject({ firstName: guest.firstName, email });
          expect(await json(await api.get(`/api/v1/stays/${stay.id}`))).toMatchObject({ status: 'CHECKED_IN' });
          expect(await json(await api.get(`/api/v1/invoices/${invoice.id}`))).toMatchObject({ status: 'ISSUED', payments: [], totalAmount: 225 });
        } finally {
          await otherContext.close();
        }
      });

      await test.step('download invoice PDF and pay through UI; verify persisted payment and paid list', async () => {
        await page.goto('/billing');
        const search = waitForApi(page, 'GET', '/api/v1/invoices/search', email);
        await page.getByRole('searchbox').fill(email);
        const found = await json<SpringPage<InvoiceSearchResult>>(await search);
        expect(found.content.map(item => item.invoice.id)).toEqual([invoice.id]);
        const row = page.getByRole('row').filter({ hasText: tag });
        await expect(row).toContainText(invoice.invoiceNumber);
        await row.getByRole('button', { name: common.view, exact: true }).click();
        const detail = page.getByRole('dialog');
        await expect(detail).toContainText(`${tag} breakfast`);
        const downloadPromise = page.waitForEvent('download');
        await detail.getByRole('button', { name: billingLabels.download_pdf, exact: true }).click();
        const download = await downloadPromise;
        expect(new URL(download.url()).pathname).toBe(`/api/v1/invoices/${invoice.id}/pdf`);
        expect(download.suggestedFilename()).toMatch(/\.pdf$/i);
        expect(await download.failure()).toBeNull();
        const pdfPath = info.outputPath('invoice.pdf');
        await download.saveAs(pdfPath);
        const bytes = await readFile(pdfPath);
        await info.attach('browser-invoice.pdf', { path: pdfPath, contentType: 'application/pdf' });
        assertPdf(bytes);
        await page.keyboard.press('Escape');
        await expect(detail).toBeHidden();
        await row.getByRole('button', { name: common.register_payment, exact: true }).click();
        const paymentDialog = page.getByRole('dialog');
        await paymentDialog.getByRole('spinbutton').fill(String(invoice.totalAmount));
        await paymentDialog.locator('#payment-method-select').selectOption('CASH');
        await paymentDialog.getByLabel(billingLabels.transaction_reference, { exact: true }).fill(tag);
        const payment = waitForApi(page, 'POST', `/api/v1/invoices/${invoice.id}/payments`);
        await paymentDialog.getByRole('button', { name: billingLabels.confirm_payment, exact: true }).click();
        const paid = await json<PaymentResponse>(await payment, 201);
        expect(paid).toMatchObject({ invoiceId: invoice.id, amount: invoice.totalAmount, paymentMethod: 'CASH', transactionReference: tag });
        await expect(paymentDialog).toBeHidden();
        const persisted = await json<InvoiceResponse>(await api.get(`/api/v1/invoices/${invoice.id}`));
        expect(persisted.status).toBe('PAID');
        expect(persisted.payments.map(p => p.id)).toEqual([paid.id]);
        // The current PaymentModal updates status optimistically; reload to
        // require the actual persisted status to survive a fresh network read.
        const reloaded = waitForApi(page, 'GET', '/api/v1/invoices/search');
        await page.reload();
        expect((await json<SpringPage<InvoiceSearchResult>>(await reloaded)).content
          .find(item => item.invoice.id === invoice.id)?.invoice.status).toBe('PAID');
        await expect(page.getByRole('row').filter({ hasText: tag })).toContainText(common.invoice_status_PAID);
        evidence.paymentId = paid.id;
      });

      await test.step('checkout in UI, persist DIRTY room and deliver frontdesk email with the real billing PDF', async () => {
        const list = waitForApi(page, 'GET', '/api/v1/stays');
        await page.goto('/stays');
        expect((await json<SpringPage<NativeStay>>(await list)).content.map(s => s.id)).toContain(stay.id);
        const checkout = waitForApi(page, 'PUT', `/api/v1/stays/${stay.id}/check-out`);
        await page.locator(`[id="checkout-btn-${stay.id}"]`).click();
        expect(await json(await checkout)).toMatchObject({ id: stay.id, status: 'CHECKED_OUT', checkoutEmailFailed: false });
        const row = page.getByRole('row').filter({ hasText: room.roomNumber });
        await expect(row).toContainText(common.status_checked_out);
        await expect(row.locator(`[id="checkout-btn-${stay.id}"]`)).toHaveCount(0);
        expect(await json(await api.get(`/api/v1/stays/${stay.id}`))).toMatchObject({ status: 'CHECKED_OUT', checkoutEmailFailed: false });
        expect(await json(await api.get(`/api/v1/rooms/${room.id}`))).toMatchObject({ status: 'DIRTY' });
        await receivedMail(mailbox, info, email, checkoutSubject, [`Native ${tag}`, room.roomNumber, `${tag} breakfast`], invoice.id);
        evidence.finalStayStatus = 'CHECKED_OUT';
        evidence.reservationAndCheckoutMail = 'RECEIVED';
      });
    } finally {
      const evidencePath = info.outputPath('fixture-identifiers.json');
      await writeFile(evidencePath, JSON.stringify(evidence, null, 2));
      await info.attach('fixture-identifiers.json', { path: evidencePath, contentType: 'application/json' });
      try {
        await status(await api.mutate('PUT', '/api/v1/stays/settings', restore), 200);
      } finally {
        await mailbox.dispose();
      }
    }
  });
