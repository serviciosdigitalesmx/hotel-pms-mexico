package com.hotelpms.frontdesk.config;

import com.hotelpms.frontdesk.notifications.NotificationServiceAuthContext;
import com.hotelpms.internalauth.feign.InternalFeignAuthInterceptor;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign configuration that signs request-context calls and scheduled outbox calls with the internal
 * HMAC signature required by downstream {@code InternalAuthFilter} instances (T-GW-07 / T-GST-05).
 * See {@link InternalFeignAuthInterceptor} for the shared signing logic.
 */
@Configuration
public class FeignHeaderConfig {

  private final String hmacSecret;
  private final NotificationServiceAuthContext notificationServiceAuthContext;

  /**
   * Constructs the Feign configuration with the shared HMAC secret.
   *
   * @param hmacSecret the internal HMAC secret, shared with all microservices
   * @param notificationServiceAuthContext scoped identity for scheduled notification delivery
   */
  public FeignHeaderConfig(
      @Value("${internal.hmac.secret}") final String hmacSecret,
      final NotificationServiceAuthContext notificationServiceAuthContext) {
    this.hmacSecret = hmacSecret;
    this.notificationServiceAuthContext = notificationServiceAuthContext;
  }

  /**
   * Registers the shared {@link InternalFeignAuthInterceptor}, including the scoped system identity
   * used by the scheduled operational-notification outbox.
   *
   * @return the configured interceptor
   */
  @Bean
  public RequestInterceptor authHeaderInterceptor() {
    return new InternalFeignAuthInterceptor(hmacSecret, notificationServiceAuthContext::resolve);
  }
}
