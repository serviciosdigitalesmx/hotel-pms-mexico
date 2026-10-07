package com.hotelpms.gateway.config;

import com.hotelpms.gateway.filter.PublicBookingGatewayFilterFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** Explicit gateway filter registration for JVM and Native runtimes. */
@Configuration
public class PublicBookingGatewayConfig {
  @Bean
  PublicBookingGatewayFilterFactory publicBookingGatewayFilterFactory(
      @Value("${internal.hmac.secret}") String hmacSecret,
      @Value("${public-booking.resolver-url:http://frontdesk-service:8081/internal/public-hotels}")
          String resolverUrl,
      @Value("${public-booking.hotel-id:}") String hotelId,
      WebClient.Builder webClientBuilder) {
    return new PublicBookingGatewayFilterFactory(hmacSecret, resolverUrl, hotelId, webClientBuilder);
  }
}
