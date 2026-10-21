package com.hotelpms.monolith.openapi;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Keeps the former Gateway's service-specific OpenAPI links while publishing one process. */
@Configuration(proxyBeanMethods = false)
public class MonolithOpenApiConfiguration implements WebMvcConfigurer {

  @Bean
  GroupedOpenApi authOpenApi() {
    return group("auth-service", "com.hotelpms.auth.controller");
  }

  @Bean
  GroupedOpenApi frontdeskOpenApi() {
    return group(
        "frontdesk-service", "com.hotelpms.frontdesk", "com.hotelpms.monolith.booking");
  }

  @Bean
  GroupedOpenApi guestOpenApi() {
    return group("guest-service", "com.hotelpms.guest.controller");
  }

  @Bean
  GroupedOpenApi billingOpenApi() {
    return group("billing-service", "com.hotelpms.billing.controller");
  }

  @Bean
  GroupedOpenApi foodAndBeverageOpenApi() {
    return group("fb-service", "com.hotelpms.fb.controller");
  }

  @Override
  public void addViewControllers(final ViewControllerRegistry registry) {
    registry.addViewController("/auth-service/v3/api-docs")
        .setViewName("forward:/v3/api-docs/auth-service");
    registry.addViewController("/frontdesk-service/v3/api-docs")
        .setViewName("forward:/v3/api-docs/frontdesk-service");
    registry.addViewController("/guest-service/v3/api-docs")
        .setViewName("forward:/v3/api-docs/guest-service");
    registry.addViewController("/billing-service/v3/api-docs")
        .setViewName("forward:/v3/api-docs/billing-service");
    registry.addViewController("/fb-service/v3/api-docs")
        .setViewName("forward:/v3/api-docs/fb-service");
  }

  private static GroupedOpenApi group(final String name, final String... packagesToScan) {
    return GroupedOpenApi.builder()
        .group(name)
        .packagesToScan(packagesToScan)
        .pathsToExclude("/internal/**")
        .build();
  }
}
