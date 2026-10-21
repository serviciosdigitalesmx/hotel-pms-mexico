package com.hotelpms.monolith.booking;

import com.hotelpms.frontdesk.reservations.dto.ReservationRequest;
import com.hotelpms.frontdesk.reservations.dto.ReservationResponse;
import com.hotelpms.frontdesk.reservations.service.ReservationService;
import com.hotelpms.frontdesk.stays.repository.HotelSettingsRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Public tenant-slug booking route formerly resolved and authenticated by the API Gateway. */
@RestController
@RequestMapping("/api/public/hotels/{slug}/reservations")
public class PublicBookingController {

  private static final Pattern VALID_SLUG = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,119}");
  private static final String PUBLIC_BOOKING_USER = "public-booking-web";

  private final HotelSettingsRepository hotelSettingsRepository;
  private final ReservationService reservationService;

  public PublicBookingController(
      final HotelSettingsRepository hotelSettingsRepository,
      final ReservationService reservationService) {
    this.hotelSettingsRepository = hotelSettingsRepository;
    this.reservationService = reservationService;
  }

  /** Creates a reservation for the tenant resolved from its public slug, never from request data. */
  @PostMapping
  public ResponseEntity<ReservationResponse> createReservation(
      @PathVariable final String slug, @Valid @RequestBody final ReservationRequest request) {
    if (slug == null || !VALID_SLUG.matcher(slug).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_PUBLIC_HOTEL_SLUG");
    }
    final UUID tenantId =
        hotelSettingsRepository
            .findByPublicSlug(slug)
            .map(settings -> settings.getHotelId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    final SecurityContext previousContext = SecurityContextHolder.getContext();
    final SecurityContext bookingContext = SecurityContextHolder.createEmptyContext();
    final UsernamePasswordAuthenticationToken bookingAuthentication =
        new UsernamePasswordAuthenticationToken(
            PUBLIC_BOOKING_USER,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_RECEPTIONIST")));
    bookingAuthentication.setDetails(tenantId.toString());
    bookingContext.setAuthentication(bookingAuthentication);
    SecurityContextHolder.setContext(bookingContext);
    try {
      return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
    } finally {
      SecurityContextHolder.setContext(previousContext);
    }
  }
}
