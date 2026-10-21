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
import com.hotelpms.guest.config.BatchJobContext;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;

/** In-process adapters for the guest, stay, billing, and hotel-profile bounded contexts. */
@Configuration(proxyBeanMethods = false)
public class GuestAndBillingInProcessClients {

  @Bean
  MonolithReservationClient monolithGuestReservationClient(final ReservationService reservations) {
    return new MonolithReservationClient(reservations);
  }

  @Bean
  MonolithStayServiceClient monolithGuestStayClient(
      final StayService stays, final ObjectMapper mapper) {
    return new MonolithStayServiceClient(stays, mapper);
  }

  /** Concrete adapter types let Spring AOT pre-generate resilience proxies for Native Image. */
  public static class MonolithReservationClient implements ReservationClient {
    private final ReservationService reservations;

    public MonolithReservationClient(final ReservationService reservations) {
      this.reservations = reservations;
    }

    @Override
    public boolean hasActiveReservations(final UUID guestId) {
      return reservations.hasActiveReservations(guestId);
    }
  }

  @Bean
  MonolithGuestBillingServiceClient monolithGuestBillingClient(
      @Lazy final InvoiceService invoices, final ObjectMapper mapper) {
    return new MonolithGuestBillingServiceClient(invoices, mapper);
  }

  public static class MonolithStayServiceClient implements StayServiceClient {
    private final StayService stays;
    private final ObjectMapper mapper;

    public MonolithStayServiceClient(final StayService stays, final ObjectMapper mapper) {
      this.stays = stays;
      this.mapper = mapper;
    }

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
  }

  public static class MonolithGuestBillingServiceClient implements BillingServiceClient {
    private final InvoiceService invoices;
    private final ObjectMapper mapper;

    public MonolithGuestBillingServiceClient(
        final InvoiceService invoices, final ObjectMapper mapper) {
      this.invoices = invoices;
      this.mapper = mapper;
    }

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
  }

  @Bean
  MonolithBillingGuestClient monolithBillingGuestClient(
      final GuestService guests, final ObjectMapper mapper) {
    return new MonolithBillingGuestClient(guests, mapper);
  }

  public static class MonolithBillingGuestClient implements GuestClient {
    private final GuestService guests;
    private final ObjectMapper mapper;

    public MonolithBillingGuestClient(final GuestService guests, final ObjectMapper mapper) {
      this.guests = guests;
      this.mapper = mapper;
    }

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
  }

  @Bean
  MonolithBillingHotelSettingsClient monolithBillingHotelSettingsClient(
      final HotelSettingsService settings, final ObjectMapper mapper) {
    return new MonolithBillingHotelSettingsClient(settings, mapper);
  }

  public static class MonolithBillingHotelSettingsClient implements HotelSettingsClient {
    private final HotelSettingsService settings;
    private final ObjectMapper mapper;

    public MonolithBillingHotelSettingsClient(
        final HotelSettingsService settings, final ObjectMapper mapper) {
      this.settings = settings;
      this.mapper = mapper;
    }

    @Override
    public com.hotelpms.billing.client.dto.HotelSettingsResponse getSettings() {
      return convert(settings.getOrCreate(tenantId()),
          com.hotelpms.billing.client.dto.HotelSettingsResponse.class, mapper);
    }
  }

  private static UUID tenantId() {
    final BatchJobContext batchContext = BatchJobContext.get();
    if (batchContext != null) {
      return UUID.fromString(batchContext.getHotelId());
    }
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
