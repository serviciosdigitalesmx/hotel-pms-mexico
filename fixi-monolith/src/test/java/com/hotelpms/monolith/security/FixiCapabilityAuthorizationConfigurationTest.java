package com.hotelpms.monolith.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.hotelpms.internalauth.contracts.Capability;
import org.junit.jupiter.api.Test;

class FixiCapabilityAuthorizationConfigurationTest {
  @Test
  void mapsFixiBusinessRoutesToTheirBackendCapabilities() {
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/customers", "GET")).isEqualTo(Capability.CUSTOMERS_READ);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/service-orders", "POST")).isEqualTo(Capability.ORDERS_CREATE);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/service-orders/123/authorize", "POST"))
        .isEqualTo(Capability.QUOTES_AUTHORIZE_RECORD);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/inventory/movements", "POST"))
        .isEqualTo(Capability.INVENTORY_ADJUST);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/procurement/purchases", "POST"))
        .isEqualTo(Capability.INVENTORY_PURCHASE);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/expenses", "GET")).isEqualTo(Capability.FINANCE_READ);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/reports/owner", "GET")).isEqualTo(Capability.FINANCE_READ);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/quotations", "GET")).isEqualTo(Capability.PLATFORM_HOTELS_MANAGE);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/rooms", "GET")).isEqualTo(Capability.PLATFORM_HOTELS_MANAGE);
  }

  @Test
  void leavesAuthenticationAndPlatformAuthorizationToTheirExistingSecurityPipelines() {
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/auth/login", "POST")).isNull();
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/auth/platform/tenants", "GET")).isNull();
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/auth/public/verify-email", "POST")).isNull();
  }

  @Test
  void mapsBillingAuditAndMessagingRoutesToWorkshopCapabilities() {
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/invoices", "GET")).isEqualTo(Capability.BILLING_READ);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/invoices/123/payments", "POST")).isEqualTo(Capability.PAYMENTS_RECEIVE);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/invoices/123/payments/456/refund", "POST"))
        .isEqualTo(Capability.PAYMENTS_REFUND);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/audit-events", "GET")).isEqualTo(Capability.REPORTS_READ);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/whatsapp/inbox/conversations", "GET"))
        .isEqualTo(Capability.SETTINGS_MANAGE);
    assertThat(FixiCapabilityAuthorizationConfiguration.requiredCapability(
        "/api/v1/notifications/preferences", "GET")).isEqualTo(Capability.SETTINGS_READ);
  }
}
