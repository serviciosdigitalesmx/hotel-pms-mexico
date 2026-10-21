package com.hotelpms.monolith.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.billing.client.GuestClient;
import com.hotelpms.billing.client.HotelSettingsClient;
import com.hotelpms.billing.client.dto.GuestResponse;
import com.hotelpms.billing.client.dto.GuestSearchPageResponse;
import com.hotelpms.billing.service.InvoiceService;
import com.hotelpms.frontdesk.reservations.service.ReservationService;
import com.hotelpms.frontdesk.stays.service.HotelSettingsService;
import com.hotelpms.frontdesk.stays.service.StayService;
import com.hotelpms.guest.client.BillingServiceClient;
import com.hotelpms.guest.client.ReservationClient;
import com.hotelpms.guest.client.StayServiceClient;
import com.hotelpms.guest.client.dto.GuestInvoiceClientResponse;
import com.hotelpms.guest.client.dto.GuestLastStayClientResponse;
import com.hotelpms.guest.client.dto.InvoiceSummaryClientResponse;
import com.hotelpms.guest.client.dto.StaySummaryClientResponse;
import com.hotelpms.guest.service.GuestService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;

/** In-process adapters for the guest, stay, billing, and hotel-profile bounded contexts. */
@Configuration(proxyBeanMethods = false)
public class GuestAndBillingInProcessClients {

  @Bean
  ReservationClient monolithGuestReservationClient(final ReservationService reservations) {
    return reservations::hasActiveReservations;
  }

  @Bean
  StayServiceClient monolithGuestStayClient(
      final StayService stays, final ObjectMapper mapper) {
    return new StayServiceClient() {
      @Override
      public GuestLastStayClientResponse getLastStayDate(final UUID guestId) {
        return convert(stays.getLastStayDateForGuest(guestId, tenantId()),
            GuestLastStayClientResponse.class, mapper);
      }

      @Override
      public List<StaySummaryClientResponse> getStayHistory(final UUID guestId) {
        return stays.getStayHistoryForGuest(guestId, tenantId()).stream()
            .map(item -> convert(item, StaySummaryClientResponse.class, mapper)).toList();
      }
    };
  }

  @Bean
  BillingServiceClient monolithGuestBillingClient(
      final InvoiceService invoices, final ObjectMapper mapper) {
    return new BillingServiceClient() {
      @Override
      public GuestInvoiceClientResponse getLastInvoiceDate(final UUID guestId) {
        return convert(invoices.getLastInvoiceDateForGuest(guestId, tenantId()),
            GuestInvoiceClientResponse.class, mapper);
      }

      @Override
      public List<InvoiceSummaryClientResponse> getInvoiceHistory(final UUID guestId) {
        return invoices.getInvoiceHistoryForGuest(guestId, tenantId()).stream()
            .map(item -> convert(item, InvoiceSummaryClientResponse.class, mapper)).toList();
      }
    };
  }

  @Bean
  GuestClient monolithBillingGuestClient(
      final GuestService guests, final ObjectMapper mapper) {
    return new GuestClient() {
      @Override
      public GuestResponse getGuestById(final UUID id) {
        return convert(guests.getGuestById(id), GuestResponse.class, mapper);
      }

      @Override
      public GuestSearchPageResponse searchGuests(final String query, final int size) {
        return new GuestSearchPageResponse(guests.searchGuests(query,
            PageRequest.of(0, Math.max(1, size))).getContent().stream()
            .map(item -> convert(item, GuestResponse.class, mapper)).toList());
      }

      @Override
      public List<GuestResponse> getGuestsBatch(final List<UUID> ids) {
        return guests.getGuestsByIds(ids).stream()
            .map(item -> convert(item, GuestResponse.class, mapper)).toList();
      }
    };
  }

  @Bean
  HotelSettingsClient monolithBillingHotelSettingsClient(
      final HotelSettingsService settings, final ObjectMapper mapper) {
    return () -> convert(settings.getOrCreate(tenantId()),
        com.hotelpms.billing.client.dto.HotelSettingsResponse.class, mapper);
  }

  private static UUID tenantId() {
    final var authentication =
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication();
    if (authentication == null || !(authentication.getDetails() instanceof String tenant)) {
      throw new org.springframework.security.access.AccessDeniedException("TENANT_CONTEXT_REQUIRED");
    }
    return UUID.fromString(tenant);
  }

  private static <T> T convert(final Object source, final Class<T> target, final ObjectMapper mapper) {
    return mapper.convertValue(source, target);
  }
}
