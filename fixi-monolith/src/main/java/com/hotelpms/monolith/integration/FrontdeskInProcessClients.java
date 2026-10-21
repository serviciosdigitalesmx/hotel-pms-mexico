package com.hotelpms.monolith.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.billing.service.InvoiceService;
import com.hotelpms.billing.service.PdfInvoiceService;
import com.hotelpms.frontdesk.client.BillingClient;
import com.hotelpms.frontdesk.client.GuestClient;
import com.hotelpms.frontdesk.client.dto.ChargeRequest;
import com.hotelpms.frontdesk.client.dto.ChargeResponse;
import com.hotelpms.frontdesk.client.dto.CustomerDeviceResponse;
import com.hotelpms.frontdesk.client.dto.GuestCreateRequest;
import com.hotelpms.frontdesk.client.dto.GuestResponse;
import com.hotelpms.frontdesk.client.dto.GuestSearchPageResponse;
import com.hotelpms.frontdesk.client.dto.InvoiceCreatedResponse;
import com.hotelpms.frontdesk.client.dto.InvoiceForEmailResponse;
import com.hotelpms.frontdesk.client.dto.InvoiceStatusResponse;
import com.hotelpms.frontdesk.client.dto.StayInvoiceRequest;
import com.hotelpms.guest.service.DeviceService;
import com.hotelpms.guest.service.GuestService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;

/** Direct in-process adapters replacing the Repair/Guest/Billing Feign boundaries. */
@Configuration(proxyBeanMethods = false)
public class FrontdeskInProcessClients {

  @Bean
  GuestClient monolithFrontdeskGuestClient(
      @Lazy final GuestService guests,
      final DeviceService devices,
      final ObjectMapper objectMapper) {
    return new GuestClient() {
      @Override
      public GuestResponse getGuestById(final UUID id) {
        return convert(guests.getGuestById(id), GuestResponse.class, objectMapper);
      }

      @Override
      public CustomerDeviceResponse getCustomerDevice(final UUID customerId, final UUID deviceId) {
        return convert(devices.get(customerId, deviceId), CustomerDeviceResponse.class, objectMapper);
      }

      @Override
      public GuestResponse createGuest(final GuestCreateRequest request) {
        final var guestRequest =
            convert(request, com.hotelpms.guest.dto.request.GuestRequest.class, objectMapper);
        return convert(guests.createGuest(guestRequest), GuestResponse.class, objectMapper);
      }

      @Override
      public List<GuestResponse> getGuestsBatch(final List<UUID> ids) {
        return guests.getGuestsByIds(ids).stream()
            .map(guest -> convert(guest, GuestResponse.class, objectMapper))
            .toList();
      }

      @Override
      public GuestSearchPageResponse searchGuests(final String query, final int size) {
        return search(query, size);
      }

      @Override
      public GuestSearchPageResponse searchGuestsStrict(final String query, final int size) {
        return search(query, size);
      }

      private GuestSearchPageResponse search(final String query, final int size) {
        final var page = guests.searchGuests(query, PageRequest.of(0, Math.max(1, size)));
        return new GuestSearchPageResponse(
            page.getContent().stream()
                .map(guest -> convert(guest, GuestResponse.class, objectMapper))
                .toList());
      }
    };
  }

  @Bean
  BillingClient monolithFrontdeskBillingClient(
      @Lazy final InvoiceService invoices,
      @Lazy final PdfInvoiceService pdfInvoices,
      final ObjectMapper objectMapper) {
    return new BillingClient() {
      @Override
      public InvoiceStatusResponse getLatestInvoiceByReservation(final UUID reservationId) {
        return convert(
            invoices.getLatestInvoiceByReservation(reservationId),
            InvoiceStatusResponse.class,
            objectMapper);
      }

      @Override
      public InvoiceStatusResponse getInvoiceById(final UUID invoiceId) {
        return convert(invoices.getInvoice(invoiceId), InvoiceStatusResponse.class, objectMapper);
      }

      @Override
      public InvoiceForEmailResponse getInvoiceForEmail(final UUID invoiceId) {
        return convert(invoices.getInvoice(invoiceId), InvoiceForEmailResponse.class, objectMapper);
      }

      @Override
      public byte[] getInvoicePdf(final UUID invoiceId) {
        return pdfInvoices.generateInvoicePdf(invoiceId);
      }

      @Override
      public InvoiceCreatedResponse createInvoiceForStay(final StayInvoiceRequest request) {
        final var billingRequest =
            convert(request, com.hotelpms.billing.dto.StayInvoiceRequest.class, objectMapper);
        return convert(
            invoices.createInvoiceForStay(billingRequest),
            InvoiceCreatedResponse.class,
            objectMapper);
      }

      @Override
      public ChargeResponse addCharge(final UUID stayId, final ChargeRequest request) {
        final var billingRequest =
            convert(request, com.hotelpms.billing.dto.ChargeRequest.class, objectMapper);
        return convert(invoices.addCharge(stayId, billingRequest), ChargeResponse.class, objectMapper);
      }
    };
  }

  private static <T> T convert(
      final Object source, final Class<T> targetType, final ObjectMapper objectMapper) {
    return objectMapper.convertValue(source, targetType);
  }
}
