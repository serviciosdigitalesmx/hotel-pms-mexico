package com.hotelpms.internalauth.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FixiRoleCapabilitiesTest {
  @Test
  void ownerAndAdminAreTenantAdministratorsButNotPlatformRoles() {
    assertThat(FixiRoleCapabilities.forRole("OWNER"))
        .contains(Capability.USERS_MANAGE, Capability.FINANCE_READ, Capability.SETTINGS_MANAGE)
        .doesNotContain(Capability.PLATFORM_HOTELS_MANAGE);
    assertThat(FixiRoleCapabilities.forRole("ADMIN"))
        .contains(Capability.USERS_MANAGE, Capability.FINANCE_READ)
        .doesNotContain(Capability.CAPABILITIES_MANAGE, Capability.PERMISSIONS_MANAGE,
            Capability.PLATFORM_HOTELS_MANAGE);
  }

  @Test
  void managerSupervisesWorkshopOperationsWithoutOwnerOrPlatformAuthority() {
    assertThat(FixiRoleCapabilities.forRole("MANAGER"))
        .contains(Capability.USERS_MANAGE, Capability.ORDERS_READ, Capability.INVENTORY_READ,
            Capability.REPORTS_READ)
        .doesNotContain(Capability.FINANCE_READ, Capability.CAPABILITIES_MANAGE,
            Capability.PERMISSIONS_MANAGE, Capability.PLATFORM_HOTELS_MANAGE);
  }

  @Test
  void receptionistAndTechnicianHaveOperationalTemplatesWithoutSensitiveAdministration() {
    assertThat(FixiRoleCapabilities.forRole("RECEPTIONIST"))
        .contains(Capability.CUSTOMERS_WRITE, Capability.ORDERS_CREATE,
            Capability.QUOTES_AUTHORIZE_RECORD, Capability.PAYMENTS_RECEIVE)
        .doesNotContain(Capability.USERS_MANAGE, Capability.FINANCE_READ,
            Capability.SETTINGS_MANAGE);
    assertThat(FixiRoleCapabilities.forRole("TECHNICIAN"))
        .contains(Capability.ORDERS_READ, Capability.ORDERS_UPDATE, Capability.INVENTORY_READ)
        .doesNotContain(Capability.FINANCE_READ, Capability.USERS_MANAGE,
            Capability.SETTINGS_MANAGE, Capability.REPORTS_READ);
  }

  @Test
  void historicalAndPlatformRolesAreNotSelectableWorkshopRoles() {
    assertThat(FixiRoleCapabilities.isEmployeeRole("KITCHEN")).isFalse();
    assertThat(FixiRoleCapabilities.isEmployeeRole("HOUSEKEEPER")).isFalse();
    assertThat(FixiRoleCapabilities.isEmployeeRole("SUPER_ADMIN")).isFalse();
    assertThat(FixiRoleCapabilities.isWorkshopRole("SUPER_ADMIN")).isFalse();
    assertThat(FixiRoleCapabilities.isWorkshopRole("OWNER")).isTrue();
    assertThat(FixiRoleCapabilities.isFixiCapability(Capability.PLATFORM_HOTELS_MANAGE)).isFalse();
  }
}
