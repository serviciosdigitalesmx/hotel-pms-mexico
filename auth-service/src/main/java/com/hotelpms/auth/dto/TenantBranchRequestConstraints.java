package com.hotelpms.auth.dto;

/** Shared validation constraints for tenant branch request payloads. */
final class TenantBranchRequestConstraints {

  static final int MAX_NAME_LENGTH = 120;
  static final int MAX_PHONE_LENGTH = 40;
  static final int MAX_EXTRA_FIELD_LENGTH = 200;
  static final int MAX_ADDRESS_LENGTH = 500;
  static final int MAX_TIME_ZONE_LENGTH = 80;
  static final int MAX_COUNTRY_LENGTH = 80;

  private TenantBranchRequestConstraints() {
  }
}
