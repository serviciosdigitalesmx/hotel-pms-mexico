package com.hotelpms.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

/** Conventional Gateway filter-factory bean for the public booking route. */
public final class PublicBookingGatewayFilterFactory extends PublicBookingFilter {
  /** Creates the filter from server-side provider settings. */
  @Autowired
  public PublicBookingGatewayFilterFactory(
      @Value("${internal.hmac.secret}") final String hmacSecret,
      @Value("${public-booking.resolver-url:http://frontdesk-service:8081/internal/public-hotels}")
          final String resolverUrl,
      @Value("${public-booking.hotel-id:}") final String resolverIdentityHotelId,
      final WebClient.Builder webClientBuilder) {
    super(hmacSecret, resolverUrl, resolverIdentityHotelId, webClientBuilder.build());
  }
}
